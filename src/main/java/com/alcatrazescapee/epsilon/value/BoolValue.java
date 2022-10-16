package com.alcatrazescapee.epsilon.value;

import java.util.function.BooleanSupplier;


public final class BoolValue implements BooleanSupplier, Value<Boolean>
{
    private boolean value;

    @Override public boolean getAsBoolean() { return value; }
    @Override public Boolean get() { return value; }
    @Override public void set(Boolean value) { this.value = value; }
}
