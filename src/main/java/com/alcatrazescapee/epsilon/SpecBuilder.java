package com.alcatrazescapee.epsilon;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.alcatrazescapee.epsilon.value.BoolValue;
import com.alcatrazescapee.epsilon.value.FloatValue;
import com.alcatrazescapee.epsilon.value.IntValue;
import com.alcatrazescapee.epsilon.value.TypeValue;
import com.alcatrazescapee.epsilon.value.Value;

/**
 * A builder for a specification. {@link Value} forms the bridge between the internal spec which is used to populate from IR.
 */
public interface SpecBuilder
{
    /**
     * Pushes a category with the name {@code name} onto the stack. Allows nesting of named config elements, which are seperated with a dot '.' character.
     */
    SpecBuilder push(String name);

    /**
     * Pops the last category from the stack.
     */
    SpecBuilder pop();

    /**
     * Pops the last {@code n} categories from the stack.
     */
    default SpecBuilder pop(int n)
    {
        for (int i = 0; i < n; i++) pop();
        return this;
    }

    /**
     * Swaps the top element of the stack.
     */
    default SpecBuilder swap(String name)
    {
        return pop().push(name);
    }

    /**
     * Defines a comment, which will be attached to the next value created by any invocation of {@code define()}. Subsequent calls to this will append to the existing comment.
     * If this is invoked before a call to {@link #push(String)}, the comment will be instead prepended to the category.
     */
    SpecBuilder comment(String... comment);

    /**
     * Define an unrestricted integer value with the name {@code name} and a default value of {@code defaultValue}.
     */
    default IntValue define(String name, int defaultValue)
    {
        return define(name, defaultValue, ValueConverter.INT);
    }

    /**
     * Define an integer value which must take values in the inclusive range {@code [minValue, maxValue]} with the name {@code name} and a default value of {@code defaultValue}.
     */
    default IntValue define(String name, int defaultValue, int minValue, int maxValue)
    {
        return comment("Range: [%d, %d]".formatted(minValue, maxValue)).define(name, defaultValue, ValueConverter.forRange(minValue, maxValue));
    }

    /**
     * Define an unrestricted float value with the name {@code name} and a default value of {@code defaultValue}.
     */
    default FloatValue define(String name, float defaultValue)
    {
        return define(name, defaultValue, ValueConverter.FLOAT);
    }

    /**
     * Define a float value which must take values in the inclusive range {@code [minValue, maxValue]} with the name {@code name} and a default value of {@code defaultValue}.
     */
    default FloatValue define(String name, float defaultValue, float minValue, float maxValue)
    {
        return comment("Range: [%s, %s]".formatted(minValue, maxValue)).define(name, defaultValue, ValueConverter.forRange(minValue, maxValue));
    }

    /**
     * Define a boolean value with the name {@code name} and a default value of {@code defaultValue}.
     */
    default BoolValue define(String name, boolean defaultValue)
    {
        return define(name, defaultValue, ValueConverter.BOOL);
    }

    /**
     * Define a string value with the name {@code name} and a default value of {@code defaultValue}.
     */
    default TypeValue<String> define(String name, String defaultValue)
    {
        return define(name, defaultValue, ValueConverter.STRING);
    }

    /**
     * Define a string value with the name {@code name} and a default value of {@code defaultValue}. The value must satisfy the {@code predicate}, or will report a parse error of {@code error}.
     */
    default TypeValue<String> define(String name, String defaultValue, Predicate<String> predicate, String error)
    {
        return define(name, defaultValue, Type.STRING.map(value -> {
            if (!predicate.test(value)) throw new ParseError(error);
        }, TypeValue::new));
    }

    /**
     * Define a string list value with the name {@code name} and a default value of {@code defaultValue}. The value must satisfy the {@code predicate}, or will report a parse error of {@code error}.
     */
    default TypeValue<List<String>> define(String name, List<String> defaultValue)
    {
        return define(name, defaultValue, ValueConverter.STRING_LIST);
    }

    /**
     * Define an enum value with the name {@code name} and a default value of {@code defaultValue}.
     */
    default <E extends Enum<E>> TypeValue<E> define(String name, E defaultValue, Class<E> enumClass)
    {
        return define(name, defaultValue, enumClass, EnumSet.allOf(enumClass));
    }

    /**
     * Define an enum value with the name {@code name} and a default value of {@code defaultValue} where the value must be one of the provided {@code allowedValue}s.
     */
    default <E extends Enum<E>> TypeValue<E> define(String name, E defaultValue, Class<E> enumClass, E allowedValue, E... allowedValues)
    {
        return define(name, defaultValue, enumClass, EnumSet.of(allowedValue, allowedValues));
    }

    /**
     * Define an enum value with the name {@code name} and a default value of {@code defaultValue}, where the value must be present in the {@code set}.
     */
    default <E extends Enum<E>> TypeValue<E> define(String name, E defaultValue, Class<E> enumClass, EnumSet<E> set)
    {
        return comment("Allowed values: %s".formatted(Arrays.stream(enumClass.getEnumConstants()).filter(set::contains).map(Enum::name).collect(Collectors.joining(", ")))).define(name, defaultValue, ValueConverter.forEnum(enumClass, set));
    }

    /**
     * Define an arbitrary typed value with a default name {@code name} and a default value of {@code defaultValue}. The value is internally coerced using the {@code converter} from its config representation.
     */
    <T, U, V extends Value<U>> V define(String name, U defaultValue, ValueConverter<T, U, V> converter);

    /**
     * Builds the completed spec.
     */
    Spec build();
}
