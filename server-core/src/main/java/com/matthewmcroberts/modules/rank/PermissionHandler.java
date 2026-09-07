package com.matthewmcroberts.modules.rank;

import com.matthewmcroberts.modules.rank.models.Rank;
import com.matthewmcroberts.modules.rank.parsers.PermissionNodeParser;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Locked;
import lombok.NonNull;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * Handles permission management for players based on their assigned ranks.
 * Manages permission attachments, calculates effective permissions, and updates player permissions.
 */
public final class PermissionHandler {

    private final JavaPlugin plugin;

    /**
     * Lock for synchronizing access to the player permission attachment cache.
     */
    private final ReadWriteLock playerPermissionAttachmentCacheLock = new ReentrantReadWriteLock();

    /**
     * Cache for online player permission attachments, mapped by player UUID.
     */
    private final Map<UUID, PermissionAttachment> playerPermissionAttachmentCache = new HashMap<>();

    public PermissionHandler(@NonNull final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the {@link PermissionAttachment} for a given player UUID.
     *
     * @param playerId the UUID of the player
     * @return the {@link PermissionAttachment} if present, or {@code null} if not cached
     */
    @Locked.Read("playerPermissionAttachmentCacheLock")
    public @NonNull Optional<PermissionAttachment> getPlayerPermissionAttachment(@NonNull final UUID playerId) {
        return Optional.ofNullable(this.playerPermissionAttachmentCache.get(playerId));
    }

    /**
     * Sets the {@link PermissionAttachment} for a given player UUID.
     *
     * @param playerId   the UUID of the player
     * @param attachment the {@link PermissionAttachment} to cache
     */
    @Locked.Write("playerPermissionAttachmentCacheLock")
    public void setPlayerPermissionAttachment(
            @NonNull final UUID playerId, @NonNull final PermissionAttachment attachment) {
        this.playerPermissionAttachmentCache.put(playerId, attachment);
    }

    /**
     * Removes the permission attachment for a player from the cache.
     *
     * @param playerId the UUID of the player
     */
    @Locked.Write("playerPermissionAttachmentCacheLock")
    public void removePlayerPermissionAttachment(@NonNull final UUID playerId) {
        this.playerPermissionAttachmentCache.remove(playerId);
    }

    /**
     * Recalculates and updates permissions for a single player based on their ranks.
     *
     * @param player the player to recalculate permissions for
     * @param ranks  the list of ranks assigned to the player
     */
    public void recalculatePermissionsForPlayer(
            @NonNull final Player player,
            @NonNull final Rank ranks) {

        final PermissionAttachment permissionAttachment =
                this.getOrCreatePermissionAttachment(player);

        final Set<String> oldPermissions =
                Set.copyOf(permissionAttachment.getPermissions().keySet());

        final Object2BooleanMap<String> effectivePermissions =
                this.calculatePermissionsMap(ranks);

        // Remove old permissions
        oldPermissions.forEach(permissionAttachment::unsetPermission);

        // Apply new permissions
        effectivePermissions.forEach(permissionAttachment::setPermission);

        // Resync commands
        this.resyncPlayerCommand(player);
    }

    public void recalculatePermissionsForPlayers(
            @NonNull final Iterable<Player> players,
            @NonNull final java.util.function.Function<Player, Rank> getRankFunction) {

        for (final Player player : players) {
            final Rank rank = getRankFunction.apply(player);
            this.recalculatePermissionsForPlayer(player, rank);
        }
    }

    /**
     * Resyncs the player's command tree with the server.
     *
     * @param player the player to resync commands for
     */
    public void resyncPlayerCommand(final Player player) {
        final CraftServer craftServer = (CraftServer) Bukkit.getServer();
        final Commands dispatcher = craftServer.getHandle().getServer().getCommands();

        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        dispatcher.sendCommands(nmsPlayer);
    }

    /**
     * Calculates the effective permission map for a list of ranks.
     * Handles permission precedence based on rank priority and global vs non-global ranks.
     *
     * @param rank the rank to calculate the effective permission map for
     * @return the effective permission map for the specified ranks
     */
    public Object2BooleanMap<String> calculatePermissionsMap(@NonNull final Rank rank) {
        final Object2BooleanMap<String> effectivePermissions =
                new Object2BooleanOpenHashMap<>();

        for (final String permission : rank.getEffectivePermissions()) {
            final PermissionNodeParser nodeParser = new PermissionNodeParser(permission);

            final String sanitizedNode = nodeParser.getSanitizedNode();
            final boolean permissionValue = !nodeParser.isNegated();

            effectivePermissions.put(sanitizedNode, permissionValue);
        }

        return effectivePermissions;
    }

    /**
     * Gets or creates a permission attachment for the specified player.
     *
     * @param player the player to get or create a permission attachment for
     * @return the permission attachment for the player
     */
    private PermissionAttachment getOrCreatePermissionAttachment(@NonNull final Player player) {
        final Optional<PermissionAttachment> permissionAttachmentOpt =
                this.getPlayerPermissionAttachment(player.getUniqueId());

        if (permissionAttachmentOpt.isPresent()) {
            return permissionAttachmentOpt.get();
        } else {
            final PermissionAttachment newAttachment = player.addAttachment(this.plugin);
            this.setPlayerPermissionAttachment(player.getUniqueId(), newAttachment);
            return newAttachment;
        }
    }

    /**
     * Determines if a rank should take precedence over another rank for permission calculation.
     * Global ranks always take priority over non-global ranks.
     * If both ranks are the same type, higher priority value wins.
     *
     * @param newRank      the rank to check for precedence
     * @param existingRank the existing rank to compare against (may be null)
     * @return true if the new rank should take precedence
     */
    private boolean shouldRankTakePrecedence(@NonNull final Rank newRank, @Nullable final Rank existingRank) {
        if (existingRank == null) {
            return true;
        }

        // Both ranks are same type (global or non-global), use priority value
        return newRank.getPriority() > existingRank.getPriority();
    }

    /**
     * Clears all cached permission attachments.
     * Should be called during module teardown.
     */
    @Locked.Write("playerPermissionAttachmentCacheLock")
    public void clearCache() {
        this.playerPermissionAttachmentCache.clear();
    }
}
