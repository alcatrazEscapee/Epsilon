package com.alcatrazescapee.epsilon;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.alcatrazescapee.epsilon.value.TypeValue;
import com.alcatrazescapee.epsilon.value.Value;

public interface Type<T>
{
    Type<Boolean> BOOL = Type::asBool;
    Type<Integer> INT = Type::asInt;
    Type<Float> FLOAT = Type::asFloat;
    Type<String> STRING = Type::asString;
    Type<List<String>> STRING_LIST = t -> Type.asList(t, Type::asString);

    static boolean asBool(Object token) throws ParseError
    {
        if (token instanceof Boolean boolValue) return boolValue;
        throw new ParseError("Cannot convert " + token + " to boolean");
    }

    static int asInt(Object value) throws ParseError
    {
        if (value instanceof Integer intValue) return intValue;
        throw new ParseError("Cannot convert " + value + " to int");
    }

    static float asFloat(Object value) throws ParseError
    {
        if (value instanceof Float floatValue) return floatValue;
        if (value instanceof Integer intValue) return intValue;
        throw new ParseError("Cannot convert " + value + " to float");
    }

    static String asString(Object value) throws ParseError
    {
        if (value instanceof String string) return string;
        throw new ParseError("Cannot convert " + value + " to string");
    }

    static <T> List<T> asList(Object token, Function<Object, T> element) throws ParseError
    {
        if (token instanceof List<?> value) return value.stream().map(element).toList();
        throw new ParseError("Cannot convert " + token + " to list");
    }

    T parse(Object token);

    default <V extends Value<T>> ValueConverter<T, T, V> map(Supplier<V> factory)
    {
        return map(Function.identity(), Function.identity(), factory);
    }

    default <V extends Value<T>> ValueConverter<T, T, V> map(Function<T, T> parseFunction, Supplier<V> factory)
    {
        return map(parseFunction, Function.identity(), factory);
    }

    default <U> ValueConverter<T, U, TypeValue<U>> map(Function<T, U> parseFunction, Function<U, T> writeFunction)
    {
        return map(parseFunction, writeFunction, TypeValue::new);
    }

    default <U, V extends Value<U>> ValueConverter<T, U, V> map(Function<T, U> parseFunction, Function<U, T> writeFunction, Supplier<V> factory)
    {
        return new ValueConverter<>(this, parseFunction, writeFunction, factory);
    }
}
