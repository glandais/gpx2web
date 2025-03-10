package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.*;
import io.github.glandais.gpx.io.write.FileExporter;

import java.util.*;
import java.util.function.Consumer;

public abstract class TabularFileWriter implements FileExporter {

    public void write(GPXPath path, Consumer<Map<ValueKey, Set<ValueKind>>> writeHeaders,
                      TabularRowInit initRow,
                      TabularCellWriter writeCell) {

        List<Point> points = path.getPoints();
        List<Values> valuesList = points.stream().map(Point::getCsvData).toList();

        Map<ValueKey, Set<ValueKind>> columns = new LinkedHashMap<>();
        valuesList.forEach(values -> values.getKeySet().forEach(key ->
                columns.computeIfAbsent(key, k -> new LinkedHashSet<>()).addAll(values.getAll(key).keySet())
        ));
        columns.forEach((k, v) -> v.removeIf(valueKind -> valueKind == ValueKind.current));
        writeHeaders.accept(columns);

        int i = 0;
        for (Values values : valuesList) {
            initRow.accept(i++, values);
            int j = 0;
            for (Map.Entry<ValueKey, Set<ValueKind>> column : columns.entrySet()) {
                for (ValueKind valueKind : column.getValue()) {
                    Value<?, ?> value = values.get(column.getKey(), valueKind);
                    writeCell.accept(i - 1, j++, values, new ValueKeyKind(column.getKey(), valueKind), value);
                }
            }
        }
    }

}
