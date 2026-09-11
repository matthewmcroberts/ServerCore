package com.matthewmcroberts.modules;

import java.util.ArrayList;
import java.util.List;

public class ServerModuleManager implements ServerModule {
    private static ServerModuleManager instance;

    private final List<ServerModule> registeredModules;

    private ServerModuleManager() {
        registeredModules = new ArrayList<>();
    }

    public static ServerModuleManager getInstance() {
        if (instance == null) {
            instance = new ServerModuleManager();
        }

        return instance;
    }

    public ServerModuleManager registerModule(ServerModule module) {
        registeredModules.add(module);
        return this;
    }

    public <T extends ServerModule> T getRegisteredModule(Class<T> clazz) {
        for (ServerModule module : registeredModules) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }

        return null;
    }

    @Override
    public void setup() {
        for (ServerModule module : registeredModules) {
            module.setup();
        }
    }

    @Override
    public void teardown() {
        for (int i = registeredModules.size() - 1; i >= 0; i--) {
            registeredModules.get(i).teardown();
        }
    }
}