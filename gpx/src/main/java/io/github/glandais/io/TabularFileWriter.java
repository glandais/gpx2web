package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Value;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.gpx.storage.Values;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TabularFileWriter {

    public void write(GPXPath path, Consumer<Map<String, Set<ValueKind>>> writeHeaders,
                      TabularRowInit initRow,
                      TabularCellWriter writeCell) {

        List<Point> points = path.getPoints();
        List<Values> valuesList = points.stream().map(Point::getCsvData).collect(Collectors.toList());

        Map<String, Set<ValueKind>> columns = new LinkedHashMap<>();
        valuesList.forEach(values -> values.getKeySet().forEach(key ->
                columns.computeIfAbsent(key, k -> new LinkedHashSet<>()).addAll(values.getAll(key).keySet())
        ));
        columns.forEach((k, v) -> v.removeIf(valueKind -> valueKind == ValueKind.current));
        writeHeaders.accept(columns);

        int i = 0;
        for (Values values : valuesList) {
            initRow.accept(i++, values);
            int j = 0;
            for (Map.Entry<String, Set<ValueKind>> column : columns.entrySet()) {
                for (ValueKind valueKind : column.getValue()) {
                    Value<?, ?> value = values.get(column.getKey(), valueKind);
                    writeCell.accept(i - 1, j++, values, new ValueKey(column.getKey(), valueKind), value);
                }
            }
        }
    }

}
