package com.alcatrazescapee.epsilon;

import java.util.function.Supplier;

/**
 * An error occurring during a transformation from a config value or type to a derived value as specified in a {@link Spec}.
 */
public final class ParseError extends RuntimeException
{
    /**
     * Throw a {@link ParseError} with the message of {@code error} if {@code value} is {@code null}.
     */
    public static <T> T requireNotNull(T value, String error) throws ParseError
    {
        if (value == null) throw new ParseError(error);
        return value;
    }

    /**
     * Throw a {@link ParseError} with the message of {@code error} if the value obtained by resolving {@code value} is {@code null}, or throws any kind of {@link RuntimeException}.
     */
    public static <T> T requireNotNull(Supplier<T> value, String error) throws ParseError
    {
        return requireNotNull(require(value), error);
    }

    /**
     * Throw a {@link ParseError} with the message of {@code error} if {@code value} throws any kind of {@link RuntimeException}.
     */
    public static <T> T require(Supplier<T> value) throws ParseError
    {
        try { return value.get(); }
        catch (RuntimeException e) { throw new ParseError(e.getMessage()); }
    }

    public ParseError(String message)
    {
        super(message);
    }
}
