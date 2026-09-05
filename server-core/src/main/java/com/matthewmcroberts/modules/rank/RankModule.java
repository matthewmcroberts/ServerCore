package com.matthewmcroberts.modules.rank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.rank.client.RankApiClient;
import com.matthewmcroberts.modules.rank.client.RankWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;

import static org.bukkit.Bukkit.getLogger;

@RequiredArgsConstructor
public class RankModule implements ServerModule {
    private final JavaPlugin plugin;

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
    // getRankById
    // getAllRanks
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
