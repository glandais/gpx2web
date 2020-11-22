package io.github.glandais;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class FilesMixin {

    @CommandLine.Parameters(paramLabel = "FILE", description = "Files or folders to process")
    private List<File> input = new ArrayList<>();

    protected List<File> gpxFiles = new ArrayList<>();

    public void initFiles() {

        if (input.isEmpty()) {
            CommandLine.usage(this, System.out);
            System.exit(0);
        }
        gpxFiles = new ArrayList<>();
        input.stream().forEach(this::checkFile);

    }

    private void checkFile(File fin) {
        if (!fin.exists()) {
            log.warn("{} doesn't exist", fin);
        } else if (fin.isFile()) {
            if (fin.getName().toLowerCase().endsWith(".gpx")) {
                gpxFiles.add(fin);
                log.info("Will process {}", fin);
            } else {
                log.warn("{} is not a GPX", fin);
            }
        } else if (fin.isDirectory()) {
            File[] listFiles = fin.listFiles();
            for (File child : listFiles) {
                checkFile(child);
            }
        }
    }

}
