package io.github.glandais.gpx.io.write;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Value;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.data.values.Values;
import io.github.glandais.gpx.data.values.unit.NumberUnit;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
@Singleton
public class JsonFileWriter implements FileExporter {

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();

        Set<ValueKey> keys = new TreeSet<>();
        List<Map<ValueKey, Object>> pointsValues = new ArrayList<>();
        for (Point point : path.getPoints()) {
            Values values = point.getCsvData();
            Map<ValueKey, Object> pointValues = new LinkedHashMap<>();
            for (ValueKey key : values.getKeySet()) {
                keys.add(key);
                pointValues.put(key, getValue(values.get(key, ValueKind.current)));
            }
            pointsValues.add(pointValues);
        }

        map.put("keys", keys.stream().map(k -> "\"" + k + "\"").toList());
        map.put("points", pointsValues);

        write(map, file);
    }

    private String getValue(Value<?, ?> value) {
        if (value == null) {
            return null;
        } else {
            StorageUnit unit = value.unit();
            String result = unit.formatHuman(value.value());
            if (!(unit instanceof NumberUnit<?>)) {
                result = "\"" + result + "\"";
            }
            return result;
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
