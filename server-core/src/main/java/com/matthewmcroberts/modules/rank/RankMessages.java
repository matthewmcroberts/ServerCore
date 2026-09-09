package com.matthewmcroberts.modules.rank;

import com.matthewmcroberts.utils.messages.ServerMessageComponent;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RankMessages extends ServerMessageComponent {

    public static final Component PREFIX = Component.text("Rank");

    public static @NonNull Component withRankPrefix(@NonNull final Component component) {
        return main(PREFIX, component);
    }

    public static @NonNull Component createCommandHelpMessage(
            @NonNull final String command,
            @NonNull final Component description,
            @NonNull final TextColor commandColor) {

        return Component.text(command, commandColor)
                .append(Component.text(" - ").color(NamedTextColor.GRAY))
                .append(description.color(NamedTextColor.GRAY));
    }

    public static final class Error {

        public static final Args1<Component> RANK_ID_NOT_FOUND = rankId ->
                withRankPrefix(
                        Component.text("No rank with the id ")
                                .append(rankId)
                                .append(Component.text(" was found."))
                );

        public static final Args0 INVALID_PLAYER_ID =
                () -> withRankPrefix(Component.text("Invalid UUID format."));

        public static final Args0 RANK_NOT_ASSIGNED =
                () -> withRankPrefix(
                        Component.text("The player has no rank assigned.")
                                .color(NamedTextColor.GRAY)
                );

        public static final class Priority {

            private static final Args1<Component> RESTRICTED = reason ->
                    withRankPrefix(
                            Component.text("You do not have permission to ")
                                    .append(reason)
                                    .append(Component.text(" this rank."))
                                    .color(NamedTextColor.GRAY)
                    );

            public static final Args0 RESTRICTED_ASSIGN =
                    () -> RESTRICTED.apply(Action.ASSIGN.apply());

            public static final Args0 RESTRICTED_UNASSIGN =
                    () -> RESTRICTED.apply(Action.UNASSIGN.apply());

            private static final class Action {

                public static final Args0 ASSIGN =
                        () -> Component.text("assign");

                public static final Args0 UNASSIGN =
                        () -> Component.text("unassign");
            }
        }

        public static final class Permissions {

            public static final Args1<Component> FAILED_UPDATE = reason ->
                    withRankPrefix(
                            Component.text("Failed to update rank permissions: ")
                                    .append(reason)
                                    .color(NamedTextColor.GRAY)
                    );

            public static final class Reason {

                public static final Args0 ALREADY_EXISTS =
                        () -> Component.text("permission node already exists");

                public static final Args0 NOT_FOUND =
                        () -> Component.text("permission node not found");
            }
        }

        public static final class Inheritance {

            public static final Args1<Component> FAILED_UPDATE = reason ->
                    withRankPrefix(
                            Component.text("Failed to update rank inheritances: ")
                                    .append(reason)
                                    .append(Component.text("."))
                                    .color(NamedTextColor.GRAY)
                    );

            public static final class Reason {

                public static final Args0 ALREADY_EXISTS =
                        () -> Component.text("inheritance already exists");

                public static final Args0 NOT_FOUND =
                        () -> Component.text("inheritance not found");

                public static final Args0 PRIORITY =
                        () -> Component.text(
                                "you cannot inherit from a rank with an equal or higher priority"
                        );

                public static final Args0 CIRCULAR =
                        () -> Component.text("circular inheritance detected");
            }
        }

        public static final class Exception {

            public static final class RankCreate {

                public static final Args1<Component> FAILED_CREATE = reason ->
                        withRankPrefix(
                                Component.text("Failed to create rank: ")
                                        .append(element(reason))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );

                public static final class Reason {

                    public static final Args0 ID_ALREADY_EXISTS =
                            () -> Component.text("rank id already exists");

                    public static final Args0 MAX_RANKS_ALLOWED =
                            () -> Component.text("maximum number of ranks allowed reached");
                }
            }

            public static final class RankUpdate {

                public static final Args1<Component> FAILED_UPDATE = reason ->
                        withRankPrefix(
                                Component.text("Failed to update rank: ")
                                        .append(element(reason))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );
            }

            public static final class Reason {

                public static final Args0 INVALID_NAME =
                        () -> Component.text("invalid name");

                public static final Args0 INVALID_PRIORITY =
                        () -> Component.text("priority must be a positive integer");
            }
        }
    }

    public static final class Command {

        public static final Args0 LIST_EMPTY =
                () -> withRankPrefix(
                        Component.text("No ranks found.")
                                .color(NamedTextColor.GRAY)
                );

        public static final Args2<Component, Component> LIST_ELEMENT = (category, value) -> {
            final Component processedValue =
                    value.hasDecoration(TextDecoration.BOLD)
                            ? value
                            : value.decoration(TextDecoration.BOLD, false);

            return Component.text("  • ")
                    .append(
                            category
                                    .color(NamedTextColor.GRAY)
                                    .decoration(TextDecoration.BOLD, false)
                    )
                    .append(Component.text(": "))
                    .append(element(processedValue))
                    .color(NamedTextColor.GRAY);
        };

        public static final Args2<Component, Component> CREATE =
                (rankName, command) ->
                        withRankPrefix(
                                Component.text("Successfully created ")
                                        .append(element(rankName))
                                        .append(Component.text(" rank. Use "))
                                        .append(element(command))
                                        .append(Component.text(" to view options for editing the rank."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args1<Component> DELETE =
                rankName ->
                        withRankPrefix(
                                Component.text("Successfully deleted ")
                                        .append(element(rankName))
                                        .append(Component.text(
                                                " rank. All players with this rank have been unassigned."
                                        ))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> PERMISSION_ADD =
                (permission, rankName) ->
                        withRankPrefix(
                                Component.text("Successfully added permission node ")
                                        .append(element(permission))
                                        .append(Component.text(" to "))
                                        .append(element(rankName))
                                        .append(Component.text(" rank."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> PERMISSION_REMOVE =
                (permission, rankName) ->
                        withRankPrefix(
                                Component.text("Successfully removed permission node ")
                                        .append(element(permission))
                                        .append(Component.text(" from "))
                                        .append(element(rankName))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> INHERIT_ADD =
                (inheritance, rankName) ->
                        withRankPrefix(
                                Component.text("Successfully added inheritance ")
                                        .append(element(inheritance))
                                        .append(Component.text(" to "))
                                        .append(element(rankName))
                                        .append(Component.text(" rank."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> INHERIT_REMOVE =
                (inheritance, rankName) ->
                        withRankPrefix(
                                Component.text("Successfully removed inheritance ")
                                        .append(element(inheritance))
                                        .append(Component.text(" from "))
                                        .append(element(rankName))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> PRIORITY =
                (rankName, priority) ->
                        withRankPrefix(
                                Component.text("Successfully set the priority of ")
                                        .append(element(rankName))
                                        .append(Component.text(" to "))
                                        .append(element(priority))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final Args2<Component, Component> DISPLAYNAME =
                (rankName, displayName) ->
                        withRankPrefix(
                                Component.text("Successfully set the display name of ")
                                        .append(element(rankName))
                                        .append(Component.text(" to "))
                                        .append(element(displayName))
                                        .append(Component.text("."))
                                        .color(NamedTextColor.GRAY)
                        );

        public static final class Assign {

            public static final Args2<Component, Component> SENDER =
                    (rankName, playerName) ->
                            withRankPrefix(
                                    Component.text("Successfully assigned ")
                                            .append(element(rankName))
                                            .append(Component.text(" rank to "))
                                            .append(element(playerName))
                                            .append(Component.text("."))
                            );

            public static final Args1<Component> TARGET =
                    rankName ->
                            withRankPrefix(
                                    Component.text("Your rank has been updated to ")
                                            .append(element(rankName))
                                            .append(Component.text("."))
                            );
        }

        public static final class Unassign {

            public static final Args2<Component, Component> SENDER =
                    (rankName, playerName) ->
                            withRankPrefix(
                                    Component.text("Successfully unassigned ")
                                            .append(element(rankName))
                                            .append(Component.text(" rank from "))
                                            .append(element(playerName))
                                            .append(Component.text("."))
                            );

            public static final Args1<Component> SENDER_OFFLINE =
                    playerId ->
                            withRankPrefix(
                                    Component.text("Successfully unassigned rank for player id ")
                                            .append(element(playerId))
                                            .append(Component.text("."))
                            );

            public static final Args1<Component> TARGET =
                    rankName ->
                            withRankPrefix(
                                    Component.text("Your ")
                                            .append(element(rankName))
                                            .append(Component.text(" rank has been removed."))
                            );
        }

        public static final class Help {

            public static final Args0 TITLE =
                    () -> Component.text("Rank Commands");

            public static final Args0 HELP =
                    () -> Component.text("Shows the current menu.");

            public static final Args0 INFO =
                    () -> Component.text("Shows info about a specified rank.");

            public static final Args0 LIST =
                    () -> Component.text(
                            "List all loaded ranks."
                    );

            public static final Args0 GET =
                    () -> Component.text(
                            "Get the rank assigned to the specified player."
                    );

            public static final Args0 CREATE =
                    () -> Component.text(
                            "Create a new rank. "
                                    + "Example display name (Color is just a placeholder): <color>ADMIN</color>"
                    );

            public static final Args0 DELETE =
                    () -> Component.text(
                            "Delete a specified rank."
                    );

            public static final Args0 PERMISSION_ADD =
                    () -> Component.text(
                            "Adds a permission node to the specified rank. Prefix a node with '-' to negate it."
                    );

            public static final Args0 PERMISSION_REMOVE =
                    () -> Component.text(
                            "Removes a permission node from the specified rank."
                    );

            public static final Args0 INHERIT_ADD =
                    () -> Component.text(
                            "Adds an inheritance to the specified rank. Ranks can have multiple inheritances."
                    );

            public static final Args0 INHERIT_REMOVE =
                    () -> Component.text(
                            "Removes an inheritance from the specified rank."
                    );

            public static final Args0 PRIORITY =
                    () -> Component.text(
                            "Set the priority of the specified rank. "
                                    + "Larger integers indicate a higher priority (Positive integers only)."
                    );

            public static final Args0 DISPLAYNAME =
                    () -> Component.text(
                            "Set the display name of the specified rank. "
                                    + "Example (Color is just a placeholder): <color>ADMIN</color>"
                    );

            public static final Args0 ASSIGN =
                    () -> Component.text(
                            "Assigns the rank to the specified player. Player must be online."
                    );

            public static final Args0 UNASSIGN =
                    () -> Component.text(
                            "Unassigns the rank from the specified player."
                    );

            public static final Args0 UNASSIGN_ID =
                    () -> Component.text(
                            "Unassigns the rank from the player with the specified UUID. "
                                    + "This is useful for removing offline player ranks."
                    );
        }

        public static final class Info {

            public static final Args0 HEADER =
                    () -> Component.text("Rank Info")
                            .color(NamedTextColor.BLUE)
                            .decorate(TextDecoration.BOLD);
        }

        public static final class Get {

            public static final Args1<Component> HEADER = playerName ->
                    playerName
                            .color(NamedTextColor.BLUE)
                            .decorate(TextDecoration.BOLD)
                            .append(
                                    Component.text(" Rank Info")
                                            .color(NamedTextColor.BLUE)
                                            .decorate(TextDecoration.BOLD)
                            );
        }
    }
}