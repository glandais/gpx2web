import java.applet.Applet;
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import netscape.javascript.JSObject;

import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;

public class MagicPopup extends Frame implements ActionListener {

	private static final long serialVersionUID = -4533004192385246689L;

	private static final int IO_BUFFER_SIZE = 4 * 1024;

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

	private JSONParser jsonParser = new JSONParser();
	private Random random = new Random();
	private DecimalFormat df = null;

	private Applet applet;
	private Button openFileButton;
	private Button kmzButton;
	private Button cacheButton;
	private TextField gpxUrlField;
	private Button openUrlButton;
	private Label infoLabel;

	public MagicPopup(Applet applet) {
		super();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		this.df = new DecimalFormat("0.##############", symbols);
		this.applet = applet;
		setUpUi();
	}

	private void setUpUi() {
		setLayout(new GridLayout(4, 1));
		setTitle("Magic");

		infoLabel = new Label("");
		add(infoLabel);

		Panel p = new Panel(new GridLayout(1, 2));
		gpxUrlField = new TextField("", 50);
		p.add(gpxUrlField);
		openUrlButton = new Button("Open GPX URL");
		openUrlButton.addActionListener(this);
		p.add(openUrlButton);
		add(p);

		openFileButton = new Button("Open GPX");
		openFileButton.addActionListener(this);
		add(openFileButton);

		kmzButton = new Button("Export tiles (png)");
		kmzButton.addActionListener(this);
		add(kmzButton);

		cacheButton = new Button("Change cache folder");
		cacheButton.addActionListener(this);
		add(cacheButton);
	}

