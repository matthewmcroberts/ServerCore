package com.matthewmcroberts.utils.messages;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Utility class for creating Mineplex styled messages.
 *
 * @see MessageComponent
 */
public abstract class ServerMessageComponent extends MessageComponent {
    /**
     * The prefix for game messages.
     */
    @NonNull protected static final Component GAME_PREFIX = Component.translatable("mineplex.engine.game");
    /**
     * The prefix for command messages.
     */
    @NonNull protected static final Component COMMAND_PREFIX = Component.translatable("mineplex.engine.command");

    /**
     * Prefix a component with the game prefix and style the component if no styles are present.
     *
     * @param component the component to prefix
     * @return the prefixed component
     */
    @NonNull protected static Component withGamePrefix(@NonNull final ComponentLike component) {
        return main(GAME_PREFIX, component);
    }

    /**
     * Prefix a component with the command prefix and style the component if no styles are present.
     *
     * @param component the component to prefix
     * @return the prefixed component
     */
    @NonNull protected static Component withCommandPrefix(@NonNull final ComponentLike component) {
        return main(COMMAND_PREFIX, component);
    }

    /**
     * Change the colour of a component if it is empty.
     *
     * @param component the component to change
     * @param colour    the colour to change to
     * @return the changed component
     */
    @NonNull protected static Component changeColourIfEmpty(
            @NonNull final ComponentLike component, @NonNull final TextColor colour) {
        return component.asComponent().colorIfAbsent(colour);
    }

    /**
     * Join components with spaces and style the components if no styles are present.
     *
     * @param module the prefix of the message
     * @param body   the body of the message
     * @return the joined component
     */
    @NonNull protected static Component main(@NonNull final ComponentLike module, @NonNull final ComponentLike body) {
        final Component separator = Component.text(">").mergeStyle(module.asComponent());
        Component joinedModule = Component.join(JoinConfiguration.noSeparators(), module, separator);

        // Adjust colour
        joinedModule = changeColourIfEmpty(joinedModule, NamedTextColor.BLUE);
        final Component adjustedBody = changeColourIfEmpty(body, NamedTextColor.GRAY);

        return joinSpace(joinedModule, adjustedBody);
    }

    /**
     * Apply {@link NamedTextColor#YELLOW} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component element(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.YELLOW);
    }

    /**
     * Apply {@link NamedTextColor#WHITE} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component chat(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.WHITE);
    }

    /**
     * Apply {@link NamedTextColor#GREEN} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component time(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.GREEN);
    }

    /**
     * Apply {@link NamedTextColor#GREEN} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component skill(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.GREEN);
    }

    /**
     * Apply {@link NamedTextColor#AQUA} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component link(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.AQUA);
    }

    /**
     * Apply {@link NamedTextColor#RED} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component loot(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.RED);
    }

    /**
     * Apply {@link NamedTextColor#YELLOW} to the component if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull protected static Component count(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.YELLOW);
    }

    /**
     * Convert all components for item lores.
     *
     * @param components the components to convert
     * @return the converted components
     * @see MineplexMessageComponent#itemLore(ComponentLike)
     */
    @NonNull public static List<Component> itemLores(@NonNull final ComponentLike... components) {
        final List<Component> converted = new ArrayList<>(components.length);
        for (final ComponentLike component : components) {
            converted.add(itemLore(component));
        }
        return converted;
    }

    /**
     * Convert all components for item lores.
     *
     * @param components the components to convert
     * @return the converted components
     * @see MineplexMessageComponent#itemLore(ComponentLike)
     */
    @NonNull public static List<Component> itemLores(@NonNull final List<Component> components) {
        final List<Component> converted = new ArrayList<>(components.size());
        for (final Component component : components) {
            converted.add(itemLore(component));
        }
        return converted;
    }

    /**
     * Apply {@link NamedTextColor#GOLD} and remove {@link TextDecoration#ITALIC} to the component
     * if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull public static Component itemLore(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.GRAY)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Apply {@link NamedTextColor#WHITE} and remove {@link TextDecoration#ITALIC} to the component
     * if no styles are present.
     *
     * @param component the component to style
     * @return the styled component
     */
    @NonNull public static Component itemName(@NonNull final ComponentLike component) {
        return changeColourIfEmpty(component, NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
