package io.github.glandais;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class FilesMixin {

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output folder")
    protected File output = new File("output");

    @CommandLine.Parameters(paramLabel = "FILE", description = "Files or folders to process")
    private List<File> input = new ArrayList<>();

    protected List<File> gpxFiles = new ArrayList<>();

    @SneakyThrows
    public void processFiles(GPXFileReader gpxFileReader, BiConsumer<GPXPath, File> gpxAndFolderConsumer) {

        if (input.isEmpty()) {
            CommandLine.usage(this, System.out);
            System.exit(0);
        }
        gpxFiles = new ArrayList<>();
        input.forEach(this::checkFile);

        for (File gpxFile : gpxFiles) {
            log.info("Processing GPX {}", gpxFile.toString());

            GPX gpx = gpxFileReader.parseGpx(gpxFile);
            File gpxFolder = new File(output, gpxFile.getName()
                    .replace(".gpx", ""));
            gpxFolder.mkdirs();
            for (GPXPath path : gpx.paths()) {
                log.info("Processing path {}", path.getName());

                File pathFolder = new File(gpxFolder, path.getName());
                pathFolder.mkdirs();
                log.info("Exporting to {}", pathFolder.getCanonicalPath());

                gpxAndFolderConsumer.accept(path, pathFolder);

                log.info("Processed path {}", path.getName());
            }
            log.info("Processed GPX {}", gpxFile);
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
