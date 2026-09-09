package com.matthewmcroberts.modules.rank.commands;

import com.matthewmcroberts.modules.rank.RankMessages;
import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.RankPermissions;
import com.matthewmcroberts.modules.rank.client.dto.PlayerRankAssignment;
import com.matthewmcroberts.modules.rank.commands.arguments.RankByIdArgumentType;
import com.matthewmcroberts.modules.rank.models.PermissionNode;
import com.matthewmcroberts.modules.rank.models.Rank;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.adventure.AdventureComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public final class RankCommand {
    public static final String COMMAND_NAME = "rank";

    private static final String CREATE_LITERAL = "create";
    private static final String DELETE_LITERAL = "delete";
    private static final String INFO_LITERAL = "info";
    private static final String LIST_LITERAL = "list";
    private static final String PERMISSION_LITERAL = "permission";
    private static final String INHERIT_LITERAL = "inherit";
    private static final String PRIORITY_LITERAL = "priority";
    private static final String DISPLAYNAME_LITERAL = "displayname";
    private static final String ASSIGN_LITERAL = "assign";
    private static final String UNASSIGN_LITERAL = "unassign";
    private static final String GET_LITERAL = "get";
    private static final String HELP_LITERAL = "help";

    private static final String ADD_SUB_LITERAL = "add";
    private static final String REMOVE_SUB_LITERAL = "remove";

    private static final String RANK_ID_ARGUMENT = "rankId";
    private static final String DISPLAY_NAME_ARGUMENT = "displayName";
    private static final String PRIORITY_ARGUMENT = "priority";
    private static final String PERMISSION_ARGUMENT = "permission";
    private static final String PLAYER_ARGUMENT = "player";
    private static final String INHERITED_RANK_ID_ARGUMENT = "inheritedRankId";

    private final WeakReference<RankModule> rankModuleReference;

    private @NonNull RankModule getRankModule() {
        return Objects.requireNonNull(this.rankModuleReference.get(), "RankModule is no longer available.");
    }

    /**
     * Gets the description of the command.
     *
     * @return the description of the command
     */
    public @Nullable String getDescription() {
        return "";
    }

    /**
     * Get the aliases of the command.
     *
     * @return the aliases of the command
     */
    public @NonNull Collection<String> getAliases() {
        return List.of();
    }

    /**
     * Builds the {@link LiteralCommandNode} for the command.
     *
     * @return the built {@link LiteralCommandNode}
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> buildNode() {
        return Commands.literal(COMMAND_NAME)
                .requires(source -> RankPermissions.canUseRootNode(source.getSender()))
                .executes(context -> {
                    this.handleHelp(context);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal(HELP_LITERAL).executes(context -> {
                    this.handleHelp(context);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal(INFO_LITERAL)
                        .then(Commands.argument(RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                .suggests(this::rankIdSuggestions)
                                .executes(context -> {
                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                    this.handleInfo(context, rank);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal(LIST_LITERAL).executes(context -> {
                    this.handleList(context);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal(CREATE_LITERAL)
                        .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                        .then(Commands.argument(RANK_ID_ARGUMENT, StringArgumentType.word())
                                .then(Commands.argument(DISPLAY_NAME_ARGUMENT, StringArgumentType.greedyString())
                                        .executes(context -> {
                                            final String rankId = context.getArgument(RANK_ID_ARGUMENT, String.class);
                                            final String displayName =
                                                    context.getArgument(DISPLAY_NAME_ARGUMENT, String.class);
                                            this.handleCreate(context, rankId, displayName);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal(DELETE_LITERAL)
                        .requires(source -> RankPermissions.canUseDeleteArgument(source.getSender()))
                        .then(Commands.argument(RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                .suggests(this::rankIdSuggestions)
                                .executes(context -> {
                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                    this.handleDelete(context, rank);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal(PERMISSION_LITERAL)
                        .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                        .then(Commands.literal(ADD_SUB_LITERAL)
                                .then(Commands.argument(
                                                RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                        .suggests(this::rankIdSuggestions)
                                        .then(Commands.argument(PERMISSION_ARGUMENT, StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                                    final String permissionNode =
                                                            context.getArgument(PERMISSION_ARGUMENT, String.class);
                                                    this.handlePermissionAdd(context, rank, permissionNode);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
                        .then(Commands.literal(REMOVE_SUB_LITERAL)
                                .then(Commands.argument(
                                                RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                        .suggests(this::rankIdSuggestions)
                                        .then(Commands.argument(PERMISSION_ARGUMENT, StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                                    final String permissionNode =
                                                            context.getArgument(PERMISSION_ARGUMENT, String.class);
                                                    this.handlePermissionRemove(context, rank, permissionNode);
                                                    return Command.SINGLE_SUCCESS;
                                                })))))
                .then(Commands.literal(INHERIT_LITERAL)
                        .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                        .then(Commands.literal(ADD_SUB_LITERAL)
                                .then(Commands.argument(
                                                RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                        .suggests(this::rankIdSuggestions)
                                        .then(Commands.argument(
                                                        INHERITED_RANK_ID_ARGUMENT,
                                                        new RankByIdArgumentType(this.rankModuleReference))
                                                .suggests(this::rankIdSuggestions)
                                                .executes(context -> {
                                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                                    final Rank inheritedRank =
                                                            context.getArgument(INHERITED_RANK_ID_ARGUMENT, Rank.class);
                                                    this.handleInheritAdd(context, rank, inheritedRank);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))
                        .then(Commands.literal(REMOVE_SUB_LITERAL)
                                .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                                .then(Commands.argument(
                                                RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                        .suggests(this::rankIdSuggestions)
                                        .then(Commands.argument(
                                                        INHERITED_RANK_ID_ARGUMENT,
                                                        new RankByIdArgumentType(this.rankModuleReference))
                                                .suggests(this::rankIdSuggestions)
                                                .executes(context -> {
                                                    final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                                    final Rank inheritedRank =
                                                            context.getArgument(INHERITED_RANK_ID_ARGUMENT, Rank.class);
                                                    this.handleInheritRemove(context, rank, inheritedRank);
                                                    return Command.SINGLE_SUCCESS;
                                                })))))
                .then(Commands.literal(PRIORITY_LITERAL)
                        .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                        .then(Commands.argument(RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                .suggests(this::rankIdSuggestions)
                                .then(Commands.argument(PRIORITY_ARGUMENT, IntegerArgumentType.integer())
                                        .executes(context -> {
                                            final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                            final int priority = context.getArgument(PRIORITY_ARGUMENT, Integer.class);
                                            this.handlePriority(context, rank, priority);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal(DISPLAYNAME_LITERAL)
                        .requires(source -> RankPermissions.canUseModificationArgument(source.getSender()))
                        .then(Commands.argument(RANK_ID_ARGUMENT, new RankByIdArgumentType(this.rankModuleReference))
                                .suggests(this::rankIdSuggestions)
                                .then(Commands.argument(DISPLAY_NAME_ARGUMENT, StringArgumentType.greedyString())
                                        .executes(context -> {
                                            final Rank rank = context.getArgument(RANK_ID_ARGUMENT, Rank.class);
                                            final String displayName =
                                                    context.getArgument(DISPLAY_NAME_ARGUMENT, String.class);
                                            this.handleDisplayName(context, rank, displayName);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal(ASSIGN_LITERAL)
                        .requires(source -> RankPermissions.canUseNonModificationArguments(source.getSender()))
                        .then(Commands.argument(
                                        PLAYER_ARGUMENT,
                                        ArgumentTypes.player())
                                .then(Commands.argument(
                                                RANK_ID_ARGUMENT,
                                                new RankByIdArgumentType(this.rankModuleReference))
                                        .suggests(this::rankIdSuggestions)
                                        .executes(context -> {
                                            final Rank rank =
                                                    context.getArgument(RANK_ID_ARGUMENT, Rank.class);

                                            final PlayerSelectorArgumentResolver resolver =
                                                    context.getArgument(
                                                            PLAYER_ARGUMENT,
                                                            PlayerSelectorArgumentResolver.class
                                                    );

                                            final Player targetPlayer = resolver.resolve(context.getSource())
                                                    .stream()
                                                    .findFirst()
                                                    .orElse(null);


                                            this.handleAssign(context, rank, targetPlayer);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal(UNASSIGN_LITERAL)
                        .requires(source -> RankPermissions.canUseNonModificationArguments(source.getSender()))
                        .then(Commands.argument(PLAYER_ARGUMENT, StringArgumentType.word())
                                .suggests(this::playerNameSuggestions)
                                .executes(context -> {
                                    final String rawPlayerString = context.getArgument(PLAYER_ARGUMENT, String.class);
                                    try {
                                        final UUID playerId = UUID.fromString(rawPlayerString);
                                        this.handleUnassignById(context, playerId);
                                    } catch (IllegalArgumentException e) {
                                        this.handleUnassign(context, rawPlayerString);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal(GET_LITERAL)
                        .then(Commands.argument(PLAYER_ARGUMENT, ArgumentTypes.player())
                                .executes(context -> {
                                    final PlayerSelectorArgumentResolver resolver =
                                            context.getArgument(
                                                    PLAYER_ARGUMENT,
                                                    PlayerSelectorArgumentResolver.class
                                            );

                                    final Player targetPlayer = resolver.resolve(context.getSource())
                                            .stream()
                                            .findFirst()
                                            .orElse(null);

                                    if (targetPlayer == null) {
                                        context.getSource().getSender().sendMessage(
                                                Component.text("Player not found.")
                                        );
                                        return 0;
                                    }

                                    this.handleGet(context, targetPlayer);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
    }

    @NonNull private Optional<Player> getPlayerSender(@NonNull final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof final Player player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    @NonNull private Player playerSenderOrThrow(@NonNull final CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return this.getPlayerSender(context).orElseThrow(() -> new SimpleCommandExceptionType(
                new AdventureComponent(Component.text("Not Player")))
                .create());
    }

    protected void handleException(
            @NonNull final CommandContext<CommandSourceStack> context, @NonNull final Throwable exception) {
        final Optional<Player> playerOpt = this.getPlayerSender(context);
        playerOpt.ifPresent(player -> player.sendMessage(Component.text("Error: ")
                .append(Component.text(exception.getMessage()))
                .color(NamedTextColor.RED)));
    }

    private void handleInfo(@NonNull final CommandContext<CommandSourceStack> context, @NonNull final Rank rank)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);

        final Component message = Component.join(
                JoinConfiguration.newlines(),
                RankMessages.Command.Info.HEADER.apply(),
                RankMessages.Command.LIST_ELEMENT.apply(Component.text("Id"), Component.text(rank.getRankId())),
                RankMessages.Command.LIST_ELEMENT.apply(Component.text("Display Name"), rank.getRenderedDisplayName()),
                RankMessages.Command.LIST_ELEMENT.apply(Component.text("Priority"), Component.text(rank.getPriority())),
                RankMessages.Command.LIST_ELEMENT.apply(
                        Component.text("Inherited Id(s)"),
                        Component.text(rank.getInheritedRankIds().toString())),
                RankMessages.Command.LIST_ELEMENT.apply(
                        Component.text("Own Permissions"),
                        Component.text(rank.getOwnPermissions().toString())),
                RankMessages.Command.LIST_ELEMENT.apply(
                        Component.text("Effective Permissions"),
                        Component.text(rank.getEffectivePermissions().toString())));

        sender.sendMessage(message);
    }

    private void handleList(@NonNull final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);

        final List<Rank> allRanks = getRankModule().getAllRanks();
        final List<Component> rankComponents = allRanks.stream()
                .map(rank ->
                        RankMessages.Command.LIST_ELEMENT.apply(Component.text("Rank"), rank.getRenderedDisplayName()))
                .toList();

        final Component message = RankMessages.Command.Info.HEADER
                .apply()
                .appendNewline()
                .append(Component.join(JoinConfiguration.newlines(), rankComponents));

        sender.sendMessage(message);
    }

    private void handleCreate(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final String rankId,
            @NonNull final String displayName)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        this.getRankModule()
                .createRank(rankId, displayName, 1, Set.of(), Set.of(), Set.of())
                .whenComplete((rank, throwable) -> {
                    if (throwable != null) {
                        this.handleException(context, throwable);
                        return;
                    }
                    RankMessages.Command.CREATE.send(
                            sender,
                            Component.text(rank.getRankId()),
                            Component.text("/" + COMMAND_NAME + " " + HELP_LITERAL));
                });
    }

    private void handleDelete(@NonNull final CommandContext<CommandSourceStack> context, @NonNull final Rank rank)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        this.getRankModule().getRankClient().deleteRank(rank.getRankId()).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.DELETE.send(sender, Component.text(rank.getRankId()));
        });
    }

    private void handlePermissionAdd(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final String permissionNode)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        if (rank.getOwnPermissions().contains(permissionNode)) {
            RankMessages.Error.Permissions.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Permissions.Reason.ALREADY_EXISTS.apply());
            return;
        }

        rank.addPermissions(Set.of(PermissionNode.of(permissionNode))).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.PERMISSION_ADD.send(
                    sender, Component.text(permissionNode), Component.text(rank.getRankId()));
        });
    }

    private void handlePermissionRemove(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final String permissionNode)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        if (!rank.getOwnPermissions().contains(permissionNode)) {
            RankMessages.Error.Permissions.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Permissions.Reason.NOT_FOUND.apply());
            return;
        }

        rank.removePermissions(Set.of(PermissionNode.of(permissionNode)))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        this.handleException(context, throwable);
                        return;
                    }
                    RankMessages.Command.PERMISSION_REMOVE.send(
                            sender, Component.text(permissionNode), Component.text(rank.getRankId()));
                });
    }

    private void handleInheritAdd(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final Rank inheritedRank)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        if (rank.getInheritedRankIds().contains(inheritedRank.getRankId())) {
            RankMessages.Error.Inheritance.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Inheritance.Reason.ALREADY_EXISTS.apply());
            return;
        }

        if (rank.getRankId().equals(inheritedRank.getRankId())) {
            RankMessages.Error.Inheritance.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Inheritance.Reason.CIRCULAR.apply());
            return;
        }

        if (rank.getPriority() <= inheritedRank.getPriority()) {
            RankMessages.Error.Inheritance.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Inheritance.Reason.PRIORITY.apply());
            return;
        }

        rank.addInheritedRankIds(Set.of(inheritedRank.getRankId())).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.INHERIT_ADD.send(
                    sender, Component.text(inheritedRank.getRankId()), Component.text(rank.getRankId()));
        });
    }

    private void handleInheritRemove(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final Rank inheritedRank)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        if (!rank.getInheritedRankIds().contains(inheritedRank.getRankId())) {
            RankMessages.Error.Inheritance.FAILED_UPDATE.send(
                    sender, RankMessages.Error.Inheritance.Reason.NOT_FOUND.apply());
            return;
        }

        rank.removeInheritedRankIds(Set.of(inheritedRank.getRankId())).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.INHERIT_REMOVE.send(
                    sender, Component.text(inheritedRank.getRankId()), Component.text(rank.getRankId()));
        });
    }

    private void handlePriority(
            @NonNull final CommandContext<CommandSourceStack> context, @NonNull final Rank rank, final int priority)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        rank.setPriority(priority).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.PRIORITY.send(sender, Component.text(rank.getRankId()), Component.text(priority));
        });
    }

    private void handleDisplayName(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final String displayName)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        rank.setDisplayName(displayName).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }
            RankMessages.Command.DISPLAYNAME.send(
                    sender, Component.text(rank.getRankId()), Component.text(displayName));
        });
    }

    private void handleAssign(
            @NonNull final CommandContext<CommandSourceStack> context,
            @NonNull final Rank rank,
            @NonNull final Player targetPlayer)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);

        if (!this.hasPermissionToModifyPlayer(sender, targetPlayer, rank)) {
            RankMessages.Error.Priority.RESTRICTED_ASSIGN.send(sender);
            return;
        }

        rank.assignPlayer(targetPlayer.getUniqueId(), sender.getUniqueId()).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }

            RankMessages.Command.Assign.SENDER.send(
                    sender, Component.text(rank.getRankId()), Component.text(targetPlayer.getName()));
        });
    }

    private void handleUnassign(
            @NonNull final CommandContext<CommandSourceStack> context, @NonNull final String rawPlayerName)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        final Player targetPlayer = Bukkit.getPlayer(rawPlayerName);
        if (targetPlayer == null) {
            //TODO
            sender.sendMessage(Component.text("Offline player").color(NamedTextColor.RED));
            return;
        }

        final UUID targetId = targetPlayer.getUniqueId();
        final Optional<Rank> targetRankOpt = this.getRankModule().getRankForOnlinePlayer(targetPlayer);
        if (targetRankOpt.isEmpty()) {
            RankMessages.Error.RANK_NOT_ASSIGNED.send(sender);
            return;
        }

        final Rank targetRank = targetRankOpt.get();

        if (!this.hasPermissionToModifyPlayer(sender, targetPlayer, targetRank)) {
            RankMessages.Error.Priority.RESTRICTED_UNASSIGN.send(sender);
            return;
        }

        targetRank.unAssignPlayer(targetId).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.handleException(context, throwable);
                return;
            }

            RankMessages.Command.Unassign.SENDER.send(
                    sender, Component.text(targetRank.getRankId()), Component.text(targetPlayer.getName()));
        });
    }

    private void handleUnassignById(
            @NonNull final CommandContext<CommandSourceStack> context, @NonNull final UUID playerId)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);
        try {
            this.getRankModule()
                    .getOfflinePlayerRankAssignment(playerId)
                    .thenCompose(assignment -> {
                        final String rankId = assignment.getRankId();
                        return this.getRankModule()
                                .getRankById(rankId)
                                .map(rank -> {
                                    if (!this.hasPermissionToModifyPlayer(sender, Objects.requireNonNull(Bukkit.getPlayer(playerId)), rank)) {
                                        RankMessages.Error.Priority.RESTRICTED_UNASSIGN.send(sender);
                                        return CompletableFuture.<Void>completedFuture(null);
                                    }
                                    return rank.unAssignPlayer(playerId);
                                })
                                .orElseThrow(() -> new RuntimeException("Rank Id not found"));
                    })
                    .whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            this.handleException(context, throwable);
                        }
                    });
        } catch (final IllegalArgumentException e) {
            RankMessages.Error.INVALID_PLAYER_ID.send(sender);
        }
    }

    private void handleGet(
            @NonNull final CommandContext<CommandSourceStack> context, @NonNull final Player targetPlayer)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);

        final UUID targetId = targetPlayer.getUniqueId();
        Optional<PlayerRankAssignment> playerRankAssignmentOpt = this.getRankModule().getOnlinePlayerRankAssignment(targetId);
        if (playerRankAssignmentOpt.isEmpty()) {
            // We can assume the error is coming from the batcher, meaning the player doesn't have a rank assigned.
            RankMessages.Error.RANK_NOT_ASSIGNED.send(sender);
            log.info("Failed to fetch player rank assignment. This likely means the player has no rank assigned.");
            return;
        }

        final PlayerRankAssignment assignment = playerRankAssignmentOpt.get();
        final Optional<Rank> rankOpt = this.getRankModule().getRankById(assignment.getRankId());
        if (rankOpt.isEmpty()) {
            log.error(
                    "Expected rank to be present but wasn't: {}",
                    assignment.getRankId());
            sender.sendMessage(Component.text("An error occurred").color(NamedTextColor.RED));
            return;
        }

        final Rank rank = rankOpt.get();
        final AtomicReference<Component> message = new AtomicReference<>(Component.join(
                JoinConfiguration.newlines(),
                RankMessages.Command.Get.HEADER.apply(Component.text(targetPlayer.getName() + "'s")),
                RankMessages.Command.LIST_ELEMENT.apply(Component.text("Rank"), rank.getRenderedDisplayName())));


        this.addPermissionsAndSendMessage(sender, targetId, message.get());
    }

    private void addPermissionsAndSendMessage(
            @NonNull final Player sender, @NonNull final UUID targetId, @NonNull final Component oldMessage) {
        Component message = oldMessage;
        if (Bukkit.getPlayer(targetId) != null) {
            final Optional<PermissionAttachment> attachmentOpt =
                    this.getRankModule().getPlayerPermissionAttachment(targetId);
            if (attachmentOpt.isPresent()) {
                final PermissionAttachment attachment = attachmentOpt.get();
                message = message.appendNewline()
                        .append(RankMessages.Command.LIST_ELEMENT.apply(
                                Component.text("Permissions"),
                                Component.text(
                                        attachment.getPermissions().entrySet().toString())));
            } else {
                message = message.appendNewline()
                        .append(RankMessages.Command.LIST_ELEMENT.apply(
                                Component.text("Permissions"),
                                Component.text("[]").color(NamedTextColor.YELLOW)));

                sender.sendMessage(message);
                log.error(
                        "Expected permission attachment to be present but wasn't: {}. It's possible the rank the player is assigned to has no permissions.",
                        targetId);
                return;
            }
        }

        sender.sendMessage(message);
    }

    private void handleHelp(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(context);

        final Component message = Component.join(
                JoinConfiguration.newlines(),
                RankMessages.Command.Help.TITLE
                        .apply()
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.BOLD),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " help", RankMessages.Command.Help.HELP.apply(), NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " info <rankId>",
                        RankMessages.Command.Help.INFO.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " list", RankMessages.Command.Help.LIST.apply(), NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " get <player>",
                        RankMessages.Command.Help.GET.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " create <rankId> <displayName>",
                        RankMessages.Command.Help.CREATE.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " delete <rankId>",
                        RankMessages.Command.Help.DELETE.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " permission add <rankId> <node>",
                        RankMessages.Command.Help.PERMISSION_ADD.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " permission remove <rankId> <node>",
                        RankMessages.Command.Help.PERMISSION_REMOVE.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " inherit add <rankId> <inheritedRankId>",
                        RankMessages.Command.Help.INHERIT_ADD.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " inherit remove <rankId> <inheritedRankId>",
                        RankMessages.Command.Help.INHERIT_REMOVE.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " priority <rankId> <priority>",
                        RankMessages.Command.Help.PRIORITY.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " displayname <rankId> <displayName>",
                        RankMessages.Command.Help.DISPLAYNAME.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " assign <player> <rankId>",
                        RankMessages.Command.Help.ASSIGN.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " unassign <player>",
                        RankMessages.Command.Help.UNASSIGN.apply(),
                        NamedTextColor.YELLOW),
                RankMessages.createCommandHelpMessage(
                        "/" + COMMAND_NAME + " unassign <playerId>",
                        RankMessages.Command.Help.UNASSIGN_ID.apply(),
                        NamedTextColor.YELLOW));
        sender.sendMessage(message);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasPermissionToModifyPlayer(
            @NonNull final Player sender, @NonNull final Player targetPlayer, @NonNull final Rank targetRank) {
        if (RankPermissions.canUseModificationArgument(sender)) {
            return true;
        }

        if (sender.getUniqueId().equals(targetPlayer.getUniqueId())) {
            return false;
        }

        final Optional<Rank> senderRankOpt = this.getRankModule().getRankForOnlinePlayer(sender);

        if (senderRankOpt.isEmpty()) {
            return false;
        }

        final Rank senderRank = senderRankOpt.get();

        if (senderRank.getPriority() <= targetRank.getPriority()) {
            return false;
        }

        final Optional<Rank> currentTargetRankOpt = this.getRankModule().getRankForOnlinePlayer(targetPlayer);
        if (currentTargetRankOpt.isPresent()) {
            final Rank currentTargetRank = currentTargetRankOpt.get();
            return currentTargetRank.getPriority() <= senderRank.getPriority();
        }

        return true;
    }

    private CompletableFuture<Suggestions> rankIdSuggestions(
            @NonNull final CommandContext<CommandSourceStack> ctx, @NonNull final SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();

        this.getRankModule().getAllRanks().stream()
                .map(Rank::getRankId)
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> playerNameSuggestions(
            @NonNull final CommandContext<CommandSourceStack> ctx, @NonNull final SuggestionsBuilder builder)
            throws CommandSyntaxException {
        final Player sender = this.playerSenderOrThrow(ctx);
        final String remaining = builder.getRemainingLowerCase();

        Bukkit.getOnlinePlayers().stream()
                .filter(sender::canSee)
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
