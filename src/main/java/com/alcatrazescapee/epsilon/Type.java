package com.alcatrazescapee.epsilon;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alcatrazescapee.epsilon.value.TypeValue;
import com.alcatrazescapee.epsilon.value.Value;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A {@code Type<T>} is a type that is able to serialize to toml directly. As such, it must be one of the supported primitive types ({@code BOOL, INT, FLOAT, STRING}), or a derived type such as a list (i.e. {@code STRING_LIST}).
 * A {@link ValueConverter} can be created by starting with a {@code Type}, and using one of the {@code map()} variants.
 */
public interface Type<T>
{
    Type<Boolean> BOOL = token -> {
        if (token instanceof Boolean boolValue) return boolValue;
        throw new ParseError("Cannot convert " + token + " to boolean");
    };
    Type<Integer> INT = token -> {
        if (token instanceof Integer intValue) return intValue;
        throw new ParseError("Cannot convert " + token + " to int");
    };
    Type<Float> FLOAT = token -> {
        if (token instanceof Float floatValue) return floatValue;
        if (token instanceof Integer intValue) return intValue.floatValue();
        throw new ParseError("Cannot convert " + token + " to float");
    };
    Type<String> STRING = new Type<>() {
        @Override
        public String parse(Object token)
        {
            if (token instanceof String string) return string;
            throw new ParseError("Cannot convert " + token + " to string");
        }

        @Override
        @SuppressWarnings("deprecation")
        public String write(String value)
        {
            return "\"%s\"".formatted(StringEscapeUtils.escapeJava(value));
        }
    };

    Type<List<String>> STRING_LIST = Type.STRING.listOf();

    /**
     * @return A new type representing a {@code List<T>} of the underlying {@code elementType}
     */
    static <T> Type<List<T>> list(Type<T> elementType)
    {
        return new Type<>() {
            @Override
            public List<T> parse(Object token)
            {
                if (token instanceof List<?> value) return value.stream().map(elementType::parse).toList();
                throw new ParseError("Cannot convert " + token + " to list");
            }

            @Override
            public String write(List<T> value)
            {
                return "[%s]".formatted(value.stream()
                    .map(elementType::write)
                    .collect(Collectors.joining(", ")));
            }
        };
    }

    /**
     * Parses an object of type {@code <T>} from a toml value. The token will be a representable toml value such as int, boolean, string, or list.
     * @param token A toml representable object value, which may be a {@link Integer}, {@link Boolean}, {@link Float}, {@link String}, or {@link List}.
     * @return An intermediate representation of the object.
     */
    T parse(Object token) throws ParseError;

    /**
     * Writes this type to a toml representable string.
     * @param value A value of the type {@code <T>}.
     * @return A toml representable string.
     */
    default String write(T value)
    {
        return String.valueOf(value);
    }

    /**
     * @return A new type representing a {@code List<T>} of this type.
     */
    default Type<List<T>> listOf()
    {
        return list(this);
    }

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
