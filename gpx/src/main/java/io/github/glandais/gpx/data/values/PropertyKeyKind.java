package io.github.glandais.gpx.data.values;

import io.github.glandais.gpx.data.values.unit.Unit;

public record PropertyKeyKind<S, U extends Unit<S>>(
        PropertyKey<S, U> propertyKey,
        ValueKind kind
) {
}
