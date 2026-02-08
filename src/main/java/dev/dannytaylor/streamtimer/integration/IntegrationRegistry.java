/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration;

import dev.dannytaylor.streamtimer.integration.twitch.TwitchIntegration;
import dev.dannytaylor.streamtimer.integration.websocket.WebSocketIntegration;

public class IntegrationRegistry {
    public static void bootstrap() {
        AuthConfig.bootstrap();
        TwitchIntegration.bootstrap();
        WebSocketIntegration.bootstrap();
    }

    public static void close() {
        TwitchIntegration.close();
        WebSocketIntegration.close();
    }
}