	private void setInfo(String info) {
		infoLabel.setText(info);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == openFileButton) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					FileInputStream fis = new FileInputStream(file);
					parseGPX(fis);
				}
			} else if (e.getSource() == openUrlButton) {
				URL url = new URL(gpxUrlField.getText());
				InputStream is = url.openStream();
				parseGPX(is);
			} else if (e.getSource() == kmzButton) {
				processTiles(false);
			} else if (e.getSource() == cacheButton) {
				changeCacheFolder();
			}
		} catch (Exception ex) {
			setInfo("ERROR");
			ex.printStackTrace();
		}
	}

	private void changeCacheFolder() throws IOException {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			file = file.getParentFile();
			cacheFolder = file;
			cacheProperties
					.setProperty("cache.folder", file.getCanonicalPath());
			cacheProperties
					.store(new FileOutputStream(cachePropertiesFile), "");
		}
	}

	private void parseGPX(InputStream is) throws Exception {

		setInfo("Opening GPX");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document gpxFile = db.parse(is);

		GPXProcessor processor = new GPXProcessor(gpxFile);
		processor.parse();

		setInfo("Displaying GPX");
		displayGPX(processor);
		setInfo("");
	}

	private void displayGPX(GPXProcessor processor) {
		JSObject window = getWindow();

		Object[] emptyParams = {};
		Object[] coords = new Object[2];
		Object[] coordsInfo = new Object[3];
		window.call("clearMap", emptyParams);

		List<GPXPath> paths = processor.getPaths();
		for (GPXPath gpxPath : paths) {
			window.call("startPath", emptyParams);
			List<GPXPoint> points = gpxPath.getPoints();
			for (GPXPoint gpxPoint : points) {
				coords[0] = gpxPoint.getLon();
				coords[1] = gpxPoint.getLat();
				window.call("addPathPoint", coords);
			}
			window.call("endPath", emptyParams);
		}

		for (GPXPoint gpxPoint : processor.getWpts()) {
			coordsInfo[0] = gpxPoint.getLon();
			coordsInfo[1] = gpxPoint.getLat();
			coordsInfo[2] = gpxPoint.getCaption();
			window.call("addWaypoint", coordsInfo);
		}
	}

	private boolean zoomOut() throws InterruptedException {
		JSObject window = getWindow();
		Object[] emptyParams = {};
		Object oldZoomLevel = window.call("zoomLevel", emptyParams);
		setInfo("Zoom level : " + oldZoomLevel.toString());
		Thread.sleep(200);
		window.call("zoomOut", emptyParams);
		Thread.sleep(3000);
		Object zoomLevel = window.call("zoomLevel", emptyParams);
		setInfo("Zoom level : " + zoomLevel.toString());
		Thread.sleep(1000);
		return !oldZoomLevel.equals(zoomLevel);
	}

	private void processTiles(boolean downloadTiles) throws Exception {
		JSObject window = getWindow();
		Object[] emptyParams = {};
		setInfo("Retreiving tiles");
		String jsonTiles = (String) window.call("getTiles", emptyParams);
		List tilesJS = (List) jsonParser.parse(jsonTiles);
		setInfo("Retreiving " + Integer.toString(tilesJS.size() - 1) + " tiles");

		List<Tile> tiles = new ArrayList<Tile>();

		for (int i = 1; i < tilesJS.size(); i++) {
			Tile tile = new Tile();
			List tileJS = (List) tilesJS.get(i);
			tile.setUrl((String) tileJS.get(0));
			tile.setRealUrl((String) tileJS.get(0));

			tile.getBg().setLon(((Number) tileJS.get(1)).doubleValue());
			tile.getBg().setLat(((Number) tileJS.get(2)).doubleValue());

			tile.getBd().setLon(((Number) tileJS.get(3)).doubleValue());
			tile.getBd().setLat(((Number) tileJS.get(4)).doubleValue());

			tile.getHd().setLon(((Number) tileJS.get(5)).doubleValue());
			tile.getHd().setLat(((Number) tileJS.get(6)).doubleValue());

			tile.getHg().setLon(((Number) tileJS.get(7)).doubleValue());
			tile.getHg().setLat(((Number) tileJS.get(8)).doubleValue());

			tiles.add(tile);
		}

		exportTiles(tiles, downloadTiles);
	}

	private void exportTiles(List<Tile> tiles, boolean downloadTiles)
			throws Exception {
		for (Tile tile : tiles) {
			getFile(tile.getUrl(), tile, downloadTiles);
		}
		transform(tiles);
		setInfo("");
	}

	public static void write(RenderedImage image, float quality, File file)
			throws IOException {
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("JPEG");
		if (!iter.hasNext())
			throw new IOException("No Writers Available");
		writer = (ImageWriter) iter.next();
		if (file.exists())
			file.delete();
		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		writer.setOutput(ios);
		JPEGImageWriteParam iwp = new JPEGImageWriteParam(null);
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality);
		writer.write(null, new IIOImage(image, null, null), iwp);
		ios.flush();
		writer.dispose();
		ios.close();
	}

	private void transform(List<Tile> tiles) throws IOException {
		for (Tile tile : tiles) {
			if (tile.getUrl().toUpperCase().endsWith(".PNG")) {
				// File tileFile = new File(cacheFolder, tile.getUrl());
				// String newUrl = tile.getUrl().substring(0,
				// tile.getUrl().length() - 4)
				// + ".jpg";
				String coords = tile.getUrl().substring(0,
						tile.getUrl().length() - 4)
						+ ".wld";

				// File newTileFile = new File(cacheFolder, newUrl);
				// if (!newTileFile.exists()) {
				// BufferedImage image = ImageIO.read(tileFile);
				// write(image, 0.95f, newTileFile);
				// }

				File coordsFile = new File(coords);
				if (!coordsFile.exists()) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(
							coordsFile));
					bw
							.write("URL BL.lon BL.lat BR.lon BR.lat TL.lon TL.lat TR.lon TR.lat width height");
					bw.newLine();
					bw.write(tile.getRealUrl());
					bw.newLine();
					writePoint(bw, tile.getBg());
					writePoint(bw, tile.getBd());
					writePoint(bw, tile.getHg());
					writePoint(bw, tile.getHd());
					bw.write(df.format(tile.getBg().distanceTo(tile.getBd())));
					bw.newLine();
					bw.write(df.format(tile.getBg().distanceTo(tile.getHd())));
					bw.close();
				}
				// tile.setUrl(newUrl);
			}
		}
	}

	private void writePoint(BufferedWriter bw, GPXPoint point)
			throws IOException {
		bw.write(df.format(point.getLon()));
		bw.newLine();
		bw.write(df.format(point.getLat()));
		bw.newLine();
	}

	private void getFile(String url, Tile tile, boolean downloadTiles)
			throws Exception {
		String[] split = url.split(";");
		String dataSet = new String(Base64.decode(split[1].getBytes()));

		String subs = split[2].substring(0, split[2].length() - 1);
		String tileName = new String(Base64.decode(subs.getBytes()));

		int varLength = tileName.length() / 2;
		int firstPart = Integer.parseInt(tileName.substring(0, varLength));
		int secondPart = Integer.parseInt(tileName.substring(varLength));

		int x = (firstPart & 0x55555555) | (secondPart & 0xAAAAAAAA);
		int y = (secondPart & 0x55555555) | (firstPart & 0xAAAAAAAA);

		String xStr = Integer.toString(x);
		while (xStr.length() < 6) {
			xStr = "0" + xStr;
		}
		String yStr = Integer.toString(y);
		while (yStr.length() < 6) {
			yStr = "0" + yStr;
		}

		tileName = xStr + "_" + yStr;

		String subFolder = dataSet + "/" + xStr + "/"
				+ yStr.substring(0, yStr.length() - 2) + "/";
		File folder = new File(cacheFolder, subFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File cachedFile = new File(folder, tileName + ".png");
		cachedFile.getParentFile().mkdirs();

		tile.setUrl(cachedFile.getAbsolutePath());
		tile.setName(tileName);
		tile.setX(x);
		tile.setY(y);

		if (downloadTiles) {
			if (!cachedFile.exists()) {
				setInfo("Retreiving tile " + cachedFile);

				FileOutputStream fos = new FileOutputStream(cachedFile);
				URL remote = new URL(url);
				InputStream is = remote.openStream();

				copyStream(fos, is);
				fos.close();
			}
		}

	}

	private void copyStream(OutputStream fos, InputStream is)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = is.read(b)) != -1) {
			fos.write(b, 0, read);
		}
		is.close();
	}

	private JSObject getWindow() {
		JSObject window = JSObject.getWindow(applet);
		return window;
	}

}
