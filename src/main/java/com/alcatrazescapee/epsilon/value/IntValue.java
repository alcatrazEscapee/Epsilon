package com.alcatrazescapee.epsilon.value;

import java.util.function.IntSupplier;
import org.jetbrains.annotations.NotNull;

public final class IntValue implements IntSupplier, Value<Integer>
{
    private int value;

    public IntValue(int value)
    {
        this.value = value;
    }

    @Override public int getAsInt() { return value; }
    @Override @NotNull public Integer get() { return value; }
    @Override public void set(Integer value) { this.value = value; }
}
