package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.Point;

@FunctionalInterface
public interface TabularRowInit<C> {
    void accept(C context, Integer row, Point values);
}
