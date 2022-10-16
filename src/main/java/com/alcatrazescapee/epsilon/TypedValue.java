package com.alcatrazescapee.epsilon;

import java.util.function.Consumer;

import com.alcatrazescapee.epsilon.value.Value;

record TypedValue<T, U, V extends Value<U>>(String name, String longName, String[] comment, V value, U defaultValue, ValueConverter<T, U, V> converter)
{
    T write()
    {
        return converter.write(value);
    }

    void parse(Object object, Consumer<ParseError> error)
    {
        ParseError.resolve(() -> converter.parseInto(object, value), e -> error.accept(e.map(m -> "Reading " + longName + ": " + m)));
    }

    void parseDefault()
    {
        value.set(defaultValue);
    }
}
