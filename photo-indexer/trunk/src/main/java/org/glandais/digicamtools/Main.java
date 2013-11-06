package org.glandais.digicamtools;

import java.io.File;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ("-import".equals(args[0])) {
			Importer.main(args);
		} else if ("-export".equals(args[0])) {
			new ExporterFlickr().process(new File(args[1]));
		} else if ("-convert".equals(args[0])) {
			Converter.main(args);
		}
	}

}
