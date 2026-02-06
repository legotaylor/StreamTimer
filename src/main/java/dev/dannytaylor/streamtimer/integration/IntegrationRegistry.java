/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration;

import dev.dannytaylor.streamtimer.integration.twitch.TwitchIntegration;
import dev.dannytaylor.streamtimer.integration.websocket.WebSocketIntegration;

import java.util.concurrent.CountDownLatch;

public class IntegrationRegistry {
    public static CountDownLatch closeLatch;

    public static void bootstrap() {
        AuthConfig.bootstrap();
        TwitchIntegration.bootstrap();
        WebSocketIntegration.bootstrap();
    }

    public static void close() {
        closeLatch = new CountDownLatch(1);
        TwitchIntegration.close();
        WebSocketIntegration.close();
        closeLatch.countDown();
    }
}
