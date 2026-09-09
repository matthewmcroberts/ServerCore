package com.matthewmcroberts.utils.functions;

import lombok.NonNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

/**
 * Represents a function that accepts five arguments and produces a result.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object, Object, Object, Object)}.
 *
 * @param <T> the first type of the input to the function
 * @param <U> the second type of the input to the function
 * @param <V> the third type of the input to the function
 * @param <W> the fourth type of the input to the function
 * @param <X> the fifth type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface PentaFunction<T, U, V, W, X, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @param w the fourth function argument
     * @param x the fifth function argument
     * @return the function result
     */
    @SuppressWarnings("checkstyle:ParameterName")
    @UnknownNullability
    R apply(
            @UnknownNullability T t,
            @UnknownNullability U u,
            @UnknownNullability V v,
            @UnknownNullability W w,
            @UnknownNullability X x);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <S>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    @UnknownNullability
    default <S> PentaFunction<T, U, V, W, X, S> andThen(@NonNull final Function<? super R, ? extends S> after) {
        return (t, u, v, w, x) -> after.apply(this.apply(t, u, v, w, x));
    }
}
