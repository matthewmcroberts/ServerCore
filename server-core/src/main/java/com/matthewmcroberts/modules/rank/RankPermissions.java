package com.matthewmcroberts.modules.rank;

import com.matthewmcroberts.utils.PermissionBuilder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RankPermissions {
    public static final Permission RANK_PREFIX = PermissionBuilder.newBuilder()
            .setName("mineplex.rank")
            .setDescription("Gives the user very limited access to the rank command.")
            .build();

    public static final Permission COMMAND_NO_MODIFICATION = PermissionBuilder.newBuilder()
            .setParent(RANK_PREFIX)
            .setNameWithParentPrefix("command.nonmodification")
            .setDescription("Allows the user to access command without having access to make modifications to ranks.")
            .build();

    public static final Permission COMMAND_MODIFICATION = PermissionBuilder.newBuilder()
            .setParent(RANK_PREFIX)
            .setNameWithParentPrefix("command.modification")
            .setDescription("Allows the user to access the full command.")
            .build();

    // For safety, require deletion to not be given by default when a user has modification access
    public static final Permission COMMAND_MODIFICATION_DELETE = PermissionBuilder.newBuilder()
            .setParent(COMMAND_MODIFICATION)
            .setNameWithParentPrefix("delete")
            .setDescription("Allows the user to delete a rank.")
            .build();

    public static boolean canUseNonModificationArguments(@NonNull final CommandSender sender) {
        return sender.hasPermission(COMMAND_NO_MODIFICATION) || sender.hasPermission(COMMAND_MODIFICATION);
    }

    public static boolean canUseModificationArgument(@NonNull final CommandSender sender) {
        return sender.hasPermission(COMMAND_MODIFICATION);
    }

    public static boolean canUseDeleteArgument(@NonNull final CommandSender sender) {
        return sender.hasPermission(COMMAND_MODIFICATION_DELETE);
    }

    public static boolean canUseRootNode(@NonNull final CommandSender sender) {
        return sender.hasPermission(RANK_PREFIX)
                || sender.hasPermission(COMMAND_MODIFICATION)
                || sender.hasPermission(COMMAND_NO_MODIFICATION);
    }
}
