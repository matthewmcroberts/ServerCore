package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.converter.RawStringMessageConverter;
import jakarta.websocket.WebSocketContainer;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.tyrus.client.ClientManager;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

public class RankWebSocketClient {

    private final JavaPlugin plugin;
    private final WebSocketStompClient stompClient;
    private final WebSocketEventHandler eventHandler;

    @Getter
    private StompSession session;

    public RankWebSocketClient(JavaPlugin plugin, ObjectMapper objectMapper, WeakReference<RankModule> rankModuleReference) {
        this.plugin = plugin;

        WebSocketContainer webSocketContainer = ClientManager.createClient();

        StandardWebSocketClient webSocketClient =
                new StandardWebSocketClient(webSocketContainer);

        this.stompClient = new WebSocketStompClient(webSocketClient);

        this.stompClient.setMessageConverter(new RawStringMessageConverter());

        this.eventHandler = new WebSocketEventHandler(plugin, objectMapper, rankModuleReference);
    }

    public CompletableFuture<StompSession> connect(String url) {
        plugin.getLogger().info("Connecting to RankManager WebSocket: " + url);

        CompletableFuture<StompSession> future = stompClient.connectAsync(url, eventHandler);

        future.thenAccept(session -> {
            this.session = session;

            plugin.getLogger().info("Connected to RankManager WebSocket.");

            subscribeToEvents(session);

        }).exceptionally(exception -> {
            plugin.getLogger().severe("Failed to connect to RankManager WebSocket: " + exception.getMessage());
            return null;
        });

        return future;
    }

    private void subscribeToEvents(StompSession session) {
        session.subscribe("/topic/player-rank-assign", eventHandler);
        session.subscribe("/topic/player-rank-remove", eventHandler);
        session.subscribe("/topic/rank-create", eventHandler);
        session.subscribe("/topic/rank-delete", eventHandler);
        session.subscribe("/topic/rank-inheritance", eventHandler);
        session.subscribe("/topic/rank-permission", eventHandler);
        session.subscribe("/topic/rank-update", eventHandler);

        plugin.getLogger().info("Subscribed to all RankManager events.");
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public void disconnect() {

        if (session != null && session.isConnected()) {
            session.disconnect();
        }

        if (stompClient.isRunning()) {
            stompClient.stop();
        }

        session = null;
    }
}