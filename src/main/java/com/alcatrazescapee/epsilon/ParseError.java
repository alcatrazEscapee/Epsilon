package com.alcatrazescapee.epsilon;

final class ParseError extends RuntimeException
{
    ParseError(String message)
    {
        super(message);
    }
}
