/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.websocket;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.BindException;
import java.net.InetSocketAddress;

public class WebSocketConnection extends WebSocketServer {
    public WebSocketConnection(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        StreamTimerLoggerImpl.info("[WebSocket Integration] New connection: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        StreamTimerLoggerImpl.info("[WebSocket Integration] Closed connection: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        StreamTimerLoggerImpl.info("[WebSocket Integration] (" + webSocket.getRemoteSocketAddress() + "): " + s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        StreamTimerLoggerImpl.error("[WebSocket Integration] Error: " + e);
    }

    @Override
    public void onStart() {
        StreamTimerLoggerImpl.info("[WebSocket Integration] Started server on port " + this.getPort() + "!");
    }

    private void sendFrame(WebSocket webSocket, byte[] frame) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(frame);
        }
    }

    public void sendFrame(byte[] frame) {
        for (WebSocket webSocket : this.getConnections()) {
            this.sendFrame(webSocket, frame);
        }
    }
}
