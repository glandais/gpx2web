package io.github.glandais;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class FilesMixin {

    @CommandLine.Parameters(paramLabel = "FILE", description = "Files or folders to process")
    private List<File> input = new ArrayList<>();

    protected List<File> gpxFiles = new ArrayList<>();

    @SneakyThrows
    public void processFiles(GPXParser gpxParser, BiConsumer<GPXPath, File> gpxAndFolderConsumer) {

        if (input.isEmpty()) {
            CommandLine.usage(this, System.out);
            System.exit(0);
        }
        gpxFiles = new ArrayList<>();
        input.stream().forEach(this::checkFile);

        for (File gpxFile : gpxFiles) {
            log.info("Processing GPX {}", gpxFile.toString());

            List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
            File gpxFolder = new File(gpxFile.getParentFile(), gpxFile.getName()
                    .replace(".gpx", ""));
            gpxFolder.mkdirs();
            for (GPXPath path : paths) {
                log.info("Processing path {}", path.getName());

                File pathFolder = new File(gpxFolder, path.getName());
                pathFolder.mkdirs();

                gpxAndFolderConsumer.accept(path, pathFolder);

                log.info("Processed path {}", path.getName());
            }
            log.info("Processed GPX {}", gpxFile.toString());
        }

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
