package org.magic.JnxPrepare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class LayerComputer {

	public static File userHome = new File(System.getProperty("user.home"));
	public static File cachePropertiesFile = new File(userHome, "magic.ini");
	public static Properties cacheProperties;
	public static File cacheFolder = new File(userHome, "magic");

	public static WorkQueue workQueue = new WorkQueue(6);
	private static DocumentBuilder DOCUMENT_BUILDER;

	static {
		cacheProperties = new Properties();
		if (cachePropertiesFile.exists()) {
			try {
				cacheProperties.load(new FileInputStream(cachePropertiesFile));
				cacheFolder = new File(
						cacheProperties.getProperty("cache.folder"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!cacheFolder.exists()) {
			new File(cacheFolder, "tmp").mkdirs();
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DOCUMENT_BUILDER = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public static void main(String[] args) throws Exception {
		// validateTiles(new File(LayerComputer.cacheFolder, "magicCache"));
		downloadTiles();
	}

	private static void downloadTiles() throws FileNotFoundException,
			ParserConfigurationException, SAXException, IOException, Exception {

		// processMagic();
		processWMS();
	}

	private static void processMagic() throws Exception {

		FileInputStream fis = new FileInputStream("France.gpx");
		Document gpxFile = DOCUMENT_BUILDER.parse(fis);
		GPXProcessor gpxProcessor = new GPXProcessor(gpxFile, 5.0);
		gpxProcessor.parse();

		double minLon = gpxProcessor.getMinLon();
		double maxLon = gpxProcessor.getMaxLon();
		double minLat = gpxProcessor.getMinLat();
		double maxLat = gpxProcessor.getMaxLat();

		File[] layerFolders = cacheFolder.listFiles();
		for (File file : layerFolders) {
			if (!file.getName().startsWith(".")) {
				System.out.println(file.toString());
				MagicTiles tiles = new MagicTiles(
						MagicPower2MapSpace.INSTANCE_256, file);
				tiles.computeLayers(minLon, maxLon, minLat, maxLat,
						gpxProcessor);
				System.out.println("done " + file.toString());
			}
		}
	}

	private static void processWMS() throws FileNotFoundException,
			SAXException, IOException, Exception {
		FileInputStream fis;
		Document gpxFile;
		GPXProcessor gpxProcessor;
		double minLon;
		double maxLon;
		double minLat;
		double maxLat;
		// File departements = new File("D:\\viamichelin\\departements\\todo");
		File departements = new File(
				"/home/glandais/code/workspaces/workspace.osm/JnxPrepare/departements/todo");
		File[] listFiles = departements.listFiles();
		for (File file : listFiles) {
			if (!file.getName().startsWith(".")) {
				fis = new FileInputStream(file);
				gpxFile = DOCUMENT_BUILDER.parse(fis);
				gpxProcessor = new GPXProcessor(gpxFile, 3.0);
				gpxProcessor.parse();

				minLon = gpxProcessor.getMinLon();
				maxLon = gpxProcessor.getMaxLon();
				minLat = gpxProcessor.getMinLat();
				maxLat = gpxProcessor.getMaxLat();

				WMSLayer layer = new WMSLayer(15, "SCAN25");
				String setName = file.getName().replaceAll(".gpx", "");
				if (setName.indexOf('-') != -1) {
					setName = setName.substring(0, setName.indexOf('-') - 1);
				}
				layer.computeLayers(minLon, maxLon, minLat, maxLat,
						gpxProcessor, setName);
			}
		}
	}

	private static void validateTiles(File file) {
		if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
			try {
				ImageIO.read(file);
			} catch (Throwable e) {
				System.out.println(file.getAbsolutePath());
				file.deleteOnExit();
			}
		} else if (file.isFile()
				&& file.getName().toLowerCase().endsWith(".png")) {
			file.deleteOnExit();
		} else {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File file2 : files) {
					if (!file2.getName().startsWith(".")) {
						validateTiles(file2);
					}
				}
			}
		}
	}

}
