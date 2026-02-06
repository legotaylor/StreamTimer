/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.integration.AuthConfig;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TwitchAuthenticator {
    private static final String authURL = "https://id.twitch.tv/oauth2/authorize";
    private static final String tokenURL = "https://id.twitch.tv/oauth2/token";

    private static final String scopes = String.join(" ", "chat:read", "bits:read", "channel:read:subscriptions");

    private final String clientId;
    private final String clientSecret;
    private final int port;

    public TwitchAuthenticator(String clientId, String clientSecret, int port) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.port = port;
    }

    public OAuth2Credential authenticate() {
        try {
            String accessToken = null;

            if (!AuthConfig.instance.twitchAccessToken.value().isBlank()) {
                if (AuthConfig.instance.twitchTokenExpiry.value() < System.currentTimeMillis() + 60_000) {
                    try {
                        StreamTimerLoggerImpl.info("[Twitch Integration] Authenticating via saved refresh access token...");
                        accessToken = refreshAccessToken(AuthConfig.decrypt(AuthConfig.instance.twitchRefreshToken.value()));
                    } catch (Exception error) {
                        StreamTimerLoggerImpl.error("[Twitch Integration] Failed to decrypt saved refresh access token: " + error);
                    }
                } else {
                    try {
                        StreamTimerLoggerImpl.info("[Twitch Integration] Authenticating via saved access token...");
                        accessToken = AuthConfig.decrypt(AuthConfig.instance.twitchAccessToken.value());
                    } catch (Exception error) {
                        StreamTimerLoggerImpl.error("[Twitch Integration] Failed to decrypt saved access token: " + error);
                    }
                }
            }

            if (accessToken == null) {
                StreamTimerLoggerImpl.info("[Twitch Integration] Requesting new access token...");
                CompletableFuture<String> codeFuture = new CompletableFuture<>();
                startCallback(codeFuture);
                openBrowser(getAuthURL());
                accessToken = getToken(codeFuture.join());
            }

            return new OAuth2Credential("twitch", accessToken);
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("[Twitch Integration] Failed to authenticate with Twitch: " + error);
            return null;
        }
    }

    private String parseToken(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String accessToken = jsonObject.get("access_token").getAsString();
        try {
            AuthConfig.instance.twitchAccessToken.setValue(AuthConfig.encrypt(accessToken), true);
            AuthConfig.instance.twitchRefreshToken.setValue(AuthConfig.encrypt(jsonObject.get("refresh_token").getAsString()), true);
            AuthConfig.instance.twitchTokenExpiry.setValue(System.currentTimeMillis() + jsonObject.get("expires_in").getAsLong() * 1000L, true);
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("[Twitch Integration] Failed to save tokens: " + error);
        }
        return accessToken;
    }

    private String refreshAccessToken(String refreshToken) throws IOException, InterruptedException {
        return parseToken(HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(tokenURL)).POST(HttpRequest.BodyPublishers.ofString("grant_type=refresh_token" + "&refresh_token=" + encode(refreshToken) + "&client_id=" + this.clientId + "&client_secret=" + clientSecret)).header("Content-Type", "application/x-www-form-urlencoded").build(), HttpResponse.BodyHandlers.ofString()).body());
    }

    private String getToken(String code) throws IOException, InterruptedException {
        return parseToken(HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(tokenURL)).POST(HttpRequest.BodyPublishers.ofString("client_id=" + this.clientId + "&client_secret=" + this.clientSecret + "&code=" + code + "&grant_type=authorization_code" + "&redirect_uri=" + encode(getRedirect()))).header("Content-Type", "application/x-www-form-urlencoded").build(), HttpResponse.BodyHandlers.ofString()).body());
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isBlank()) return Map.of();
        return Arrays.stream(query.split("&")).map(p -> p.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    private void startCallback(CompletableFuture<String> codeFuture) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if (!codeFuture.isDone()) {
                codeFuture.completeExceptionally(new TimeoutException("Authentication Timed Out!"));
                server.stop(0);
            }
            scheduler.shutdown();
        }, AuthConfig.instance.twitchTimeout.value(), TimeUnit.MILLISECONDS);

        server.createContext("/callback", exchange -> {
            Map<String, String> parameters = parseQuery(exchange.getRequestURI().getQuery());
            String code = parameters.get("code");
            String response = "<html><head><style>body {color: #ffffff;background-color: #3c3f41;font-family: sans-serif;display: flex;justify-content: center;align-items: center;height: 200px;margin: auto;width: 50%;padding: 10px;text-align: center;}</style><title>" + StaticVariables.name + " - Authentication complete</title></head><body><div><h2>Authentication complete</h2><p>You may now close this window</p></div></body></html>";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            codeFuture.complete(code);
            server.stop(0);
        });
        server.start();
    }

    private void openBrowser(String url) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI(url));
    }

    private String getAuthURL() {
        return authURL + "?client_id=" + this.clientId + "&redirect_uri=" + encode(getRedirect()) + "&response_type=code" + "&scope=" + encode(scopes);
    }

    private String getRedirect() {
        return "http://localhost:" + this.port + "/callback";
    }

    private String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}