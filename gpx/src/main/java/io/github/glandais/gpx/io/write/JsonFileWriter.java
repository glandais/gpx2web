package io.github.glandais.gpx.io.write;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.unit.Unit;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class JsonFileWriter implements FileExporter {

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();

        Set<String> keys = new TreeSet<>();
        List<Map<String, Object>> pointsValues = new ArrayList<>();
        for (Point point : path.getPoints()) {
            Map<String, Object> pointValues = new LinkedHashMap<>();
            for (PropertyKey<?, ?> key : PropertyKeys.getList()) {
                Object value = getValue(point, key);
                if (value != null) {
                    String propertyKeyName = key.getPropertyKeyName();
                    pointValues.put(propertyKeyName, value);
                    keys.add(propertyKeyName);
                }
            }
            pointsValues.add(pointValues);
        }

        map.put("keys", keys.stream().map(k -> "\"" + k + "\"").toList());
        map.put("points", pointsValues);

        write(map, file);
    }

    private <S, U extends Unit<S>> Object getValue(Point point, PropertyKey<S, U> key) {
        S value = point.get(key);
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return key.formatHuman(value);
        } else {
            return "\"" + key.formatHuman(value) + "\"";
        }
    }

    public void write(Map map, File target) throws IOException {
        try (FileWriter fw = new FileWriter(target)) {
            write(fw, map);
        }
    }

    public void write(FileWriter fw, Object o) throws IOException {
        if (o == null) {
            fw.write("null");
            return;
        }
        if (o instanceof List l) {
            fw.write("[");
            boolean first = true;
            for (Object object : l) {
                if (!first) {
                    fw.write(",");
                }
                first = false;
                write(fw, object);
            }
            fw.write("]");
        } else if (o instanceof Map<?, ?> m) {
            fw.write("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) {
                    fw.write(",");
                }
                first = false;
                write(fw, "\"" + e.getKey() + "\":");
                write(fw, e.getValue());
            }
            fw.write("}");
        } else {
            fw.write(o.toString());
        }
    }
}
