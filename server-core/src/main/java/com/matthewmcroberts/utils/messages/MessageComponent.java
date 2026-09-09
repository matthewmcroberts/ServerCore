package com.matthewmcroberts.utils.messages;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.matthewmcroberts.utils.functions.PentaFunction;
import com.matthewmcroberts.utils.functions.QuadFunction;
import com.matthewmcroberts.utils.functions.TriFunction;
import lombok.NonNull;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Utility class for creating messages.
 */
public abstract class MessageComponent {
    /**
     * Join components with spaces.
     *
     * @param components the components
     * @return the joined component
     */
    @NonNull protected static Component joinSpace(@NonNull final ComponentLike... components) {
        return Component.join(JoinConfiguration.spaces(), components);
    }

    /**
     * Create a message with no arguments.
     */
    @FunctionalInterface
    public interface Args0 extends Supplier<Component> {
        @Override
        default @NonNull Component get() {
            return this.apply();
        }

        /**
         * Create the message.
         *
         * @return the message
         */
        @NonNull Component apply();

        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         */
        default void send(@NonNull final Audience audience) {
            audience.sendMessage(this.apply());
        }
    }

    /**
     * Create a message with one argument.
     *
     * @param <T> the type of the argument
     */
    @FunctionalInterface
    public interface Args1<T> extends Function<T, Component> {
        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         * @param t        the argument
         */
        @SuppressWarnings("checkstyle:ParameterName")
        default void send(@NonNull final Audience audience, @UnknownNullability final T t) {
            audience.sendMessage(this.apply(t));
        }
    }

    /**
     * Create a message with two arguments.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     */
    @FunctionalInterface
    public interface Args2<T, U> extends BiFunction<T, U, Component> {
        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         * @param t        the first argument
         * @param u        the second argument
         */
        @SuppressWarnings("checkstyle:ParameterName")
        default void send(
                @NonNull final Audience audience, @UnknownNullability final T t, @UnknownNullability final U u) {
            audience.sendMessage(this.apply(t, u));
        }
    }

    /**
     * Create a message with three arguments.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     * @param <V> the type of the third argument
     */
    @FunctionalInterface
    public interface Args3<T, U, V> extends TriFunction<T, U, V, Component> {
        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         * @param t        the first argument
         * @param u        the second argument
         * @param v        the third argument
         */
        @SuppressWarnings("checkstyle:ParameterName")
        default void send(
                @NonNull final Audience audience,
                @UnknownNullability final T t,
                @UnknownNullability final U u,
                @UnknownNullability final V v) {
            audience.sendMessage(this.apply(t, u, v));
        }
    }

    /**
     * Create a message with four arguments.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     * @param <V> the type of the third argument
     * @param <W> the type of the fourth argument
     */
    @FunctionalInterface
    public interface Args4<T, U, V, W> extends QuadFunction<T, U, V, W, Component> {
        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         * @param t        the first argument
         * @param u        the second argument
         * @param v        the third argument
         * @param w        the fourth argument
         */
        @SuppressWarnings("checkstyle:ParameterName")
        default void send(
                @NonNull final Audience audience,
                @UnknownNullability final T t,
                @UnknownNullability final U u,
                @UnknownNullability final V v,
                @UnknownNullability final W w) {
            audience.sendMessage(this.apply(t, u, v, w));
        }
    }

    /**
     * Create a message with five arguments.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     * @param <V> the type of the third argument
     * @param <W> the type of the fourth argument
     * @param <X> the type of the fifth argument
     */
    @FunctionalInterface
    public interface Args5<T, U, V, W, X> extends PentaFunction<T, U, V, W, X, Component> {
        /**
         * Send the message to an audience.
         *
         * @param audience the audience
         * @param t        the first argument
         * @param u        the second argument
         * @param v        the third argument
         * @param w        the fourth argument
         * @param x        the fifth argument
         */
        @SuppressWarnings("checkstyle:ParameterName")
        default void send(
                @NonNull final Audience audience,
                @UnknownNullability final T t,
                @UnknownNullability final U u,
                @UnknownNullability final V v,
                @UnknownNullability final W w,
                @UnknownNullability final X x) {
            audience.sendMessage(this.apply(t, u, v, w, x));
        }
    }
}
