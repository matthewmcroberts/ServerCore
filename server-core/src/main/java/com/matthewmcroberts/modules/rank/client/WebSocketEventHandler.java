package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.client.events.PlayerRankAssignEvent;
import com.matthewmcroberts.modules.rank.client.events.PlayerRankRemoveEvent;
import com.matthewmcroberts.modules.rank.client.events.RankCreateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankDeleteEvent;
import com.matthewmcroberts.modules.rank.client.events.RankInheritanceUpdateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankPermissionUpdateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankUpdateEvent;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.bukkit.Bukkit.getLogger;

public class WebSocketEventHandler extends StompSessionHandlerAdapter {

    private final JavaPlugin plugin;
    private final ObjectMapper objectMapper;

    private final WeakReference<RankModule> rankModuleReference;

    private @NonNull RankModule getRankModule() {
        return Objects.requireNonNull(this.rankModuleReference.get(), "RankModule is no longer available.");
    }

    public WebSocketEventHandler(JavaPlugin plugin, ObjectMapper objectMapper, WeakReference<RankModule> rankModuleReference) {
        this.plugin = plugin;
        this.objectMapper = objectMapper;
        this.rankModuleReference = rankModuleReference;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        plugin.getLogger().info("STOMP connection established.");
    }

    @Override
    public void handleException(StompSession session, StompCommand command,
                                StompHeaders headers, byte[] payload, Throwable exception) {
        plugin.getLogger().severe(
                "STOMP error on " + command + ": " + exception.getMessage()
        );
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        final String destination = headers.getDestination();

        if (destination == null) {
            return;
        }

        String json = payload.toString();

        try {
            switch (destination) {

                case "/topic/player-rank-assign" ->
                        handlePlayerRankAssign(json);

                case "/topic/player-rank-remove" ->
                        handlePlayerRankRemove(json);

                case "/topic/rank-create" ->
                        handleRankCreate(json);

                case "/topic/rank-delete" ->
                        handleRankDelete(json);

                case "/topic/rank-inheritance" ->
                        handleRankInheritanceUpdate(json);

                case "/topic/rank-permission" ->
                        handleRankPermissionUpdate(json);

                case "/topic/rank-update" ->
                        handleRankUpdate(json);

                default ->
                        plugin.getLogger().warning(
                                "Unknown WebSocket destination: "
                                        + destination
                        );
            }

        } catch (Exception exception) {

            plugin.getLogger().severe(
                    "Failed to deserialize WebSocket event from "
                            + destination
                            + ": "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    private void handlePlayerRankAssign(String json) throws Exception {
        PlayerRankAssignEvent event = objectMapper.readValue(json, PlayerRankAssignEvent.class);
        onPlayerRankAssign(event);
    }

    private void handlePlayerRankRemove(String json) throws Exception {
        PlayerRankRemoveEvent event = objectMapper.readValue(json, PlayerRankRemoveEvent.class);
        onPlayerRankRemove(event);
    }

    private void handleRankCreate(String json) throws Exception {
        RankCreateEvent event = objectMapper.readValue(json, RankCreateEvent.class);
        onRankCreate(event);
    }

    private void handleRankDelete(String json) throws Exception {
        RankDeleteEvent event = objectMapper.readValue(json, RankDeleteEvent.class);
        onRankDelete(event);
    }

    private void handleRankInheritanceUpdate(String json) throws Exception {
        RankInheritanceUpdateEvent event = objectMapper.readValue(json, RankInheritanceUpdateEvent.class);
        onRankInheritanceUpdate(event);
    }

    private void handleRankPermissionUpdate(String json) throws Exception {
        RankPermissionUpdateEvent event = objectMapper.readValue(json, RankPermissionUpdateEvent.class);
        onRankPermissionUpdate(event);
    }

    private void handleRankUpdate(String json) throws Exception {
        RankUpdateEvent event = objectMapper.readValue(json, RankUpdateEvent.class);
        onRankUpdate(event);
    }

    protected void onPlayerRankAssign(PlayerRankAssignEvent event) {
        // Handle event
        getLogger().info("onPlayerRankAssign");
    }

    protected void onPlayerRankRemove(PlayerRankRemoveEvent event) {
        getLogger().info("onPlayerRankRemove");
    }

    protected void onRankCreate(RankCreateEvent event) {
        getLogger().info(event.getRankDto().getId());
    }

    protected void onRankDelete(RankDeleteEvent event) {
        getLogger().info("onRankDelete");
    }

    protected void onRankInheritanceUpdate(RankInheritanceUpdateEvent event) {
        getLogger().info("onRankInheritanceUpdate");
    }

    protected void onRankPermissionUpdate(RankPermissionUpdateEvent event) {
        getLogger().info("onRankPermissionUpdate");
    }

    protected void onRankUpdate(RankUpdateEvent event) {
        getLogger().info("onRankUpdate");
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        plugin.getLogger().severe(
                "RankManager WebSocket connection error: "
                        + exception.getMessage()
        );
    }
}