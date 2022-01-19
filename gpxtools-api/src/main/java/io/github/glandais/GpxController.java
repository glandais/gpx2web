package io.github.glandais;

import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

@RestController
public class GpxController {

    private final GPXParser gpxParser;

    private final GPXPathEnhancer gpxPathEnhancer;

    private final GPXFileWriter gpxFileWriter;

    private final GPXDataComputer gpxDataComputer;

    private final GPXElevationFixer gpxElevationFixer;

    public GpxController(final GPXParser gpxParser,
                         final GPXPathEnhancer gpxPathEnhancer,
                         final GPXFileWriter gpxFileWriter,
                         final GPXDataComputer gpxDataComputer,
                         final GPXElevationFixer gpxElevationFixer) {

        this.gpxParser = gpxParser;
        this.gpxPathEnhancer = gpxPathEnhancer;
        this.gpxFileWriter = gpxFileWriter;
        this.gpxDataComputer = gpxDataComputer;
        this.gpxElevationFixer = gpxElevationFixer;
    }

    @CrossOrigin(origins = "https://gabriel.landais.org")
    @PostMapping("/simplify")
    public void simplify(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "name", required = false) String name,
            HttpServletResponse response) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            if (StringUtils.hasText(name)) {
                gpxPath.setName(name);
            }
            gpxPathEnhancer.virtualize(gpxPath);
            GPXFilter.filterPointsDouglasPeucker(gpxPath);

            File tmp = File.createTempFile("gpx", "tmp");
            gpxFileWriter.writeGpxFile(paths, tmp);

            response.setContentType("application/gpx");
            try (FileInputStream fis = new FileInputStream(tmp)) {
                IOUtils.copy(fis, response.getOutputStream());
            }
            Files.delete(tmp.toPath());
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

    @CrossOrigin(origins = "https://gabriel.landais.org")
    @PostMapping("/gpxinfo")
    public GPXInfo gpxinfo(@RequestParam("file") MultipartFile file) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            gpxElevationFixer.fixElevation(gpxPath);
            float dist = Math.round(gpxPath.getDist() / 100.0) / 10.0f;

            return new GPXInfo(dist, (int) Math.round(gpxPath.getTotalElevation()), (int) Math.round(gpxPath.getTotalElevationNegative()),
                    gpxDataComputer.getWind(gpxPath), gpxDataComputer.isCrossing(gpxPath));
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

    @CrossOrigin(origins = "https://gabriel.landais.org")
    @PostMapping("/geojson")
    public void geojson(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            gpxPathEnhancer.virtualize(gpxPath);
            GPXFilter.filterPointsDouglasPeucker(gpxPath);
            try (Writer w = new OutputStreamWriter(response.getOutputStream()); BufferedWriter bw = new BufferedWriter(w)) {
                bw.write("{");
                bw.newLine();
                bw.write("  \"type\": \"LineString\",");
                bw.newLine();
                bw.write("  \"coordinates\": [");
                bw.newLine();
                for (int i = 0;
                     i < gpxPath.getPoints()
                             .size();
                     i++) {
                    Point point = gpxPath.getPoints()
                            .get(i);
                    bw.write("    [");
                    bw.write(Float.toString((float) point.getLonDeg()));
                    bw.write(", ");
                    bw.write(Float.toString((float) point.getLatDeg()));
                    bw.write("]");
                    if (i != gpxPath.getPoints()
                            .size() - 1) {
                        bw.write(",");
                    }
                    bw.newLine();
                }
                bw.write("	]");
                bw.newLine();
                bw.write("}");
                bw.newLine();
            }

        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }
}
