package com.alcatrazescapee.epsilon.value;

import java.util.function.Supplier;

/**
 * A wrapped representation of a type. This both implements {@link Supplier}, and type specific suppliers in derived classes where necessary.
 *
 * @see IntValue
 * @see FloatValue
 * @see BoolValue
 * @see TypeValue
 */
public interface Value<T> extends Supplier<T>
{
    @Override T get();
    void set(T value);
}
