/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.magic.JnxPrepare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class LayerComputer {

	public static File userHome = new File(System.getProperty("user.home"));
	public static File cachePropertiesFile = new File(userHome, "magic.ini");
	public static Properties cacheProperties;
	public static File cacheFolder = new File(userHome, "magic");

	static {
		cacheProperties = new Properties();
		if (cachePropertiesFile.exists()) {
			try {
				cacheProperties.load(new FileInputStream(cachePropertiesFile));
				cacheFolder = new File(cacheProperties
						.getProperty("cache.folder"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!cacheFolder.exists()) {
			new File(cacheFolder, "tmp").mkdirs();
		}
	}

	public static void main(String[] args) throws Exception {

		// TRUE in a first phase
		// boolean downloading = true;
		// Then delete all tiles without data (blank, ...) manually
		// And compute tiles for specified bbox
		// double minLon = 8.3;
		// double maxLon = 9.6;
		// double minLat = 41.3;
		// double maxLat = 43.1;

		Contour contour = null;

		FileInputStream fis = new FileInputStream("France.gpx");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(fis);
		GPXProcessor gpxProcessor = new GPXProcessor(gpxFile);
		gpxProcessor.parse();

		contour = gpxProcessor;
		double minLon = gpxProcessor.getMinLon();
		double maxLon = gpxProcessor.getMaxLon();
		double minLat = gpxProcessor.getMinLat();
		double maxLat = gpxProcessor.getMaxLat();

		// minLon = 0;
		// maxLon = 0.1;
		// minLat = 47;
		// maxLat = 47.1;

		File[] layerFolders = cacheFolder.listFiles();
		for (File file : layerFolders) {
			if (!file.getName().startsWith(".")) {
				// processLayer(file, minLon, maxLon, minLat, maxLat, contour);
			}
		}

		// WMSLayer layer = new WMSLayer(13, "SCAN250_IGN");
		// layer.computeLayers(minLon, maxLon, minLat, maxLat, contour);

		WMSLayer layer = new WMSLayer(15, "SCAN25");
		layer.computeLayers(minLon, maxLon, minLat, maxLat, contour);

	}

	private static void processLayer(File file, double minLon, double maxLon,
			double minLat, double maxLat, Contour contour) {
		System.out.println(file.toString());
		MagicTiles tiles = new MagicTiles(MagicPower2MapSpace.INSTANCE_256,
				file);
		tiles.computeLayers(minLon, maxLon, minLat, maxLat, contour);
	}

}
