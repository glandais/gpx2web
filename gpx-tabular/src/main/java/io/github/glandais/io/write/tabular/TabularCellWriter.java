package io.github.glandais.io.write.tabular;

import io.github.glandais.gpx.data.values.Value;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.Values;

@FunctionalInterface
public interface TabularCellWriter {
    void accept(Integer row, Integer col, Values values, ValueKey key, Value<?, ?> value);
}
