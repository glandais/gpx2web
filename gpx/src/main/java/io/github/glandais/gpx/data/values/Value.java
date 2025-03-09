package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.unit.StorageUnit;

public record Value<T, U extends StorageUnit<T>>(T value, U unit, ValueKind kind) {

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return unit.formatHuman(value);
    }

    public Value<?, ?> copy() {
        return new Value<>(
                value,
                unit,
                kind
        );
    }
}
