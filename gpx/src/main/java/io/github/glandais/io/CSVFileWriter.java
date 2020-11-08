package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Value;
import io.github.glandais.gpx.storage.Values;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CSVFileWriter {

    public void writeCsvFile(GPXPath path, File file) throws IOException {
        List<Point> points = path.getPoints();
        Map<String, List<String>> collections = new TreeMap<>();

        getCollections(points, collections, Point::getData);
        getCollections(points, collections, Point::getDebug);

        FileWriter fw = new FileWriter(file);
        fw.write(collections.keySet().stream().collect(Collectors.joining(";")) + "\n");
        for (int i = 0; i < points.size(); i++) {
            List<String> line = new ArrayList<>(collections.size());
            for (String collection : collections.keySet()) {
                line.add(collections.get(collection).get(i));
            }
            fw.write(line.stream().collect(Collectors.joining(";")) + "\n");
        }
        fw.close();
    }

    private void getCollections(List<Point> points, Map<String, List<String>> collections, Function<Point, Values> valuesGetter) {
        Set<String> columns = new LinkedHashSet<>();
        for (Point point : points) {
            valuesGetter.apply(point).forEach((k, v) -> columns.add(k));
        }
        for (String column : columns) {
            List<String> collection = points.stream().map(p -> {
                Value value = valuesGetter.apply(p).get(column);
                if (value == null) {
                    return "";
                }
                return value.getUnit().formatHuman(value.getValue());
            }).collect(Collectors.toList());

            // verify is same data already exist
            boolean add = true;
            for (List<String> otherCollection : collections.values()) {
                if (add && collection.equals(otherCollection)) {
                    add = false;
                }
            }
            if (add) {
                collections.put(column, collection);
            }
        }
    }

}
