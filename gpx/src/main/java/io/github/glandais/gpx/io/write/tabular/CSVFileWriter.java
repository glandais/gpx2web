package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeyKind;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.data.values.unit.Unit;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@Singleton
public class CSVFileWriter extends TabularFileWriter<FileWriter> {

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        FileWriter fw = new FileWriter(file);

        write(fw, path, this::writeHeaders, this::initRow, this::writeCell);

        fw.close();
    }

    @SneakyThrows
    private void writeHeaders(FileWriter fileWriter, Map<PropertyKey<?, ?>, Set<ValueKind>> propertyKeySetMap) {
        for (Map.Entry<PropertyKey<?, ?>, Set<ValueKind>> column : propertyKeySetMap.entrySet()) {
            for (ValueKind valueKind : column.getValue()) {
                fileWriter.write(column.getKey() + "-" + valueKind + ";");
            }
        }
    }

    @SneakyThrows
    private void initRow(FileWriter fileWriter, Integer integer, Point point) {
        fileWriter.write("\n");
    }

    @SneakyThrows
    private <S, U extends Unit<S>> void writeCell(FileWriter fileWriter, Integer integer, Integer integer1, Point point, PropertyKeyKind<S, U> propertyKey) {
        S value = point.get(propertyKey.propertyKey(), propertyKey.kind());
        String cell;
        if (value == null) {
            cell = "";
        } else {
            cell = propertyKey.propertyKey().formatHuman(value);
        }
        writeNext(fileWriter, cell);
    }

    @SneakyThrows
    private static void writeNext(FileWriter fw, String cell) {
        fw.write(cell);
        fw.write(";");
    }


}
