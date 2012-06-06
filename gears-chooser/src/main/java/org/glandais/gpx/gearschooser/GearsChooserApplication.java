package org.glandais.gpx.gearschooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glandais.gpx.braquet.Braquet;
import org.glandais.gpx.braquet.BraquetComputer;
import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.w3c.dom.Document;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window;

public class GearsChooserApplication extends Application implements Upload.Receiver, Upload.SucceededListener {

	private Window window;
	private File file;
	private String filename;

	@Override
	public void init() {
		window = new Window("Gears chooser");
		setMainWindow(window);
		Upload upload = new Upload("First upload a GPX of one of your rides", this);
		upload.addListener((Upload.SucceededListener) this);
		window.addComponent(upload);

	}

	public OutputStream receiveUpload(String filename, String mimeType) {
		try {
			this.filename = filename;
			file = File.createTempFile("gears", "gpx");
			return new FileOutputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void uploadSucceeded(SucceededEvent event) {

		try {
			StringWriter sw = new StringWriter();
			BufferedWriter writer = new BufferedWriter(sw);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document gpxFile = db.parse(file);

			List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false);

			final BraquetComputer braquetComputer = new BraquetComputer();
			braquetComputer.parseGPX(paths, writer);

			final Table table = new Table("Gears");
			table.addContainerProperty("Score", Long.class, null);
			table.addContainerProperty("Crankset", String.class, null);
			table.addContainerProperty("Chainrings", String.class, null);
			table.addContainerProperty("Cogset", String.class, null);
			table.addContainerProperty("Sprockets", String.class, null);
			table.addContainerProperty("Missing low", String.class, null);
			table.addContainerProperty("Missing high", String.class, null);
			table.addContainerProperty("Chainrings changes", Integer.class, null);
			table.addContainerProperty("Cogset changes", Integer.class, null);

			int i = 0;
			for (Braquet braquetDisp : braquetComputer.getBraquets()) {

				long score = 100 - Math.round(braquetDisp.getScore() * 100.0);

				table.addItem(new Object[] {

				score,

				braquetDisp.pedalier.name(),

				Arrays.toString(braquetDisp.pedalier.plateaux),

				braquetDisp.cassette.name(),

				Arrays.toString(braquetDisp.cassette.pignons),

				braquetDisp.timeMissingLow,

				braquetDisp.timeMissingHigh,

				braquetDisp.pedalierChanges,

				braquetDisp.cassetteChanges

				}, new Integer(i));
				i++;

				writer.newLine();
				writer.append(braquetDisp.toString());
			}

			table.setSelectable(true);
			table.setImmediate(true);

			table.addListener(new Property.ValueChangeListener() {
				public void valueChange(ValueChangeEvent event) {
					Integer index = (Integer) table.getValue();
					Braquet braquet = braquetComputer.getBraquets().get(index);
					displayBraquetDetails(braquet);
				}

			});

			// table.setWidth("100%");
			// table.setHeight("100%");
			Window newWindow = new Window(filename);
			// VerticalLayout layout = new VerticalLayout();
			// layout.setSizeFull();
			newWindow.addComponent(table);
			// layout.addComponent(table);

			newWindow.getContent().setSizeUndefined();
			// newWindow.addComponent(table);
			getMainWindow().addWindow(newWindow);

			/*
			Braquet braquet = new Braquet(braquets.get(0).pedalier, braquets.get(0).cassette);
			for (GPXPath gpxPath : paths) {
				gpxPath.tryBraquets(Collections.singletonList(braquet), true, writer);
			}
			*/

			// chart.removeSeries("gpx");
			// DateTimeSeries series = new DateTimeSeries("gpx");
			// DateTimePoint point = new DateTimePoint(series, new Date(), 1);
			// series.addPoint(point);
			// point = new DateTimePoint(series, new Date(), 2);
			// chart.addSeries(series);

			// textArea.setValue(sw.toString());
		} catch (Exception e) {
			// textArea.setValue(e.toString());
			e.printStackTrace();
		}
	}

	private void displayBraquetDetails(Braquet braquet) {
		Window newWindow = new Window(filename + " - " + braquet.pedalier.name() + " - " + braquet.cassette.name());
		TextArea textArea = new TextArea();
		StringBuilder sb = new StringBuilder();
		for (String line : braquet.history) {
			sb.append(line).append("\r\n");
		}
		textArea.setValue(sb.toString());
		textArea.setWidth("800px");
		textArea.setHeight("600px");
		newWindow.addComponent(textArea);
		newWindow.getContent().setSizeUndefined();
		getMainWindow().addWindow(newWindow);
	}
}
