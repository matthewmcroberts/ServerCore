package com.matthewmcroberts.modules;

import java.util.HashSet;
import java.util.Set;

public class ServerModuleManager implements ServerModule {
    private static ServerModuleManager instance;
    private final Set<ServerModule> registeredModules;

    private ServerModuleManager() {
        registeredModules = new HashSet<>();
    }

    public static ServerModuleManager getInstance() {
        if(instance == null) {
            instance = new ServerModuleManager();
        }
        return instance;
    }

    public ServerModuleManager registerModule(ServerModule module) {
        registeredModules.add(module);
        return this;
    }

    public <T extends ServerModule> T getRegisteredModule(Class<T> clazz) {
        for(ServerModule module: registeredModules) {
            if(clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }
        return null;
    }

    @Override
    public void setup() {
        for(ServerModule module: registeredModules) {
            module.setup();
        }
    }

    @Override
    public void teardown() {
        for(ServerModule module: registeredModules) {
            module.teardown();
        }
    }
}
