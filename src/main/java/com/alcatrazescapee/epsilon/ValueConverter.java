package com.alcatrazescapee.epsilon;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import com.alcatrazescapee.epsilon.value.BoolValue;
import com.alcatrazescapee.epsilon.value.FloatValue;
import com.alcatrazescapee.epsilon.value.IntValue;
import com.alcatrazescapee.epsilon.value.TypeValue;
import com.alcatrazescapee.epsilon.value.Value;

public record ValueConverter<T, U, V extends Value<U>>(Type<T> type, Function<T, U> parseFunction, Function<U, T> writeFunction, Function<U, V> factory)
{
    public static final ValueConverter<Integer, Integer, IntValue> INT = Type.INT.map(IntValue::new);
    public static final ValueConverter<Float, Float, FloatValue> FLOAT = Type.FLOAT.map(FloatValue::new);
    public static final ValueConverter<Boolean, Boolean, BoolValue> BOOL = Type.BOOL.map(BoolValue::new);
    public static final ValueConverter<String, String, TypeValue<String>> STRING = Type.STRING.map(TypeValue::new);
    public static final ValueConverter<List<String>, List<String>, TypeValue<List<String>>> STRING_LIST = Type.STRING_LIST.map(TypeValue::new);

    public static ValueConverter<Integer, Integer, IntValue> forRange(int minValue, int maxValue)
    {
        return Type.INT.map(value -> {
            if (value < minValue || value > maxValue) throw new ParseError("Value " + value + " not in range [" + minValue + ", " + maxValue + "]");
        }, IntValue::new);
    }

    public static ValueConverter<Float, Float, FloatValue> forRange(float minValue, float maxValue)
    {
        return Type.FLOAT.map(value -> {
            if (value < minValue || value > maxValue) throw new ParseError("Value " + value + " not in range [" + minValue + ", " + maxValue + "]");
        }, FloatValue::new);
    }

    public static <E extends Enum<E>> ValueConverter<String, E, TypeValue<E>> forEnum(Class<E> enumClass, EnumSet<E> set)
    {
        return Type.STRING.map(value -> {
            final E enumValue;
            try { enumValue = Enum.valueOf(enumClass, value); }
            catch (IllegalArgumentException e) { throw new ParseError("Invalid value: '%s', must be one of %s".formatted(value, set)); }
            if (!set.contains(enumValue)) throw new ParseError("Invalid value: '%s', must be one of %s".formatted(value, set));
            return enumValue;
        }, Enum::name, TypeValue::new);
    }

    void parseInto(Object object, V value)
    {
        value.set(parseFunction.apply(type.parse(object)));
    }

    T write(V value)
    {
        return writeFunction.apply(value.get());
    }

    V create(U defaultValue)
    {
        return factory.apply(defaultValue);
    }
}
