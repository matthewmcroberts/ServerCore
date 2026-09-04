package com.matthewmcroberts.modules.rank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.rank.client.RankWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getLogger;

@RequiredArgsConstructor
public class RankModule implements ServerModule {
    private final JavaPlugin plugin;

    private RankWebSocketClient client;

    private ObjectMapper objectMapper;

    @Override
    public void setup() {
        objectMapper = new ObjectMapper();

        client = new RankWebSocketClient(this.plugin, objectMapper);

        client.connect(
                "ws://localhost:8080/ws"
        );

        getLogger().info("RankModule setup complete.");
    }

    @Override
    public void teardown() {
        if(client != null) {
            client.disconnect();
        }

        client = null;
        objectMapper = null;
    }
}
