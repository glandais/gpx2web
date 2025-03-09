package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.values.Values;

@FunctionalInterface
public interface TabularRowInit {
    void accept(Integer row, Values values);
}
