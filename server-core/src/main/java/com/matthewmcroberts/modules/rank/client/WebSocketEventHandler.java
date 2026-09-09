package com.matthewmcroberts.modules.rank.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.RankMessages;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignment;
import com.matthewmcroberts.modules.rank.client.events.PlayerRankAssignEvent;
import com.matthewmcroberts.modules.rank.client.events.PlayerRankRemoveEvent;
import com.matthewmcroberts.modules.rank.client.events.RankCreateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankDeleteEvent;
import com.matthewmcroberts.modules.rank.client.events.RankInheritanceUpdateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankPermissionUpdateEvent;
import com.matthewmcroberts.modules.rank.client.events.RankUpdateEvent;
import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

@Slf4j
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

    private void onPlayerRankAssign(PlayerRankAssignEvent event) {
        final Optional<Rank> rankOpt =
                this.getRankModule().getRankById(event.getPlayerRankAssignment().getRankId());
        if (rankOpt.isEmpty()) {
            return;
        }

        final Rank rank = rankOpt.get();
        final String rawPlayerId = event.getPlayerRankAssignment().getPlayerId();
        try {
            final UUID playerId = UUID.fromString(rawPlayerId);
            final Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }

            this.getRankModule().updatePlayerRankAssignmentsCache(event.getPlayerRankAssignment());

            // Recalculate permissions for new rank
            this.getRankModule().recalculatePermissions(Set.of(player));

            RankMessages.Command.Assign.TARGET.send(player, Component.text(rank.getRankId()));
        } catch (final IllegalArgumentException e) {
            log.error(
                    "Failed to add rank assignment to cache for player[{}] due to bad id: {}",
                    rawPlayerId,
                    e.getMessage());
        }
    }

    private void onPlayerRankRemove(PlayerRankRemoveEvent event) {
        final Optional<Rank> rankOpt =
                this.getRankModule().getRankById(event.getPlayerRankAssignment().getRankId());
        if (rankOpt.isEmpty()) {
            return;
        }

        final Rank rank = rankOpt.get();
        final String rawPlayerId = event.getPlayerRankAssignment().getPlayerId();
        try {
            final UUID playerId = UUID.fromString(rawPlayerId);
            this.getRankModule().removePlayerRankAssignmentFromCache(playerId);

            final Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }

            this.getRankModule().recalculatePermissions(Set.of(player));

            RankMessages.Command.Unassign.TARGET.send(player, Component.text(rank.getRankId()));
        } catch (final IllegalArgumentException e) {
            log.error(
                    "Failed to remove rank assignment from cache for player[{}] due to bad id: {}",
                    rawPlayerId,
                    e.getMessage());
        }
    }

    private void onRankCreate(RankCreateEvent event) {
        this.getRankModule().updateRanksCache(event.getRank());
    }

    private void onRankDelete(RankDeleteEvent event) {
        final com.matthewmcroberts.modules.rank.client.dto.Rank deletedCommonRank = event.getDeletedRank();
        final Optional<Rank> deletedRankOpt =
                this.getRankModule().getRankById(deletedCommonRank.getRankId());

        if (deletedRankOpt.isEmpty()) {
            return;
        }

        final Rank deletedRank = deletedRankOpt.get();
        final Set<Player> affectedPlayers = new HashSet<>(deletedRank.getOnlinePlayersWithRank());

        this.getRankModule().removeFromRanksCache(deletedCommonRank);

        affectedPlayers.forEach(player -> {
            this.getRankModule().removePlayerRankAssignmentFromCache(player.getUniqueId());
            this.getRankModule().recalculatePermissions(affectedPlayers);
            RankMessages.Command.Unassign.TARGET.send(player, Component.text(deletedCommonRank.getRankId()));
        });

        final List<com.matthewmcroberts.modules.rank.client.dto.Rank> updatedInheritedByRanks = event.getUpdatedAffectedRanks().stream()
                .filter(rank ->
                        !rank.getRankId().equals(event.getDeletedRank().getRankId()))
                .toList();
        if (!updatedInheritedByRanks.isEmpty()) {
            for (final com.matthewmcroberts.modules.rank.client.dto.Rank affectedRank : updatedInheritedByRanks) {
                final Optional<Rank> rankOpt = this.getRankModule().getRankById(affectedRank.getRankId());

                if(rankOpt.isPresent()) {
                    final Rank rank = rankOpt.get();
                    rank.getDelegate().setInheritedRankIds(affectedRank.getInheritedRankIds());
                    rank.getDelegate().setEffectivePermissions(affectedRank.getEffectivePermissions());
                    this.getRankModule().recalculatePermissions(rank.getOnlinePlayersWithRank());
                    log.info(
                            "Rank[{}] was affected by deletion of Rank[{}]",
                            affectedRank.getRankId(),
                            deletedCommonRank.getRankId());
                }
            }
        } else {
            log.info("No ranks were affected by the deletion of Rank[{}]", deletedCommonRank.getRankId());
        }
    }

    private void onRankInheritanceUpdate(RankInheritanceUpdateEvent event) {
        final Optional<Rank> rankOpt = this.getRankModule().getRankById(event.getUpdatedRank().getRankId());
        if(rankOpt.isPresent()) {
            final Rank rank = rankOpt.get();
            rank.getDelegate().setInheritedRankIds(event.getUpdatedRank().getInheritedRankIds());
            rank.getDelegate().setEffectivePermissions(event.getUpdatedRank().getEffectivePermissions());

            final Set<Player> playersToUpdate = new HashSet<>(rank.getOnlinePlayersWithRank());
            this.updateAffectedAssignments(playersToUpdate, event.getUpdatedRank());

            for (final com.matthewmcroberts.modules.rank.client.dto.Rank affectedRank : event.getUpdatedAffectedRanks()) {
                final Optional<Rank> affectedRankOpt = this.getRankModule().getRankById(affectedRank.getRankId());
                if(affectedRankOpt.isPresent()) {
                    final Rank affected = affectedRankOpt.get();
                    affected.getDelegate().setEffectivePermissions(affectedRank.getEffectivePermissions());
                    this.updateAffectedAssignments(
                            affected.getOnlinePlayersWithRank(), affectedRank);
                    playersToUpdate.addAll(affected.getOnlinePlayersWithRank());
                }
            }

            this.getRankModule().recalculatePermissions(playersToUpdate);
        }
    }

    private void onRankPermissionUpdate(RankPermissionUpdateEvent event) {
        final Optional<Rank> rankOpt = this.getRankModule().getRankById(event.getUpdatedRank().getRankId());
        if(rankOpt.isPresent()) {
            final Rank rank = rankOpt.get();
            rank.getDelegate().setOwnPermissions(event.getUpdatedRank().getOwnPermissions());
            rank.getDelegate().setEffectivePermissions(event.getUpdatedRank().getEffectivePermissions());

            final List<Player> playersToUpdate = new ArrayList<>(rank.getOnlinePlayersWithRank());
            this.updateAffectedAssignments(playersToUpdate, event.getUpdatedRank());

            for (final com.matthewmcroberts.modules.rank.client.dto.Rank affectedRank : event.getUpdatedAffectedRanks()) {
                final Optional<Rank> commonAffectedRank = this.getRankModule().getRankById(affectedRank.getRankId());
                if(commonAffectedRank.isPresent()) {
                    final Rank affected = commonAffectedRank.get();
                    affected.getDelegate().setEffectivePermissions(affectedRank.getEffectivePermissions());
                    this.updateAffectedAssignments(
                            affected.getOnlinePlayersWithRank(), affectedRank);
                    playersToUpdate.addAll(affected.getOnlinePlayersWithRank());
                }
            }

            final Set<Player> uniquePlayers = new HashSet<>(playersToUpdate);
            this.getRankModule().recalculatePermissions(uniquePlayers);
        }
    }

    private void onRankUpdate(RankUpdateEvent event) {
        switch (event.getReason()) {
            case UPDATE_DISPLAY_NAME -> {
                final Optional<Rank> rankOpt = this.getRankModule().getRankById(event.getRank().getRankId());
                if(rankOpt.isPresent()) {
                    final Rank rank = rankOpt.get();
                    rank.getDelegate().setDisplayName(event.getRank().getDisplayName());
                    this.updateAffectedAssignments(rank.getOnlinePlayersWithRank(), event.getRank());
                }
            }
            case UPDATE_PRIORITY -> {
                final Optional<Rank> rankOpt = this.getRankModule().getRankById(event.getRank().getRankId());
                if(rankOpt.isPresent()) {
                    final Rank rank = rankOpt.get();
                    rank.getDelegate().setPriority(event.getRank().getPriority());
                    this.updateAffectedAssignments(rank.getOnlinePlayersWithRank(), event.getRank());
                }
            }
            default -> log.error("Received an unknown rank update reason. Please contact Mineplex.");
        }
    }


    private void updateAffectedAssignments(
            @NonNull final Collection<Player> affectedPlayerIds,
            @NonNull final com.matthewmcroberts.modules.rank.client.dto.Rank commonRank) {
        for (final Player player : affectedPlayerIds) {
            final Optional<PlayerRankAssignment> assignmentOpt = this.getRankModule().getOnlinePlayerRankAssignment(player.getUniqueId());
            if (assignmentOpt.isEmpty()) {
                log.error("Failed to find rank assignment for online player[{}]", player.getUniqueId());
                continue;
            }
            final PlayerRankAssignment assignment = assignmentOpt.get();
            assignment.setRankId(commonRank.getRankId());
        }
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        plugin.getLogger().severe(
                "RankManager WebSocket connection error: "
                        + exception.getMessage()
        );
    }
}