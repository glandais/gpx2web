package io.github.glandais;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

@RestController
public class FitController {

    private final GPXParser gpxParser;

    private final FitFileWriter fitFileWriter;

    private final GPXPathEnhancer gpxPathEnhancer;

    public FitController(final GPXParser gpxParser, final FitFileWriter fitFileWriter, final GPXPathEnhancer gpxPathEnhancer) {

        this.gpxParser = gpxParser;
        this.fitFileWriter = fitFileWriter;
        this.gpxPathEnhancer = gpxPathEnhancer;
    }

    @CrossOrigin(origins = "https://gabriel.landais.org")
    @PostMapping("/fit")
    public void handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "name", required = false) String name,
            HttpServletResponse response)
            throws Exception {
        List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            if (!StringUtils.isEmpty(name)) {
                gpxPath.setName(name);
            }
            gpxPathEnhancer.virtualize(gpxPath);

            GPXFilter.filterPointsDouglasPeucker(gpxPath);
            File tmp = File.createTempFile("fit", "tmp");
            fitFileWriter.writeFitFile(gpxPath, tmp);

            response.setContentType("application/fit");
            try (FileInputStream fis = new FileInputStream(tmp)) {
                IOUtils.copy(fis, response.getOutputStream());
            }
            Files.delete(tmp.toPath());

        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

}
