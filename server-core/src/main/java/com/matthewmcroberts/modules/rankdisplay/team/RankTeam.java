package com.matthewmcroberts.modules.rankdisplay.team;

import com.matthewmcroberts.modules.rankdisplay.RankDisplayModule;
import com.matthewmcroberts.modules.rankdisplay.model.MutableDisplayRank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RankTeam {
    @NonNull private final Set<Player> members = new HashSet<>();

    @NonNull private final MutableDisplayRank displayRank;

    private WeakReference<RankDisplayModule> rankDisplayModuleReference;
    private TeamDisplay display;

    private @NonNull RankDisplayModule getRankDisplayModule() {
        return Objects.requireNonNull(
                this.rankDisplayModuleReference.get(), "RankDisplayModule is no longer available");
    }

    public void setup(@NonNull final RankDisplayModule rankDisplayModule) {
        this.rankDisplayModuleReference = new WeakReference<>(rankDisplayModule);

        final ScoreboardTeam scoreboardTeam =
                rankDisplayModule.getTeamManager().createIfAbsent(this.displayRank.getId());

        this.display = scoreboardTeam.defaultDisplay();
        this.display.displayName(this.displayRank.getDisplayName());
        this.display.playerColor(NamedTextColor.WHITE);
        this.display.prefix(this.displayRank.getDisplayName().appendSpace());
        this.display.friendlyFire(true);
    }

    public void teardown() {
        this.getPlayers().forEach(pl -> this.display.removeEntry(pl.getName()));
        this.getRankDisplayModule().getTeamManager().removeTeam(this.displayRank.getId());
        this.display = null;

        this.rankDisplayModuleReference.clear();
        this.members.clear();
    }

    public void refresh() {
        this.display.displayName(this.displayRank.getDisplayName());
        this.display.prefix(this.displayRank.getDisplayName().appendSpace());

        for (final Player player : this.getPlayers()) {
            player.setPlayerListOrder(this.displayRank.getPriority());
        }
    }

    public void addPlayer(@NonNull final Player player) {
        this.display.addEntry(player.getName());
        this.members.add(player);
    }

    public void removePlayer(@NonNull final Player player) {
        this.display.removeEntry(player.getName());
        this.members.remove(player);
    }

    public @NonNull Set<Player> getPlayers() {
        return Set.copyOf(this.members);
    }
}
