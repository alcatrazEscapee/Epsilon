package com.alcatrazescapee.epsilon;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ParseError extends RuntimeException
{
    static void resolve(Runnable action, Consumer<ParseError> error)
    {
        try { action.run(); }
        catch (ParseError e) { error.accept(e); }
    }

    ParseError(String message)
    {
        super(message);
    }

    public ParseError map(UnaryOperator<String> op)
    {
        return new ParseError(op.apply(getMessage()));
    }
}
