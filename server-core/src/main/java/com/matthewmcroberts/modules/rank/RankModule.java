package com.matthewmcroberts.modules.rank;

import com.matthewmcroberts.modules.ServerModule;
import com.matthewmcroberts.modules.rank.client.RankClient;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignment;
import com.matthewmcroberts.modules.rank.client.dto.Rank;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface RankModule extends ServerModule, Listener {
    @NonNull CompletableFuture<Rank> createRank(
            @NonNull final String rankId,
            @NonNull final String rawDisplayName,
            final int priority,
            @NonNull final Set<String> ownPermissions,
            @NonNull final Set<String> effectivePermissions,
            @NonNull final Set<String> inheritedRankIds);

    @NonNull CompletableFuture<Void> deleteRank(@NonNull final String rankId);

    @NonNull List<com.matthewmcroberts.modules.rank.models.Rank> getAllRanks();

    @NonNull Optional<com.matthewmcroberts.modules.rank.models.Rank> getRankById(@NonNull final String rankId);

    @NonNull Optional<com.matthewmcroberts.modules.rank.models.Rank> getRankForOnlinePlayer(@NonNull final Player player);

    @NonNull List<@NonNull Player> getOnlinePlayersWithRank(@NonNull final String rankId);

    @NonNull Optional<PlayerRankAssignment> getOnlinePlayerRankAssignment(@NonNull final UUID playerId);

    @NonNull RankClient getRankClient();

    @NonNull CompletableFuture<PlayerRankAssignment> getOfflinePlayerRankAssignment(@NonNull final UUID playerId);

    Optional<PermissionAttachment> getPlayerPermissionAttachment(@NonNull final UUID playerId);

    void updatePlayerRankAssignmentsCache(@NonNull final PlayerRankAssignment rankAssignment);

    void recalculatePermissions(final Collection<Player> players);

    void removePlayerRankAssignmentFromCache(@NonNull final UUID playerId);

    void updateRanksCache(@NonNull final Rank rank);

    void removeFromRanksCache(@NonNull final Rank rank);
}
