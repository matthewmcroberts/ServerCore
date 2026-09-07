package com.matthewmcroberts.modules.rank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.rank.client.RankClient;
import com.matthewmcroberts.modules.rank.client.RankWebSocketClient;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignmentDto;
import com.matthewmcroberts.modules.rank.client.dto.RankDto;
import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class RankModule implements ServerModule, Listener {
    private static final String WEB_SOCKET_CONNECTION_STRING = "ws://localhost:8080/ws";

    private final JavaPlugin plugin;

    private final Map<String, Rank> ranksCache = new ConcurrentHashMap<>();

    private final Map<UUID, PlayerRankAssignmentDto> rankAssignments = new ConcurrentHashMap<>();

    private final Map<UUID, CompletableFuture<PlayerRankAssignmentDto>> pendingRankAssignments =
            new ConcurrentHashMap<>();

    private RankWebSocketClient webSocketClient;

    @Getter
    private RankClient rankClient;

    private ObjectMapper objectMapper;

    private PermissionHandler permissionHandler;

    @Override
    public void setup() {
        this.objectMapper = new ObjectMapper();

        this.webSocketClient = new RankWebSocketClient(this.plugin, objectMapper, new WeakReference<>(this));
        this.rankClient = new RankClient(objectMapper);

        this.permissionHandler = new PermissionHandler(this.plugin);

        webSocketClient.connect(WEB_SOCKET_CONNECTION_STRING);

        this.getAllRanks();

        log.info("RankModule setup complete!");
    }

    @Override
    public void teardown() {
        if(webSocketClient != null) {
            webSocketClient.disconnect();
        }

        ranksCache.clear();

        rankClient = null;
        webSocketClient = null;
        objectMapper = null;
    }

    public @NonNull CompletableFuture<RankDto> createRank(
            @NonNull final String rankId,
            @NonNull final String rawDisplayName,
            final int priority,
            @NonNull final Set<String> ownPermissions,
            @NonNull final Set<String> effectivePermissions,
            @NonNull final Set<String> inheritedRankIds) {
        final RankDto newRankDto = RankDto.builder()
                .id(rankId)
                .displayName(rawDisplayName)
                .priority(priority)
                .ownPermissions(ownPermissions)
                .effectivePermissions(effectivePermissions)
                .inheritedRankIds(inheritedRankIds)
                .build();

        return rankClient.createRank(newRankDto).thenApply(rank -> {
            this.ranksCache.put(rank.getId(), new Rank(newRankDto, this));
            return rank;
        });
    }

    public @NonNull CompletableFuture<Void> deleteRank(@NonNull final String rankId) {
        return rankClient.deleteRank(rankId).thenAccept(_ -> this.ranksCache.remove(rankId));
    }

    public CompletableFuture<Optional<Rank>> getRankById(@NonNull final String rankId) {
        final Rank cachedRankDto = this.ranksCache.get(rankId);

        if(cachedRankDto != null) {
            return CompletableFuture.completedFuture(Optional.of(cachedRankDto));
        }

        return rankClient.getRankById(rankId).thenApply(rankDto -> {
           final Rank rank = new Rank(rankDto, this);
           this.ranksCache.put(rank.getRankId(), rank);
           return Optional.of(rank);
        });
    }

    public CompletableFuture<List<Rank>> getAllRanks() {
        if(!ranksCache.isEmpty()) {
            return CompletableFuture.completedFuture(List.copyOf(ranksCache.values()));
        }

        return rankClient.getAllRanks().thenApply(ranks -> {
            ranksCache.clear();

            ranks.forEach(rankDto -> {
                final Rank rank = new Rank(rankDto, this);
                ranksCache.put(rank.getRankId(), rank);
            });
            return List.copyOf(ranksCache.values());
        });
    }

    private Optional<Rank> getRankForOnlinePlayer(@NonNull final Player player) {
        final UUID playerId = player.getUniqueId();

        final PlayerRankAssignmentDto assignment = this.rankAssignments.get(playerId);
        if (assignment == null) {
            return Optional.empty();
        }

        return Optional.of(this.ranksCache.get(assignment.getRankId()));
    }

    /**
     * Handles player login events.
     *
     * @param event the player pre-login event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
        final UUID playerId = event.getUniqueId();
        final CompletableFuture<PlayerRankAssignmentDto> rankFuture = this.rankClient.getPlayerRankAssignment(playerId.toString())
                .whenComplete((rankAssignment, throwable) -> {
                    // Remove from pending map regardless
                    this.pendingRankAssignments.remove(playerId);

                    if (throwable != null) {
                        log.error("Failed to fetch rank assignments for player {}", playerId, throwable);
                        return;
                    }

                    this.rankAssignments.put(playerId, rankAssignment);
                });

        this.pendingRankAssignments.put(playerId, rankFuture);
    }

    /**
     * Handles calculating player permissions.
     *
     * @param event the player join event
     */
    @EventHandler(ignoreCancelled = true)
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        final CompletableFuture<PlayerRankAssignmentDto> pendingFuture = this.pendingRankAssignments.get(playerId);

        final Runnable playerJoinTask = () -> {
            log.info("Recalculating permissions");
            final Optional<Rank> rank = this.getRankForOnlinePlayer(player);
            if(rank.isPresent()) {
                this.permissionHandler.recalculatePermissionsForPlayer(player, rank.get());
            } else {
                log.error("Rank was not present when recalculating permissions");
            }
        };

        if (pendingFuture != null && !pendingFuture.isDone()) {
            pendingFuture.thenRun(playerJoinTask);
        } else {
            playerJoinTask.run();
        }
    }

    /**
     * Handles player disconnect events, removing rank assignments from cache.
     *
     * @param event the player connection close event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerDisconnect(final PlayerQuitEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();
        this.rankAssignments.remove(playerId);
        this.permissionHandler.removePlayerPermissionAttachment(playerId);
    }


    // getPlayersWithRank
    // getPlayerRankAssignments
    // getPlayerRanks
}
