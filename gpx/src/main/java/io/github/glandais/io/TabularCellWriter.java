package io.github.glandais.io;

import io.github.glandais.gpx.storage.Value;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.Values;

@FunctionalInterface
public interface TabularCellWriter {
    void accept(Integer row, Integer col, Values values, ValueKey key, Value<?, ?> value);
}
