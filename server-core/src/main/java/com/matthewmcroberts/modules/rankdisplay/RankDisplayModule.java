package com.matthewmcroberts.modules.rankdisplay;

import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.ServerModuleManager;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.events.PlayerRankAssignEvent;
import com.matthewmcroberts.modules.rank.events.PlayerRankUnassignEvent;
import com.matthewmcroberts.modules.rank.events.RankCreateEvent;
import com.matthewmcroberts.modules.rank.events.RankDeleteEvent;
import com.matthewmcroberts.modules.rank.models.Rank;
import com.matthewmcroberts.modules.rankdisplay.model.MutableDisplayRank;
import com.matthewmcroberts.modules.rankdisplay.team.RankTeam;
import com.matthewmcroberts.modules.scoreboard.ScoreboardModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RankDisplayModule implements ServerModule, Listener {
    private static final String GLOBAL_RANK_PREFIX = "global";
    private static final String DEFAULT_TEAM_ID = GLOBAL_RANK_PREFIX + "default";

    private final Map<String, RankTeam> rankTeams = new ConcurrentHashMap<>(7);

    @NonNull private final JavaPlugin plugin;

    private WeakReference<ScoreboardModule> scoreboardModuleReference;
    private WeakReference<RankModule> mineplexRankModuleReference;

    @Getter
    private TeamManager teamManager;

    private ScoreboardTeam defaultTeam;

    private BukkitTask refreshTask;

    public static String parseGlobalRankId(@NonNull final String rankId) {
        return GLOBAL_RANK_PREFIX + rankId;
    }

    public @NonNull ScoreboardModule getScoreboardModule() {
        return Objects.requireNonNull(this.scoreboardModuleReference.get(), "ScoreboardModule is no longer available");
    }

    public @NonNull RankModule getRankModule() {
        return Objects.requireNonNull(
                this.mineplexRankModuleReference.get(), "RankModule is no longer available");
    }

    @Override
    public void setup() {
        this.scoreboardModuleReference =
                new WeakReference<>(ServerModuleManager.getInstance().getRegisteredModule(ScoreboardModule.class));
        this.mineplexRankModuleReference =
                new WeakReference<>(ServerModuleManager.getInstance().getRegisteredModule(RankModule.class));

        this.teamManager = this.getScoreboardModule().createTeamManager();

        this.defaultTeam = this.getTeamManager().createIfAbsent(DEFAULT_TEAM_ID);
        this.defaultTeam.defaultDisplay().friendlyFire(true);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            final List<Rank> allRanks = new ArrayList<>(this.getRankModule().getAllRanks());

            for (final Rank rank : allRanks) {
                final String id = rank.getRankId();
                final RankTeam rankTeam = new RankTeam(MutableDisplayRank.builder()
                        .id(id)
                        .displayName(rank.getRenderedDisplayName())
                        .build());
                this.rankTeams.put(id, rankTeam);
                rankTeam.setup(this);
            }
        }, 20L);

        this.refreshTask = Bukkit.getScheduler()
                .runTaskTimer(this.plugin, this::handleRefreshTask, 0L, 12000);
    }

    @Override
    public void teardown() {
        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }

        for (final RankTeam rankTeam : this.rankTeams.values()) {
            rankTeam.teardown();
        }

        this.rankTeams.clear();

        this.teamManager.removeTeam(this.defaultTeam);
        this.teamManager.close();

        this.teamManager = null;
        this.defaultTeam = null;
    }

    private void handleRefreshTask() {
        final Set<Rank> allCurrentRanks = new HashSet<>();
        allCurrentRanks.addAll(this.getRankModule().getAllRanks());

        // Handle new or updated ranks
        for (final Rank rank : allCurrentRanks) {
            final String teamKey = this.resolveKey(rank);
            final RankTeam existingTeam = this.rankTeams.get(teamKey);

            if (existingTeam == null) {
                final RankTeam rankTeam = new RankTeam(MutableDisplayRank.builder()
                        .id(teamKey)
                        .displayName(rank.getRenderedDisplayName())
                        .build());

                this.rankTeams.put(teamKey, rankTeam);
                rankTeam.setup(this);
                continue;
            }

            existingTeam.getDisplayRank().setDisplayName(rank.getRenderedDisplayName());
            existingTeam.getDisplayRank().setPriority(rank.getPriority());
            existingTeam.refresh();
        }

        // Handle deleted ranks
        final Set<String> currentRankIds =
                allCurrentRanks.stream().map(this::resolveKey).collect(Collectors.toSet());

        final List<String> teamsToRemove = this.rankTeams.keySet().stream()
                .filter(teamKey -> !currentRankIds.contains(teamKey))
                .toList();

        for (final String teamKey : teamsToRemove) {
            final RankTeam rankTeam = this.rankTeams.remove(teamKey);
            if (rankTeam != null) {
                this.reassignPlayers(rankTeam);
                rankTeam.teardown();
            }
        }
    }

    private void reassignPlayers(@NonNull final RankTeam rankTeam) {
        for (final Player player : rankTeam.getPlayers()) {
            rankTeam.removePlayer(player);

            final Optional<Rank> currentRankOpt =
                    this.getRankModule().getRankForOnlinePlayer(player);
            if (currentRankOpt.isPresent()) {
                final RankTeam newRankTeam = this.rankTeams.get(this.resolveKey(currentRankOpt.get()));
                if (newRankTeam != null) {
                    newRankTeam.addPlayer(player);
                } else {
                    this.defaultTeam.defaultDisplay().addEntry(player.getName());
                }
            } else {
                this.defaultTeam.defaultDisplay().addEntry(player.getName());
            }
        }
    }

    private void assignToTeam(@NonNull final Player player, @Nullable final Rank rank) {
        this.teamManager.addPlayer(player);

        if (rank != null) {
            this.handlePlayerAssignment(player, rank);
        }
    }

    private void handlePlayerAssignment(@NonNull final Player player, @Nullable final Rank rank) {
        if (!player.isOnline()) {
            return;
        }

        if (rank == null) {
            this.defaultTeam.defaultDisplay().addEntry(player.getName());
            return;
        }

        final String teamKey = this.resolveKey(rank);
        final RankTeam rankTeam = this.rankTeams.get(teamKey);

        if (rankTeam != null) {
            rankTeam.addPlayer(player);
            player.setPlayerListOrder(rank.getPriority());
        } else {
            this.defaultTeam.defaultDisplay().addEntry(player.getName());
        }
    }

    private void removeFromTeam(@NonNull final Player player) {
        boolean found = false;
        for (final RankTeam team : this.rankTeams.values()) {
            if (team.getMembers().contains(player)) {
                team.removePlayer(player);
                found = true;
                break;
            }
        }

        if (!found) {
            this.defaultTeam.defaultDisplay().removeEntry(player.getName());
            player.setPlayerListOrder(0);
        }
    }

    private String resolveKey(@NonNull final Rank rank) {
        return rank.getRankId();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerJoin(@NonNull final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Optional<Rank> rankOpt = this.getRankModule().getRankForOnlinePlayer(player);

        rankOpt.ifPresent(rank -> this.assignToTeam(player, rank));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerQuit(@NonNull final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.removeFromTeam(player);
        this.teamManager.removePlayer(player);
    }

    @EventHandler
    private void onRankCreate(@NonNull final RankCreateEvent event) {
        final Rank rank = event.getRank();
        final RankTeam rankTeam = new RankTeam(MutableDisplayRank.builder()
                .id(rank.getRankId())
                .displayName(rank.getRenderedDisplayName())
                .build());

        rankTeam.setup(this);
        this.rankTeams.put(rank.getRankId(), rankTeam);
    }

    @EventHandler
    private void onRankDelete(@NonNull final RankDeleteEvent event) {
        final com.matthewmcroberts.modules.rank.client.dto.Rank rank = event.getDeletedRank();
        final String rankTeamId = rank.getRankId();
        final RankTeam rankTeam = this.rankTeams.remove(rankTeamId);
        if (rankTeam != null) {
            rankTeam.teardown();
        } else {
            log.warn("Tried to delete rank team for rank[{}] but it was missing.", rank.getRankId());
        }
    }

    @EventHandler
    private void onRankAssign(@NonNull final PlayerRankAssignEvent event) {
        final Player player = event.getPlayer();
        this.removeFromTeam(player);
        this.assignToTeam(player, event.getRank());
    }

    @EventHandler
    private void onRankUnassign(@NonNull final PlayerRankUnassignEvent event) {
        final Player player = event.getPlayer();
        this.removeFromTeam(player);
        this.assignToTeam(player, null);
    }

    @EventHandler
    private void onPlayerChat(@NonNull final AsyncChatEvent event) {
        final Player player = event.getPlayer();

        final Optional<Rank> rankOpt = this.getRankModule().getRankForOnlinePlayer(player);

        if (rankOpt.isEmpty()) {
            event.renderer((source, sourceDisplayName, message, viewer) ->
                    Component.empty()
                            .append(Component.text(" "))
                            .append(sourceDisplayName.color(NamedTextColor.YELLOW))
                            .append(Component.text(" ").color(NamedTextColor.YELLOW))
                            .append(message.color(NamedTextColor.WHITE))
            );
            return;
        }

        final Rank rank = rankOpt.get();

        final Component rankDisplay = rank.getRenderedDisplayName();

        event.renderer((source, sourceDisplayName, message, viewer) ->
                Component.empty()
                        .append(rankDisplay)
                        .append(Component.text(" "))
                        .append(sourceDisplayName.color(NamedTextColor.YELLOW))
                        .append(Component.text(" ").color(NamedTextColor.YELLOW))
                        .append(message.color(NamedTextColor.WHITE))
        );
    }
}
