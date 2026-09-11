package com.matthewmcroberts.modules.scoreboard;

import com.matthewmcroberts.modules.ServerModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
@Slf4j
public class ScoreboardModule implements ServerModule {
    private final Plugin plugin;

    @Getter
    @Delegate
    private ScoreboardLibrary scoreboardLibrary;

    @Override
    @SneakyThrows
    public void setup() {
        this.scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(this.plugin);
    }

    @Override
    public void teardown() {
        this.scoreboardLibrary.close();
    }
}
