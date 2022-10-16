package com.alcatrazescapee.epsilon;

import java.util.function.Consumer;

import com.alcatrazescapee.epsilon.value.Value;

record TypedValue<T, U, V extends Value<U>>(String name, String longName, String[] comment, V value, U defaultValue, ValueConverter<T, U, V> converter)
{
    T write()
    {
        return converter.write(value);
    }

    void parse(Object object, Consumer<String> error)
    {
        try { converter.parseInto(object, value); }
        catch (ParseError e) { error.accept("Reading " + longName + ": " + e.getMessage()); }
    }

    void parseDefault()
    {
        value.set(defaultValue);
    }
}
