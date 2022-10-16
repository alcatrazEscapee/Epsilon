package com.alcatrazescapee.epsilon.value;

import java.util.function.BooleanSupplier;
import org.jetbrains.annotations.NotNull;


public final class BoolValue implements BooleanSupplier, Value<Boolean>
{
    private boolean value;

    public BoolValue(boolean value)
    {
        this.value = value;
    }

    @Override public boolean getAsBoolean() { return value; }
    @Override @NotNull public Boolean get() { return value; }
    @Override public void set(Boolean value) { this.value = value; }
}
