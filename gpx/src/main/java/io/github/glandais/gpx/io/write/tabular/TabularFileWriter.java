package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.io.write.FileExporter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class TabularFileWriter<C> implements FileExporter {

    public void write(
            C context,
            GPXPath path,
            TabularHeadersInit<C> initHeaders,
            TabularRowInit<C> initRow,
            TabularCellWriter<C> writeCell) {

        List<Point> points = path.getPoints();

        Set<PropertyKey<?, ?>> columns = new LinkedHashSet<>();
        for (Point point : points) {
            for (PropertyKey<?, ?> propertyKey : PropertyKeys.getList()) {
                if (point.get(propertyKey) != null) {
                    columns.add(propertyKey);
                }
            }
        }
        initHeaders.writeHeaders(context, columns);

        int i = 0;
        for (Point point : points) {
            initRow.accept(context, i++, point);
            int j = 0;
            for (PropertyKey<?, ?> column : columns) {
                writeCell.accept(context, i - 1, j++, point, column);
            }
        }
    }
}
