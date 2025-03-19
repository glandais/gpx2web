package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.values.PropertyKey;
import java.util.Set;

@FunctionalInterface
public interface TabularHeadersInit<C> {
    void writeHeaders(C context, Set<PropertyKey<?, ?>> set);
}
