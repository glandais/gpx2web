package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class GPXAmplifier {

	public static void main(String[] args) throws Exception {
		File fin = new File(args[0]);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(fin);

		List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false);
		for (GPXPath gpxPath : paths) {
			gpxPath.filterPoints();
			gpxPath.computeArrays();
			gpxPath.amplify();
		}
		GPXFileWriter.writeGpxFile(paths, new File(args[1]));
	}

}
