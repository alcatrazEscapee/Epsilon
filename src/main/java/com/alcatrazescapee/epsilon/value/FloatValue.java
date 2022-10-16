package com.alcatrazescapee.epsilon.value;

import org.jetbrains.annotations.NotNull;

public final class FloatValue implements Value<Float>
{
    private float value;

    public FloatValue(float value)
    {
        this.value = value;
    }

    public float getAsFloat() { return value; }
    @Override @NotNull public Float get() { return value; }
    @Override public void set(Float value) { this.value = value; }
}
