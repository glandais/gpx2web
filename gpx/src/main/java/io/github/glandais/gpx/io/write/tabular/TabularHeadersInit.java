package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.ValueKind;

import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface TabularHeadersInit<C> {
    void writeHeaders(C context, Map<PropertyKey<?, ?>, Set<ValueKind>> map);
}
