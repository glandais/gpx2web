package org.glandais.garmin.wms2jnx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class App {

	private static DocumentBuilder DOCUMENT_BUILDER;

	static {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DOCUMENT_BUILDER = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		Options options = getOptions();

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			String wmsURL = cmd.getOptionValue("wms");
			File gpxFile = (File) cmd.getParsedOptionValue("gpx");
			if (!gpxFile.exists()) {
				throw new ParseException("File not fund "
						+ gpxFile.getAbsolutePath());
			}

			File destFolder = (File) cmd.getParsedOptionValue("dest");
			if (!destFolder.exists()) {
				destFolder.mkdirs();
			}

			double bufferDistance = 3.0;
			if (cmd.hasOption("buffer")) {
				bufferDistance = ((Number) cmd.getParsedOptionValue("buffer"))
						.doubleValue();
			}

			int zoom = ((Number) cmd.getParsedOptionValue("zoom")).intValue();
			String layers = cmd.getOptionValue("layers");

			try {
				processWms(wmsURL, gpxFile, destFolder, bufferDistance, zoom,
						layers);
			} catch (Exception e) {
				System.err.println("Failed to download tiles");
				e.printStackTrace();
			}

		} catch (ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options);
			return;
		}

		// http://mapdmz.brgm.fr/cgi-bin/mapserv?map=/carto/infoterre/mapFiles/scan.map&VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap
		// "SCAN25",
		// 15

	}

	private static void processWms(String wmsURL, File gpxFile,
			File destFolder, double bufferDistance, int zoom, String layers)
			throws Exception {
		WMSLayer wmsLayer = new WMSLayer(wmsURL, layers, zoom, destFolder);

		try {
			FileInputStream fis = new FileInputStream(gpxFile);
			Document gpxDocument = DOCUMENT_BUILDER.parse(fis);
			GPXProcessor gpxProcessor = new GPXProcessor(gpxDocument,
					bufferDistance);
			gpxProcessor.parse();

			String setName = gpxFile.getName().toLowerCase()
					.replaceAll(".gpx", "");
			if (setName.indexOf('-') != -1) {
				setName = setName.substring(0, setName.indexOf('-') - 1);
			}
			wmsLayer.computeLayers(gpxProcessor, setName);
		} finally {
			wmsLayer.done();
		}

	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(160, "wms2jnx", null, options, null, true);
	}

	private static Options getOptions() {
		Options options = new Options();

		Option wmsOption = new Option("wms", true,
				"WMS URL (like http://server/path?VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap)");
		wmsOption.setRequired(true);
		wmsOption.setArgName("WMS url");
		wmsOption.setType(PatternOptionBuilder.URL_VALUE);
		options.addOption(wmsOption);

		Option layersOption = new Option("layers", true, "WMS layers");
		layersOption.setRequired(true);
		layersOption.setArgName("layers");
		layersOption.setType(PatternOptionBuilder.STRING_VALUE);
		options.addOption(layersOption);

		Option zoomOption = new Option("zoom", true,
				"Zoom level (like in MOBAC or Google Maps)");
		zoomOption.setRequired(true);
		zoomOption.setArgName("zoom");
		zoomOption.setType(PatternOptionBuilder.NUMBER_VALUE);
		options.addOption(zoomOption);

		Option gpxOption = new Option("gpx", true,
				"GPX file with paths around the needed zones");
		gpxOption.setRequired(true);
		gpxOption.setArgName("GPX file");
		gpxOption.setType(PatternOptionBuilder.EXISTING_FILE_VALUE);
		options.addOption(gpxOption);

		Option destFolder = new Option("dest", true, "Destination folder");
		destFolder.setRequired(true);
		destFolder.setArgName("dest folder");
		destFolder.setType(PatternOptionBuilder.FILE_VALUE);
		options.addOption(destFolder);

		Option bufferOption = new Option("buffer", true,
				"Buffer distance around the path, in km (default : 3.0km)");
		bufferOption.setRequired(false);
		bufferOption.setArgName("buffer");
		bufferOption.setType(PatternOptionBuilder.NUMBER_VALUE);
		options.addOption(bufferOption);

		return options;
	}

}
