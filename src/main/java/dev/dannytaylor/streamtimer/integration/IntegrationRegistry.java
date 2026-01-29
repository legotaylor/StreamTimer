package dev.dannytaylor.streamtimer.integration;

import dev.dannytaylor.streamtimer.integration.twitch.TwitchIntegration;

public class IntegrationRegistry {
    public static void bootstrap() {
        AuthConfig.bootstrap();
        TwitchIntegration.bootstrap();
    }
}
