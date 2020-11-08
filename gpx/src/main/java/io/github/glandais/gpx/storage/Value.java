package io.github.glandais.gpx.storage;

import io.github.glandais.gpx.storage.unit.StorageUnit;

public class Value<T, U extends StorageUnit<T>> {
    protected final T value;
    protected final U unit;

    public Value(T value, U unit) {
        this.value = value;
        this.unit = unit;
    }

    public T getValue() {
        return value;
    }

    public U getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return unit.formatHuman(value);
    }
}
