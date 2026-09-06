package com.matthewmcroberts.modules.rank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.rank.client.RankApiClient;
import com.matthewmcroberts.modules.rank.client.RankWebSocketClient;
import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.Bukkit.getLogger;

@RequiredArgsConstructor
public class RankModule implements ServerModule {
    private final JavaPlugin plugin;

    private List<Rank> ranksCache = new ArrayList<>();

    private RankWebSocketClient webSocketClient;
    private RankApiClient apiClient;

    private ObjectMapper objectMapper;

    @Override
    public void setup() {
        objectMapper = new ObjectMapper();

        webSocketClient = new RankWebSocketClient(this.plugin, objectMapper, new WeakReference<>(this));
        apiClient = new RankApiClient(objectMapper);

        webSocketClient.connect(
                "ws://localhost:8080/ws"
        );

        this.getAllRanks();

        getLogger().info("RankModule setup complete.");
    }

    @Override
    public void teardown() {
        if(webSocketClient != null) {
            webSocketClient.disconnect();
        }

        apiClient = null;
        webSocketClient = null;
        objectMapper = null;
    }

    // createRank
    public @NonNull CompletableFuture<Rank> createRank(
            @NonNull final String rankId,
            @NonNull final String rawDisplayName,
            final int priority,
            @NonNull final Set<String> ownPermissions,
            @NonNull final Set<String> effectivePermissions,
            @NonNull final Set<String> inheritedRankIds) {
        final Rank newRank = Rank.builder()
                .id(rankId)
                .displayName(rawDisplayName)
                .priority(priority)
                .ownPermissions(ownPermissions)
                .effectivePermissions(effectivePermissions)
                .inheritedRankIds(inheritedRankIds)
                .build();

        return apiClient.createRank(newRank).thenApply(rank -> {
            this.ranksCache.add(rank);
            return rank;
        });
    }

    // getRankById
    // getAllRanks
    public CompletableFuture<List<Rank>> getAllRanks() {
        if(!ranksCache.isEmpty()) {
            return CompletableFuture.completedFuture(List.copyOf(ranksCache));
        }

        return apiClient.getAllRanks().thenApply(ranks -> {
            ranksCache.clear();
            ranksCache.addAll(ranks);
            return List.copyOf(ranks);
        });
    }

    // updateRankDisplayName
    // updateRankPriority
    // addRankPermissions
    // removeRankPermissions
    // addRankInheritance
    // removeRankInheritance
    // deleteRank
    // getRankByName
    // getPlayersWithRank
    // assignPlayerRank
    // getPlayerRankAssignments
    // removePlayerRank
    // getPlayerRanks
}
