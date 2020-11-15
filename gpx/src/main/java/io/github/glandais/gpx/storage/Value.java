package io.github.glandais.gpx.storage;

import io.github.glandais.gpx.storage.unit.StorageUnit;
import lombok.Getter;

@Getter
public class Value<T, U extends StorageUnit<T>> {
    protected final T value;
    protected final U unit;
    protected final ValueKind kind;

    public Value(T value, U unit, ValueKind kind) {
        this.value = value;
        this.unit = unit;
        this.kind = kind;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return unit.formatHuman(value);
    }
}
