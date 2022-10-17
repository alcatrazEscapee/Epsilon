package com.alcatrazescapee.epsilon;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.alcatrazescapee.epsilon.value.TypeValue;
import com.alcatrazescapee.epsilon.value.Value;

/**
 * A {@code Type<T>} is a type that is able to serialize to toml directly. As such, it must be one of the supported primitive types ({@code BOOL, INT, FLOAT, STRING}), or a derived type such as a list (i.e. {@code STRING_LIST}).
 * A {@link ValueConverter} can be created by starting with a {@code Type}, and using one of the {@code map()} variants.
 */
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

    /**
     * Create a {@link ValueConverter} which holds a value of type {@code T}, in a value class of type {@code V}. Prefer using the constants in {@link ValueConverter} over using this directly.
     */
    default <V extends Value<T>> ValueConverter<T, T, V> map(Function<T, V> factory)
    {
        return map(Function.identity(), Function.identity(), factory);
    }

    /**
     * Creates a {@link ValueConverter} which holds a value of type {@code T}, in a value class of type {@code V}. The value is validated using the {@code parseFunction}, which should throw a {@link ParseError} if the provided value is invalid.
     */
    default <V extends Value<T>> ValueConverter<T, T, V> map(Consumer<T> parseFunction, Function<T, V> factory)
    {
        return map(t -> {
            parseFunction.accept(t);
            return t;
        }, Function.identity(), factory);
    }

    /**
     * Creates a {@link ValueConverter} which holds a value of type {@code U}, in a value class of type {@code V}. The value is converted between the raw type {@code T} and the derived type {@code U} using the {@code parseFunction} and {@code writeFunction}. The {@code parseFunction} may throw a {@link ParseError} if the provided value is invalid.
     * This variant uses the generic {@link TypeValue} class for holding arbitrary config objects.
     */
    default <U> ValueConverter<T, U, TypeValue<U>> map(Function<T, U> parseFunction, Function<U, T> writeFunction)
    {
        return map(parseFunction, writeFunction, TypeValue::new);
    }

    /**
     * Creates a {@link ValueConverter} which holds a value of type {@code U}, in a value class of type {@code V}. The value is converted between the raw type {@code T} and the derived type {@code U} using the {@code parseFunction} and {@code writeFunction}. The {@code parseFunction} may throw a {@link ParseError} if the provided value is invalid.
     */
    default <U, V extends Value<U>> ValueConverter<T, U, V> map(Function<T, U> parseFunction, Function<U, T> writeFunction, Function<U, V> factory)
    {
        return new ValueConverter<>(this, parseFunction, writeFunction, factory);
    }
}
