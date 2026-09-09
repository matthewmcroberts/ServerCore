package com.matthewmcroberts;

import com.matthewmcroberts.modules.ServerModuleManager;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.RankModuleImpl;
import com.matthewmcroberts.modules.rank.commands.RankCommand;
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
        moduleManager.registerModule(rankModule);

        moduleManager.setup();

        Bukkit.getPluginManager().registerEvents(rankModule, this);
        getLogger().info("ServerEngine has been enabled!");
    }

    @Override
    public void onDisable() {
        moduleManager.teardown();
        getLogger().info("ServerEngine has been disabled!");
    }
}
