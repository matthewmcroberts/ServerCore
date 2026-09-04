package com.matthewmcroberts;

import com.matthewmcroberts.modules.ServerModuleManager;
import com.matthewmcroberts.modules.rank.RankModule;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerEngine extends JavaPlugin {
    private ServerModuleManager moduleManager;

    @Override
    public void onEnable() {
        moduleManager = ServerModuleManager.getInstance();

        moduleManager.registerModule(new RankModule(this));

        moduleManager.setup();

        getLogger().info("ServerEngine has been enabled!");
    }

    @Override
    public void onDisable() {
        moduleManager.teardown();
        getLogger().info("ServerEngine has been disabled!");
    }
}
