/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.websocket;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class WebSocketConnection extends WebSocketServer {
    public WebSocketConnection() {
        super(new InetSocketAddress(StreamTimerConfig.instance.webSocketPort.value()));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("[Stream Timer/WebSocket Integration] New connection: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("[Stream Timer/WebSocket Integration] Closed connection: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("[Stream Timer/WebSocket Integration] (" + webSocket.getRemoteSocketAddress() + "): " + s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("[Stream Timer/WebSocket Integration] Error: " + e);
    }

    @Override
    public void onStart() {
        System.out.println("[Stream Timer/WebSocket Integration] Started server on port " + this.getPort() + "!");
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
