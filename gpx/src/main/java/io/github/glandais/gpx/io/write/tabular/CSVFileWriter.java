package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.Value;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
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
public class CSVFileWriter extends TabularFileWriter {

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        FileWriter fw = new FileWriter(file);

        write(path, columns -> writeHeader(fw, columns),
                (i, values) -> newLine(fw),
                (row, col, values, key, value) -> writeCell(fw, value));

        fw.close();
    }

    @SneakyThrows
    private void writeHeader(FileWriter fw, Map<ValueKey, Set<ValueKind>> columns) {
        for (Map.Entry<ValueKey, Set<ValueKind>> column : columns.entrySet()) {
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
    private void writeCell(FileWriter fw, Value<?, ?> value) {
        String cell;
        if (value == null) {
            cell = "";
        } else {
            StorageUnit unit = value.unit();
            cell = unit.formatHuman(value.value());
        }
        fw.write(cell);
        fw.write(";");
    }

}
