package com.matthewmcroberts.modules.rank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.rank.client.RankClient;
import com.matthewmcroberts.modules.rank.client.RankWebSocketClient;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignment;
import com.matthewmcroberts.modules.rank.client.dto.Rank;
import com.matthewmcroberts.modules.rank.commands.RankCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class RankModuleImpl implements RankModule {
    private static final String WEB_SOCKET_CONNECTION_STRING = "ws://localhost:8080/ws";

    private final JavaPlugin plugin;

    private final Map<String, com.matthewmcroberts.modules.rank.models.Rank> ranksCache = new ConcurrentHashMap<>();

    private final Map<UUID, PlayerRankAssignment> rankAssignments = new ConcurrentHashMap<>();

    private final Map<UUID, CompletableFuture<PlayerRankAssignment>> pendingRankAssignments =
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

        this.internalGetAllRanks();

        this.plugin.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(
                        new RankCommand(new WeakReference<>(this)).buildNode()
                )
        );

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

    private com.matthewmcroberts.modules.rank.models.Rank convertRank(Rank rank) {
        return new com.matthewmcroberts.modules.rank.models.Rank(rank, this);
    }

    public @NonNull CompletableFuture<Rank> createRank(
            @NonNull final String rankId,
            @NonNull final String rawDisplayName,
            final int priority,
            @NonNull final Set<String> ownPermissions,
            @NonNull final Set<String> effectivePermissions,
            @NonNull final Set<String> inheritedRankIds) {
        final Rank newRank = Rank.builder()
                .rankId(rankId)
                .displayName(rawDisplayName)
                .priority(priority)
                .ownPermissions(ownPermissions)
                .effectivePermissions(effectivePermissions)
                .inheritedRankIds(inheritedRankIds)
                .build();

        return rankClient.createRank(newRank).thenApply(rank -> {
            this.ranksCache.put(rank.getRankId(), new com.matthewmcroberts.modules.rank.models.Rank(newRank, this));
            return rank;
        });
    }

    public @NonNull CompletableFuture<Void> deleteRank(@NonNull final String rankId) {
        return rankClient.deleteRank(rankId).thenAccept(_ -> this.ranksCache.remove(rankId));
    }

    public @NonNull CompletableFuture<Optional<com.matthewmcroberts.modules.rank.models.Rank>> internalGetRankById(@NonNull final String rankId) {
        final com.matthewmcroberts.modules.rank.models.Rank cachedRank = this.ranksCache.get(rankId);

        if(cachedRank != null) {
            return CompletableFuture.completedFuture(Optional.of(cachedRank));
        }

        return rankClient.getRankById(rankId).thenApply(rankDto -> {
           final com.matthewmcroberts.modules.rank.models.Rank rank = this.convertRank(rankDto);
           this.ranksCache.put(rank.getRankId(), rank);
           return Optional.of(rank);
        });
    }

    public @NonNull CompletableFuture<List<com.matthewmcroberts.modules.rank.models.Rank>> internalGetAllRanks() {
        if(!ranksCache.isEmpty()) {
            return CompletableFuture.completedFuture(List.copyOf(ranksCache.values()));
        }

        return rankClient.getAllRanks().thenApply(ranks -> {
            ranksCache.clear();

            ranks.forEach(rankDto -> {
                final com.matthewmcroberts.modules.rank.models.Rank rank = this.convertRank(rankDto);
                ranksCache.put(rank.getRankId(), rank);
            });
            return List.copyOf(ranksCache.values());
        });
    }

    public @NonNull List<com.matthewmcroberts.modules.rank.models.Rank> getAllRanks() {
        if(ranksCache.isEmpty()) {
            log.error("Ranks cache is empty");
            return List.of();
        }

        return List.copyOf(this.ranksCache.values());
    }

    public @NonNull Optional<com.matthewmcroberts.modules.rank.models.Rank> getRankById(@NonNull final String rankId) {
        return Optional.of(this.ranksCache.get(rankId));
    }

    public @NonNull Optional<com.matthewmcroberts.modules.rank.models.Rank> getRankForOnlinePlayer(@NonNull final Player player) {
        final UUID playerId = player.getUniqueId();

        final PlayerRankAssignment assignment = this.rankAssignments.get(playerId);
        if (assignment == null) {
            return Optional.empty();
        }

        final com.matthewmcroberts.modules.rank.models.Rank rank = this.ranksCache.get(assignment.getRankId());
        if (rank == null) {
            return Optional.empty();
        }

        return Optional.of(rank);
    }

    public @NonNull List<@NonNull Player> getOnlinePlayersWithRank(@NonNull final String rankId) {
        final List<Player> players = new ArrayList<>();
        for(final Player player: Bukkit.getOnlinePlayers()) {
            Optional<com.matthewmcroberts.modules.rank.models.Rank> rankOpt = this.getRankForOnlinePlayer(player);

            rankOpt.ifPresent(rank -> {
               if(rankId.equals(rank.getRankId())) {
                   players.add(player);
               }
            });
        }

        return players;
    }

    public @NonNull CompletableFuture<PlayerRankAssignment> getOfflinePlayerRankAssignment(@NonNull final UUID playerId) {
        return this.rankClient.getPlayerRankAssignment(playerId.toString());
    }

    public @NonNull Optional<PlayerRankAssignment> getOnlinePlayerRankAssignment(@NonNull final UUID playerId) {
        return Optional.ofNullable(this.rankAssignments.get(playerId));
    }

    public Optional<PermissionAttachment> getPlayerPermissionAttachment(@NonNull final UUID playerId) {
        return this.permissionHandler.getPlayerPermissionAttachment(playerId);
    }

    public void updatePlayerRankAssignmentsCache(@NonNull final PlayerRankAssignment rankAssignment) {
        this.rankAssignments.put(UUID.fromString(rankAssignment.getPlayerId()), rankAssignment);
    }

    public void recalculatePermissions(final Collection<Player> players) {
        this.permissionHandler.recalculatePermissionsForPlayers(players, this::getRankForOnlinePlayer);
    }

    public void removePlayerRankAssignmentFromCache(@NonNull final UUID playerId) {
        this.rankAssignments.remove(playerId);
    }

    public void updateRanksCache(@NonNull final Rank rank) {
        final com.matthewmcroberts.modules.rank.models.Rank convertedRank = this.convertRank(rank);
        this.ranksCache.put(convertedRank.getRankId(), convertedRank);
    }

    public void removeFromRanksCache(@NonNull final Rank rank) {
        this.ranksCache.remove(rank.getRankId());
    }

    /**
     * Handles player login events.
     *
     * @param event the player pre-login event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
        final UUID playerId = event.getUniqueId();
        final CompletableFuture<PlayerRankAssignment> rankFuture = this.rankClient.getPlayerRankAssignment(playerId.toString())
                .whenComplete((rankAssignment, throwable) -> {
                    // Remove from pending map regardless
                    this.pendingRankAssignments.remove(playerId);

                    if (throwable != null) {
                        log.info("Failed to fetch rank assignments for player {}", playerId);
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

        final CompletableFuture<PlayerRankAssignment> pendingFuture = this.pendingRankAssignments.get(playerId);

        final Runnable playerJoinTask = () -> {
            log.info("Recalculating permissions");
            final Optional<com.matthewmcroberts.modules.rank.models.Rank> rank = this.getRankForOnlinePlayer(player);
            if(rank.isPresent()) {
                this.permissionHandler.recalculatePermissionsForPlayer(player, rank.get());
            } else {
                log.info("Rank was not present when recalculating permissions");
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
}
