package com.alcatrazescapee.epsilon;

import java.util.function.Supplier;

public final class ParseError extends RuntimeException
{
    public static <T> T requireNotNull(T value, String error) throws ParseError
    {
        if (value == null) throw new ParseError(error);
        return value;
    }

    public static <T> T requireNotNull(Supplier<T> value, String error) throws ParseError
    {
        return requireNotNull(require(value), error);
    }

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
