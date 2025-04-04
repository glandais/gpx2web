package io.github.glandais.gpx.io.write;

import io.github.glandais.gpx.data.GPXPath;
import java.io.File;
import java.io.IOException;

public interface FileExporter {
    void writeGPXPath(GPXPath path, File file) throws IOException;
}
