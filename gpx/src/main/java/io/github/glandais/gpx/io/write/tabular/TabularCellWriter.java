package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.Unit;

@FunctionalInterface
public interface TabularCellWriter<C> {
    <S, U extends Unit<S>> void accept(C context, Integer row, Integer col, Point point, PropertyKey<S, U> propertyKey);
}
