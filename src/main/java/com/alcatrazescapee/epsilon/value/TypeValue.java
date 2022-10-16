package com.alcatrazescapee.epsilon.value;

public final class TypeValue<T> implements Value<T>
{
    private T value;

    @Override public T get() { return value; }
    @Override public void set(T value) { this.value = value; }
}
