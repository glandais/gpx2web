package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeyKind;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.io.write.FileExporter;

import java.util.*;

public abstract class TabularFileWriter<C> implements FileExporter {

    public void write(C context, GPXPath path,
                      TabularHeadersInit<C> initHeaders,
                      TabularRowInit<C> initRow,
                      TabularCellWriter<C> writeCell) {

        List<Point> points = path.getPoints();

        Map<PropertyKey<?, ?>, Set<ValueKind>> columns = new LinkedHashMap<>();
        for (Point point : points) {
            for (PropertyKey<?, ?> propertyKey : PropertyKeys.getList()) {
                for (ValueKind valueKind : ValueKind.values()) {
                    if (point.get(propertyKey, valueKind) != null) {
                        columns.computeIfAbsent(propertyKey, k -> new LinkedHashSet<>()).add(valueKind);
                    }
                }
            }
        }
        columns.forEach((k, v) -> v.removeIf(valueKind -> valueKind == ValueKind.current));
        initHeaders.writeHeaders(context, columns);

        int i = 0;
        for (Point point : points) {
            initRow.accept(context, i++, point);
            int j = 0;
            for (Map.Entry<PropertyKey<?, ?>, Set<ValueKind>> column : columns.entrySet()) {
                for (ValueKind valueKind : column.getValue()) {
                    writeCell.accept(context, i - 1, j++, point, new PropertyKeyKind<>(column.getKey(), valueKind));
                }
            }
        }
    }

}
