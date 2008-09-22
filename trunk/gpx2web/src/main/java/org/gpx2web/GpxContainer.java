package org.gpx2web;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.gpx2web.binding.gpx.GpxType;

public class GpxContainer {

	private GpxType gpx;

	protected static Logger logger = Logger.getAnonymousLogger();

	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("org.gpx2web.binding.gpx");
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public GpxContainer(String gpxFile) throws Gpx2WebException {
		try {
			FileInputStream fis = new FileInputStream(gpxFile);
			gpx = (GpxType) unmarshaller.unmarshal(fis);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new Gpx2WebException(e);
		}
	}

	public GpxType getGpx() {
		return gpx;
	}

}
