package com.alcatrazescapee.epsilon.value;

import org.jetbrains.annotations.NotNull;

public final class TypeValue<T> implements Value<T>
{
    @NotNull private T value;

    public TypeValue(@NotNull T value)
    {
        this.value = value;
    }

    @Override @NotNull public T get() { return value; }
    @Override public void set(T value) { this.value = value; }
}
