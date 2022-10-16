package com.alcatrazescapee.epsilon.value;

import java.util.function.IntSupplier;

public final class IntValue implements IntSupplier, Value<Integer>
{
    private int value;

    @Override public int getAsInt() { return value; }
    @Override public Integer get() { return value; }
    @Override public void set(Integer value) { this.value = value; }
}
