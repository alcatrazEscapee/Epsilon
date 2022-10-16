package com.alcatrazescapee.epsilon.value;

public final class FloatValue implements Value<Float>
{
    private float value;

    public float getAsFloat() { return value; }
    @Override public Float get() { return value; }
    @Override public void set(Float value) { this.value = value; }
}
