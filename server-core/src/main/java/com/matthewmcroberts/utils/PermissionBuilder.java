package com.matthewmcroberts.utils;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

/**
 * A builder class for creating and registering Bukkit permissions.
 */
public class PermissionBuilder {

    /**
     * Creates a new instance of the PermissionBuilder.
     *
     * @return a new PermissionBuilder instance
     */
    @NonNull public static PermissionBuilder newBuilder() {
        return new PermissionBuilder();
    }

    /**
     * The name of the permission.
     */
    private String name;
    /**
     * The description of the permission.
     */
    private String description = "";
    /**
     * The parent permission.
     */
    private Permission parent = null;
    /**
     * The default value for the permission.
     */
    private PermissionDefault permissionDefault = PermissionDefault.OP;

    /**
     * Sets the name of the permission using a prefix and a name.
     *
     * @param prefix the prefix for the permission name
     * @param name the name of the permission
     * @return the current PermissionBuilder instance
     */
    @NonNull public PermissionBuilder setName(@NonNull final Permission prefix, @NonNull final String name) {
        return this.setName(String.join(".", prefix.getName(), name));
    }

    /**
     * Sets the name of the permission.
     *
     * @param name the name of the permission
     * @return the current PermissionBuilder instance
     */
    @NonNull public PermissionBuilder setName(@NonNull final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description of the permission.
     *
     * @param description the description of the permission
     * @return the current PermissionBuilder instance
     */
    @NonNull public PermissionBuilder setDescription(@NonNull final String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the name of the permission using the parent's prefix.
     *
     * @param name the name of the permission
     * @return the current PermissionBuilder instance
     * @throws NullPointerException if the parent permission is null
     */
    @NonNull public PermissionBuilder setNameWithParentPrefix(@NonNull final String name) {
        Preconditions.checkNotNull(this.parent, "Parent permission cannot be null");
        return this.setName(this.parent, name);
    }

    /**
     * Sets the parent permission.
     *
     * @param parent the parent permission
     * @return the current PermissionBuilder instance
     */
    @NonNull public PermissionBuilder setParent(@Nullable final Permission parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Sets the default value for the permission.
     *
     * @param permissionDefault the default value for the permission
     * @return the current PermissionBuilder instance
     */
    @NonNull public PermissionBuilder setPermissionDefault(@NonNull final PermissionDefault permissionDefault) {
        this.permissionDefault = permissionDefault;
        return this;
    }

    /**
     * Builds and registers the permission.
     *
     * @return the created Permission instance
     * @throws NullPointerException if the permission name is null
     */
    @NonNull public Permission build() {
        Preconditions.checkNotNull(this.name, "Permission name cannot be null");

        if (this.parent == null) {
            return DefaultPermissions.registerPermission(this.name, this.description, this.permissionDefault);
        } else {
            return DefaultPermissions.registerPermission(
                    this.name, this.description, this.permissionDefault, this.parent);
        }
    }
}
