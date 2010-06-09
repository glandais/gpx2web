package org.glandais.gpx.elevation.fixer;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) throws Exception {
		File fin = new File(args[0]);
		File fout = new File(args[1]);
		File[] listFiles = fin.listFiles();
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(100, 170);
		for (File file : listFiles) {
			if (!file.getName().startsWith(".")
					&& file.getName().toLowerCase().endsWith(".gpx")) {
				try {
					processfile(file, fout, bikeTimeEval);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void processfile(File file, File fout,
			GPXBikeTimeEval bikeTimeEval) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);

		GPXProcessor processor = new GPXProcessor(gpxFile, bikeTimeEval);
		processor.parse();
		processor.postProcess();		
		System.out.println(file.getName());
		processor.showStats();

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		File fileOut = new File(fout.getAbsolutePath() + "/" + file.getName());
		Source xmlSource = new DOMSource(processor.getGpxDocument());
		Result outputTarget = new StreamResult(fileOut);
		transformer.transform(xmlSource, outputTarget);

		processor.createCharts(fout.getAbsolutePath() + "/" + file.getName(),
				10);
	}

}
