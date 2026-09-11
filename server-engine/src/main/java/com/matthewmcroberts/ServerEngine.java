package com.matthewmcroberts;

import com.matthewmcroberts.modules.ServerModuleManager;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.RankModuleImpl;
import com.matthewmcroberts.modules.rank.commands.RankCommand;
import com.matthewmcroberts.modules.rankdisplay.RankDisplayModule;
import com.matthewmcroberts.modules.scoreboard.ScoreboardModule;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;

public class ServerEngine extends JavaPlugin {
    private ServerModuleManager moduleManager;

    @Override
    public void onEnable() {
        moduleManager = ServerModuleManager.getInstance();

        final RankModule rankModule = new RankModuleImpl(this);
        final ScoreboardModule scoreboardModule = new ScoreboardModule(this);
        final RankDisplayModule displayModule = new RankDisplayModule(this);

        moduleManager.registerModule(rankModule)
                .registerModule(scoreboardModule)
                .registerModule(displayModule);

        moduleManager.setup();

        Bukkit.getPluginManager().registerEvents(rankModule, this);
        Bukkit.getPluginManager().registerEvents(displayModule, this);
        getLogger().info("ServerEngine has been enabled!");
    }

    @Override
    public void onDisable() {
        moduleManager.teardown();
        getLogger().info("ServerEngine has been disabled!");
    }
}
