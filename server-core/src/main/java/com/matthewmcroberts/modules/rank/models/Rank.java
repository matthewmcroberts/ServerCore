package com.matthewmcroberts.modules.rank.models;

import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.client.RankClient;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@Slf4j
public final class Rank {
    @Getter
    private final com.matthewmcroberts.modules.rank.client.dto.Rank delegate;

    @Getter(AccessLevel.PROTECTED)
    private final WeakReference<RankModule> rankModuleReference;

    private @NonNull RankModule getRankModule() {
        return Objects.requireNonNull(this.rankModuleReference.get(), "RankModule is no longer available.");
    }

    public Rank(@NonNull final com.matthewmcroberts.modules.rank.client.dto.Rank delegate, @NonNull final RankModule rankModule) {
        this.delegate = delegate;
        this.rankModuleReference = new WeakReference<>(rankModule);
    }

    private RankClient getRankClient() {
        return this.getRankModule().getRankClient();
    }

    public @NonNull String getRankId() {
        return this.getDelegate().getRankId();
    }

    public @NonNull String getRawDisplayName() {
        return this.getDelegate().getDisplayName();
    }

    public int getPriority() {
        return this.getDelegate().getPriority();
    }

    public boolean hasOwnPermission(@NonNull final String permission) {
        return this.getOwnPermissions().contains(permission);
    }

    public boolean hasEffectivePermission(@NonNull final String permission) {
        return this.getEffectivePermissions().contains(permission);
    }

    public @NonNull Set<String> getOwnPermissions() {
        return Set.copyOf(this.getDelegate().getOwnPermissions());
    }

    public @NonNull Set<String> getEffectivePermissions() {
        return Set.copyOf(this.getDelegate().getEffectivePermissions());
    }

    public @NonNull Set<String> getInheritedRankIds() {
        return Set.copyOf(this.getDelegate().getInheritedRankIds());
    }

    public CompletableFuture<Void> setDisplayName(@NonNull final String rawDisplayName) {
        return this.getRankClient()
                .updateRankDisplayName(this.getRankId(), rawDisplayName)
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> setPriority(final int priority) {
        return this.getRankClient()
                .updateRankPriority(this.getRankId(), priority)
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> addPermissions(@NonNull final Set<PermissionNode> nodes) {
        final Set<String> rawNodes =
                nodes.stream().map(PermissionNode::getValue).collect(Collectors.toSet());
        return this.getRankClient()
                .addRankPermissions(this.getRankId(), rawNodes)
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> removePermissions(@NonNull final Set<PermissionNode> nodes) {
        return this.getRankClient()
                .removeRankPermissions(
                        this.getRankId(),
                        nodes.stream().map(PermissionNode::getValue).collect(Collectors.toSet()))
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> addInheritedRankIds(@NonNull final Set<String> inheritedRankIds) {
        return this.getRankClient()
                .addRankInheritance(this.getRankId(), inheritedRankIds)
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> removeInheritedRankIds(@NonNull final Set<String> inheritedRankIds) {
        return this.getRankClient()
                .removeRankInheritance(this.getRankId(), inheritedRankIds)
                .thenApply(rank -> null);
    }

    public CompletableFuture<Void> assignPlayer(@NonNull final UUID playerId, @NonNull final UUID assignedById) {
        return this.getRankClient()
                .assignPlayerRank(playerId.toString(), assignedById.toString(), this.getRankId())
                .thenApply(ignored -> null);
    }

    public CompletableFuture<Void> unAssignPlayer(@NonNull final UUID playerId) {
        return this.getRankClient().removePlayerRank(playerId.toString()).thenApply(ignored -> null);
    }

    public List<Player> getOnlinePlayersWithRank() {
        return this.getRankModule().getOnlinePlayersWithRank(this.getRankId());
    }

    public Set<String> getAllInheritedPermissions() {
        return this.getEffectivePermissions().stream()
                .filter(permission -> !this.getOwnPermissions().contains(permission))
                .collect(Collectors.toSet());
    }

    public @NonNull Component getRenderedDisplayName() {
        return MiniMessage.miniMessage().deserialize(this.getRawDisplayName());
    }
}
