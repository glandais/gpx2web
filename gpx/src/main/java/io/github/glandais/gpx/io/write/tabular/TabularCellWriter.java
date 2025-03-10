package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.values.Value;
import io.github.glandais.gpx.data.values.ValueKeyKind;
import io.github.glandais.gpx.data.values.Values;

@FunctionalInterface
public interface TabularCellWriter {
    void accept(Integer row, Integer col, Values values, ValueKeyKind key, Value<?, ?> value);
}
