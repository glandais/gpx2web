package org.glandais.gpx.gearschooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glandais.gpx.braquet.Braquet;
import org.glandais.gpx.braquet.BraquetComputer;
import org.glandais.gpx.braquet.BraquetProgress;
import org.glandais.gpx.braquet.PointBraquet;
import org.glandais.gpx.elevation.fixer.GPXParser;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.w3c.dom.Document;

import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class GPXAnalyzer implements Upload.Receiver, Upload.StartedListener, Upload.ProgressListener,
		Upload.SucceededListener, Upload.FailedListener, Upload.FinishedListener, GearsTab {

	private static final String SENDING_FILE_TO_SERVER = "Sending file to server...";

	private static final long serialVersionUID = 3052092648271721431L;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	private GearsApplication application;

	private File file;
	private String filename;
	private ProgressIndicator progressIndicator = null;
	private Label progressLabel = null;
	private Slider[] sliders = new Slider[4];

	private HorizontalLayout uploadLayout;

	private Upload upload;

	private double[] getRatios() {
		double[] result = new double[4];
		result[Braquet.INDEX_CRANKSET_CHANGES] = (Double) sliders[Braquet.INDEX_CRANKSET_CHANGES].getValue();
		result[Braquet.INDEX_COGSET_CHANGES] = (Double) sliders[Braquet.INDEX_COGSET_CHANGES].getValue();
		result[Braquet.INDEX_LOW_RPM] = (Double) sliders[Braquet.INDEX_LOW_RPM].getValue();
		result[Braquet.INDEX_HIGH_RPM] = (Double) sliders[Braquet.INDEX_HIGH_RPM].getValue();
		double total = 0;
		for (double d : result) {
			total = total + d;
		}
		if (total == 0) {
			result[Braquet.INDEX_CRANKSET_CHANGES] = 1.0;
		}
		return result;
	}

	public void setApplication(GearsApplication application) {
		this.application = application;
	}

	public String getCaption() {
		return "Analyze GPX";
	}

	public VerticalLayout getComponent() {
		VerticalLayout globalLayout = new VerticalLayout();

		uploadLayout = new HorizontalLayout();

		Label label = new Label("First upload a GPX of one of your rides : ");
		uploadLayout.addComponent(label);
		uploadLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		upload = new Upload(null, this);
		upload.setImmediate(true);
		upload.setButtonCaption("Select GPX");
		upload.addListener((Upload.StartedListener) this);
		upload.addListener((Upload.ProgressListener) this);
		upload.addListener((Upload.SucceededListener) this);
		upload.addListener((Upload.FailedListener) this);
		upload.addListener((Upload.FinishedListener) this);
		uploadLayout.addComponent(upload);
		uploadLayout.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);

		globalLayout.addComponent(uploadLayout);

		Panel panel = new Panel("Score ratios");
		panel.setSizeFull();
		VerticalLayout slidersLayout = new VerticalLayout();

		sliders[Braquet.INDEX_CRANKSET_CHANGES] = new Slider("Crankset changes", 0, 100);
		sliders[Braquet.INDEX_COGSET_CHANGES] = new Slider("Cogset changes", 0, 100);
		sliders[Braquet.INDEX_LOW_RPM] = new Slider("Avoid low rpm", 0, 100);
		sliders[Braquet.INDEX_HIGH_RPM] = new Slider("Avoid high rpm", 0, 100);
		try {
			sliders[Braquet.INDEX_CRANKSET_CHANGES].setValue(50.0);
			sliders[Braquet.INDEX_COGSET_CHANGES].setValue(20.0);
			sliders[Braquet.INDEX_LOW_RPM].setValue(100.0);
			sliders[Braquet.INDEX_HIGH_RPM].setValue(50.0);
		} catch (ValueOutOfBoundsException e) {
			e.printStackTrace();
		}

		Button resetButton = new Button("Reset");
		resetButton.addListener(new ClickListener() {
			public void buttonClick(final ClickEvent event) {
				try {
					sliders[Braquet.INDEX_CRANKSET_CHANGES].setValue(50.0);
					sliders[Braquet.INDEX_COGSET_CHANGES].setValue(20.0);
					sliders[Braquet.INDEX_LOW_RPM].setValue(100.0);
					sliders[Braquet.INDEX_HIGH_RPM].setValue(50.0);
				} catch (ValueOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		});
		slidersLayout.addComponent(resetButton);
		for (Slider slider : sliders) {
			slider.setWidth("300px");
			slider.setOrientation(Slider.ORIENTATION_HORIZONTAL);
			slidersLayout.addComponent(slider);
		}

		// slidersLayout.setSizeFull();
		panel.setContent(slidersLayout);
		globalLayout.addComponent(panel);
		globalLayout.setExpandRatio(panel, 1.0f);

		globalLayout.setSizeFull();
		return globalLayout;
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

	public void uploadStarted(StartedEvent event) {
		progressIndicator = new ProgressIndicator(new Float(0.0));
		progressIndicator.setVisible(true);
		progressLabel = new Label(SENDING_FILE_TO_SERVER);
		uploadLayout.addComponent(progressIndicator);
		uploadLayout.addComponent(progressLabel);
		uploadLayout.setComponentAlignment(progressIndicator, Alignment.MIDDLE_LEFT);
		uploadLayout.setComponentAlignment(progressLabel, Alignment.MIDDLE_LEFT);
		upload.setEnabled(false);
	}

	public void updateProgress(long readBytes, long contentLength) {
		if (progressIndicator != null) {
			float fvalue = (1.0f * readBytes) / (1.0f * contentLength);
			long percent = Math.round(fvalue * 100.0);
			fvalue = (float) Math.min(Math.max(0.0, fvalue), 1.0);
			progressIndicator.setValue(fvalue / 2.0f);
			progressLabel.getPropertyDataSource().setValue(SENDING_FILE_TO_SERVER + " " + percent + "%");
		}
	}

	public void uploadSucceeded(SucceededEvent event) {
		upload.setEnabled(false);
		class WorkThread extends Thread {
			public void run() {
				try {
					progressLabel.getPropertyDataSource().setValue("Processing " + filename);

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document gpxFile = db.parse(file);

					List<GPXPath> paths = GPXParser.parsePaths(gpxFile, false);

					final BraquetComputer braquetComputer = new BraquetComputer();
					braquetComputer.parseGPX(paths, new BraquetProgress() {
						public void progress(int i, int size) {
							int step = size / 15;
							if (i % step == 0) {
								Float fvalue = (i * 1.0f) / (size * 1.0f);
								long percent = Math.round(fvalue * 100.0);
								fvalue = (float) Math.min(Math.max(0.0, fvalue), 1.0);
								progressIndicator.setValue(0.5f + fvalue / 2.0f);
								progressLabel.getPropertyDataSource().setValue(
										"Processing " + filename + " " + percent + "%");
							}
						}
					});

					final Table table = new Table();
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

						long score = 100 - Math.round(braquetDisp.getScore(getRatios()) * 100.0);

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

					}

					table.setSelectable(true);
					table.setImmediate(true);

					table.setSortContainerPropertyId("Score");
					table.setSortAscending(false);

					table.addListener(new Property.ValueChangeListener() {
						private static final long serialVersionUID = 6501968342912951441L;

						public void valueChange(ValueChangeEvent event) {
							Integer index = (Integer) table.getValue();
							Braquet braquet = braquetComputer.getBraquets().get(index);
							displayBraquetDetails(braquet);
						}

					});

					Window newWindow = new Window(filename);
					table.setSizeFull();

					VerticalLayout verticalLayout = new VerticalLayout();
					verticalLayout.setSizeFull();

					final Table tableExported = table;

					HorizontalLayout horizontalLayout = new HorizontalLayout();

					Button refreshButton = new Button("Refresh scores");
					refreshButton.addListener(new ClickListener() {
						private static final long serialVersionUID = -7431482683590698194L;

						public void buttonClick(final ClickEvent event) {
							Collection<Integer> itemIds = (Collection<Integer>) table.getItemIds();
							for (Integer itemId : itemIds) {
								Braquet braquet = braquetComputer.getBraquets().get(itemId);
								Long score = 100 - Math.round(braquet.getScore(getRatios()) * 100.0);
								Property itemProperty = table.getItem(itemId).getItemProperty("Score");
								itemProperty.setValue(score);
							}
						}
					});
					horizontalLayout.addComponent(refreshButton);

					Button excelExportButton = new Button("Export to Excel");
					excelExportButton.addListener(new ClickListener() {
						private static final long serialVersionUID = -7431482683590698194L;

						public void buttonClick(final ClickEvent event) {
							ExcelExport excelExport = new ExcelExport(tableExported);
							excelExport.excludeCollapsedColumns();
							excelExport.setExportFileName("braquets" + "-" + DATE_FORMAT.format(new Date()) + ".xls");
							excelExport.export();
						}
					});
					horizontalLayout.addComponent(excelExportButton);

					verticalLayout.addComponent(horizontalLayout);
					verticalLayout.addComponent(table);
					verticalLayout.setExpandRatio(table, 1.0f);

					newWindow.setContent(verticalLayout);

					newWindow.setWidth("800px");
					application.getMainWindow().addWindow(newWindow);

				} catch (Exception e) {
					application.getMainWindow().showNotification("Processing failed (" + e.getMessage() + ")",
							Window.Notification.TYPE_ERROR_MESSAGE);
					e.printStackTrace();
				}
				upload.setEnabled(true);
				if (progressIndicator != null) {
					uploadLayout.removeComponent(progressIndicator);
					progressIndicator = null;
				}
				if (progressLabel != null) {
					uploadLayout.removeComponent(progressLabel);
					progressLabel = null;
				}
			}
		}
		final WorkThread thread = new WorkThread();
		thread.start();
	}

	public void uploadFailed(FailedEvent event) {
		application.getMainWindow().showNotification("Upload failed", Window.Notification.TYPE_ERROR_MESSAGE);
		if (progressIndicator != null) {
			uploadLayout.removeComponent(progressIndicator);
			progressIndicator = null;
		}
		upload.setEnabled(true);
	}

	public void uploadFinished(FinishedEvent event) {

	}

	private void displayBraquetDetails(Braquet braquet) {
		final Table table = new Table();
		table.addContainerProperty("Time", Date.class, null);
		table.addContainerProperty("Distance", Double.class, null);
		table.addContainerProperty("Speed", Double.class, null);
		table.addContainerProperty("Ring", Integer.class, null);
		table.addContainerProperty("Cog", Integer.class, null);
		table.addContainerProperty("Rpm", Long.class, null);
		table.addContainerProperty("Last shift", Double.class, null);

		int i = 0;
		for (PointBraquet pointBraquet : braquet.history) {

			Double dist = Math.round(pointBraquet.getDist() * 100.0) / 100.0;
			Double speed = Math.round(pointBraquet.getSpeed() * 10.0) / 10.0;
			Long rpm = Math.round(pointBraquet.getRpm());
			Double lastShift = Math.round(pointBraquet.getTimeSinceShift() / 100.0) / 10.0;

			table.addItem(new Object[] {

			new Date(pointBraquet.getPoint().getTime()),

			dist,

			speed,

			pointBraquet.getBraquet().pedalier.plateaux[pointBraquet.getiPlateau()],

			pointBraquet.getBraquet().cassette.pignons[pointBraquet.getiPignon()],

			rpm,

			lastShift

			}, new Integer(i));
			i++;
		}

		application.createWindow(table, "braquet", filename + " - " + braquet.pedalier.name() + " - "
				+ braquet.cassette.name());
	}

}
