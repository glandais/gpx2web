package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.gpx.storage.unit.StorageUnit;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@Singleton
public class CSVFileWriter extends TabularFileWriter {

    public void writeCsvFile(GPXPath path, File file) throws IOException {
        FileWriter fw = new FileWriter(file);

        write(path, columns -> writeHeader(fw, columns),
                (i, values) -> newLine(fw),
                (row, col, values, key, value) -> writeCell(fw, value));

        fw.close();
    }

    @SneakyThrows
    private void writeHeader(FileWriter fw, Map<String, Set<ValueKind>> columns) {
        for (Map.Entry<String, Set<ValueKind>> column : columns.entrySet()) {
            for (ValueKind valueKind : column.getValue()) {
                fw.write(column.getKey() + "-" + valueKind + ";");
            }
        }
    }

    @SneakyThrows
    private void newLine(FileWriter fw) {
        fw.write("\n");
    }

    @SneakyThrows
    private void writeCell(FileWriter fw, io.github.glandais.gpx.storage.Value<?, ?> value) {
        String cell;
        if (value == null) {
            cell = "";
        } else {
            StorageUnit unit = value.getUnit();
            cell = unit.formatHuman(value.getValue());
        }
        fw.write(cell);
        fw.write(";");
    }

}
