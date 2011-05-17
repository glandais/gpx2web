package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
		GPXBikeTimeEval bikeTimeEval = new GPXBikeTimeEval(100, 130, 130);

		// GregorianCalendar[] starts = getTmpStarts();
		GregorianCalendar[] starts = getCorsicaStarts();
		// Calendar instance = Calendar.getInstance();
		// instance.add(Calendar.DATE, 1);
		// instance.set(Calendar.HOUR_OF_DAY, 5);
		// instance.set(Calendar.MINUTE, 30);
		// instance.set(Calendar.SECOND, 0);
		// instance.set(Calendar.MILLISECOND, 0);
		// Calendar[] starts = new Calendar[] { instance };

		bikeTimeEval.setStarts(starts);

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

	private static GregorianCalendar[] getCorsicaStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[1];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 21);
		start.set(Calendar.MONTH, 4); // Jan. = 0!
		start.set(Calendar.YEAR, 2011);
		start.set(Calendar.HOUR_OF_DAY, 7);
		start.set(Calendar.MINUTE, 15);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		return starts;
	}

	private static GregorianCalendar[] getTmpStarts() {
		GregorianCalendar[] starts = new GregorianCalendar[2];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 30);
		start.set(Calendar.MONTH, 3); // Jan. = 0!
		start.set(Calendar.YEAR, 2011);
		start.set(Calendar.HOUR_OF_DAY, 4);
		start.set(Calendar.MINUTE, 30);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 30);
		start.set(Calendar.MONTH, 3); // Jan. = 0!
		start.set(Calendar.YEAR, 2011);
		start.set(Calendar.HOUR_OF_DAY, 6);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[1] = start;
		return starts;
	}

	private static GregorianCalendar[] getPBP2011Starts() {
		GregorianCalendar[] starts = new GregorianCalendar[2];

		GregorianCalendar start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 20);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2011);
		start.set(Calendar.HOUR_OF_DAY, 20);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[0] = start;

		start = new GregorianCalendar();
		start.set(Calendar.DAY_OF_MONTH, 22);
		start.set(Calendar.MONTH, 7); // Jan. = 0!
		start.set(Calendar.YEAR, 2011);
		start.set(Calendar.HOUR_OF_DAY, 4);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		starts[1] = start;
		return starts;
	}

	private static void processfile(File file, File fout,
			GPXBikeTimeEval bikeTimeEval) throws Exception {
		StringBuilder timeSheet = new StringBuilder();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(file);

		GPXProcessor processor = new GPXProcessor(gpxFile, bikeTimeEval);
		processor.parse();
		processor.postProcess(timeSheet);
		System.out.println(file.getName());
		processor.showStats();

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		String pathName = fout.getAbsolutePath() + "/" + file.getName();
		File fileOut = new File(pathName);
		Source xmlSource = new DOMSource(processor.getGpxDocument());
		Result outputTarget = new StreamResult(fileOut);
		transformer.transform(xmlSource, outputTarget);

		processor
				.createCharts(fout.getAbsolutePath() + "/" + file.getName(), 0);

		File outFile = new File(pathName + ".txt");
		FileWriter out = new FileWriter(outFile);
		out.write(timeSheet.toString().replace('.', ','));
		out.close();
	}

}
