import java.applet.Applet;
import java.awt.Button;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import netscape.javascript.JSObject;

import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;

public class MagicPopup extends Frame implements ActionListener {

	private static final long serialVersionUID = -4533004192385246689L;

	private static final int IO_BUFFER_SIZE = 4 * 1024;

	private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n"
			+ "xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n"
			+ "<Folder>\n" + "<name>Magic</name>\n";
	private static final String KML_FOOTER = "</Folder>\n</kml>";

	private static final int N_TILES_QUAD = 3;

	private JSONParser jsonParser = new JSONParser();
	private Random random = new Random();
	private DecimalFormat df = null;

	private Applet applet;
	private Button openFileButton;
	private Button kmzButton;
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

		kmzButton = new Button("Export map (KMZ)");
		kmzButton.addActionListener(this);
		add(kmzButton);

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
				processKMZ();
			}
		} catch (Exception ex) {
			setInfo("ERROR");
			ex.printStackTrace();
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

	private void processKMZ() throws Exception {
		JSObject window = getWindow();
		Object[] emptyParams = {};
		setInfo("Retreiving tiles");
		String jsonTiles = (String) window.call("getTiles", emptyParams);
		List tilesJS = (List) jsonParser.parse(jsonTiles);

		List<Tile> tiles = new ArrayList<Tile>();

		int tileWidth = ((Number) tilesJS.get(0)).intValue();
		for (int i = 1; i < tilesJS.size(); i++) {
			Tile tile = new Tile();
			List tileJS = (List) tilesJS.get(i);
			tile.setUrl((String) tileJS.get(0));

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

		exportKMZ(tileWidth, tiles);
	}

	private void exportKMZ(int tileWidth, List<Tile> tiles) throws Exception {
		File userHome = new File(System.getProperty("user.home"));
		File cacheFolder = new File(userHome, "magic");
		if (!cacheFolder.exists()) {
			new File(cacheFolder, "tmp").mkdirs();
		}
		for (Tile tile : tiles) {
			getFile(cacheFolder, tile.getUrl(), tile);
		}

		processQuads(cacheFolder, tiles);
		transformToJpeg(cacheFolder, tiles);

		File kmlFile = new File(cacheFolder, newKmlFileName());
		FileWriter fw = new FileWriter(kmlFile);
		fw.write(KML_HEADER);

		for (Tile tile : tiles) {
			writeTile(tileWidth, fw, tile);
		}

		fw.write(KML_FOOTER);
		fw.close();

		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			FileOutputStream dest = new FileOutputStream(file);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));

			setInfo("Creating KMZ");

			ZipEntry zipEntry = new ZipEntry("doc.kml");
			out.putNextEntry(zipEntry);

			copyStream(out, new FileInputStream(kmlFile));
			for (Tile tile : tiles) {
				zipEntry = new ZipEntry(tile.getUrl());
				out.putNextEntry(zipEntry);
				copyStream(out, new FileInputStream(new File(cacheFolder, tile
						.getUrl())));
			}
			out.close();
		}

		setInfo("");
	}

	private void transformToJpeg(File cacheFolder, List<Tile> tiles)
			throws IOException {
		for (Tile tile : tiles) {
			if (tile.getUrl().toUpperCase().endsWith(".PNG")) {
				File tileFile = new File(cacheFolder, tile.getUrl());
				String newUrl = tile.getUrl().substring(0,
						tile.getUrl().length() - 4)
						+ ".jpg";
				File newTileFile = new File(cacheFolder, newUrl);
				if (!newTileFile.exists()) {
					BufferedImage image = ImageIO.read(tileFile);
					ImageIO.write(image, "jpg", newTileFile);
				}
				tile.setUrl(newUrl);
			}
		}
	}

	private static void processQuads(File cacheFolder, List<Tile> tiles)
			throws IOException {
		List<Tile> toRemove = new ArrayList<Tile>();
		List<Tile> toAdd = new ArrayList<Tile>();

		Map<Integer, Map<Integer, Tile>> tileMap = new HashMap<Integer, Map<Integer, Tile>>();
		for (Tile tile : tiles) {
			Map<Integer, Tile> xMap = tileMap.get(tile.getX());
			if (xMap == null) {
				xMap = new HashMap<Integer, Tile>();
				tileMap.put(tile.getX(), xMap);
			}
			xMap.put(tile.getY(), tile);
		}

		for (Tile tile : tiles) {
			if (toRemove.indexOf(tile) == -1) {
				int x = tile.getX();
				int y = tile.getY();
				Tile[][] quadArray = new Tile[N_TILES_QUAD][N_TILES_QUAD];

				boolean fail = false;
				for (int i = 0; i < N_TILES_QUAD; i++) {
					for (int j = 0; j < N_TILES_QUAD; j++) {
						quadArray[i][j] = getTileFromMap(tileMap, toRemove, x
								+ i, y + j);
						if (quadArray[i][j] == null) {
							fail = true;
						}
					}
				}

				if (!fail) {
					for (int i = 0; i < N_TILES_QUAD; i++)
						for (int j = 0; j < N_TILES_QUAD; j++)
							toRemove.add(quadArray[i][j]);
					Tile quadTile = makeQuadTile(cacheFolder, quadArray);
					toAdd.add(quadTile);
				}
			}
		}

		for (Tile tile : toRemove) {
			tiles.remove(tile);
		}
		for (Tile tile : toAdd) {
			tiles.add(tile);
		}

	}

	private static Tile makeQuadTile(File cacheFolder, Tile[][] quadArray)
			throws IOException {
		Tile res = new Tile();

		String tileUrl = quadArray[0][0].getUrl();
		String newUrl = tileUrl.substring(0, tileUrl.length() - 4)
				+ ".quad.jpg";

		res.setName(quadArray[0][0].getName() + "_quad");
		res.setHg(quadArray[0][0].getHg());
		res.setHd(quadArray[N_TILES_QUAD - 1][0].getHd());
		res.setBg(quadArray[0][N_TILES_QUAD - 1].getBg());
		res.setBd(quadArray[N_TILES_QUAD - 1][N_TILES_QUAD - 1].getBd());

		res.setUrl(newUrl);

		BufferedImage resultImage = new BufferedImage(N_TILES_QUAD * 256,
				N_TILES_QUAD * 256, BufferedImage.TYPE_INT_BGR);
		Graphics graphics = resultImage.getGraphics();

		for (int i = 0; i < N_TILES_QUAD; i++)
			for (int j = 0; j < N_TILES_QUAD; j++) {
				BufferedImage image = ImageIO.read(new File(cacheFolder,
						quadArray[i][j].getUrl()));
				graphics.drawImage(image, i * 256, j * 256, null);
			}

		ImageIO.write(resultImage, "jpg", new File(cacheFolder, newUrl));

		return res;
	}

	private static Tile getTileFromMap(
			Map<Integer, Map<Integer, Tile>> tileMap, List<Tile> toRemove,
			int x, int y) {
		Map<Integer, Tile> xMap = tileMap.get(x);
		if (xMap != null) {
			Tile tile = xMap.get(y);
			if (tile != null && toRemove.indexOf(tile) == -1) {
				return tile;
			}
		}
		return null;
	}

	private void writeTile(int tileWidth, FileWriter fw, Tile tile)
			throws IOException {
		int minLod = tileWidth / 2;
		int maxLod = tileWidth * 4;
		int minFade = tileWidth / 4;
		int maxFade = tileWidth;

		fw.write("<GroundOverlay>\n" + "<name>" + tile.getName() + "</name>\n"
				+ "<Icon>\n" + "  <href>");
		fw.write(tile.getUrl());
		fw.write("</href>\n" + "  <viewBoundScale>1</viewBoundScale>\n"
				+ "</Icon>\n");

		double maxlat = Math.max(tile.getHd().getLat(), tile.getHg().getLat());
		double maxlon = Math.max(tile.getHd().getLon(), tile.getBd().getLon());

		double minlat = Math.min(tile.getBd().getLat(), tile.getBg().getLat());
		double minlon = Math.min(tile.getBg().getLon(), tile.getHg().getLon());

		double centerlat = (tile.getHd().getLat() + tile.getHg().getLat()
				+ tile.getBd().getLat() + tile.getBg().getLat()) / 4.0;
		double centerlon = (tile.getHd().getLon() + tile.getHg().getLon()
				+ tile.getBd().getLon() + tile.getBg().getLon()) / 4.0;
		GPXPoint center = new GPXPoint(centerlon, centerlat);

		double rotation;

		GPXPoint bg = new GPXPoint(minlon, minlat);
		if (tile.getBg().getLat() < tile.getBd().getLat()) {
			// rotation > 0
			double d1 = bg.distanceTo(tile.getBg());
			double d2 = bg.distanceTo(tile.getHg());
			rotation = rad2deg(Math.atan2(d1, d2));
		} else {
			// rotation < 0
			double d1 = bg.distanceTo(tile.getBg());
			double d2 = bg.distanceTo(tile.getBd());
			rotation = -rad2deg(Math.atan2(d1, d2));
		}

		double width = (tile.getHg().distanceTo(tile.getHd()) + tile.getBg()
				.distanceTo(tile.getBd())) / 2.0;
		double height = (tile.getHg().distanceTo(tile.getBg()) + tile.getHd()
				.distanceTo(tile.getBd())) / 2.0;

		double dlon = getDLon(center, width, 0, maxlon - center.getLon());
		double dlat = getDLat(center, height, 0, maxlat - center.getLat());

		double north = centerlat + dlat;
		double south = centerlat - dlat;
		double east = centerlon + dlon;
		double west = centerlon - dlon;

		fw.write("<Region>\n" + "<Lod>\n" + "  <minLodPixels>" + minLod
				+ "</minLodPixels><maxLodPixels>" + maxLod
				+ "</maxLodPixels>\n" + "  <minFadeExtent>" + minFade
				+ "</minFadeExtent><maxFadeExtent>" + maxFade
				+ "</maxFadeExtent>\n" + "</Lod>\n" + "<LatLonAltBox>\n"
				+ "  <north>" + format(maxlat) + "</north>\n" + "  <south>"
				+ format(minlat) + "</south>\n" + "  <east>" + format(maxlon)
				+ "</east>\n" + "  <west>" + format(minlon) + "</west>\n"
				+ "</LatLonAltBox>\n" + "</Region>\n");

		fw.write("<LatLonBox>\n" + "  <north>" + format(north) + "</north>\n"
				+ "  <south>" + format(south) + "</south>\n" + "  <east>"
				+ format(east) + "</east>\n" + "  <west>" + format(west)
				+ "</west>\n" + "  <rotation>" + format(rotation)
				+ "</rotation>\n" + "</LatLonBox>\n" + "</GroundOverlay>");
	}

	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	private double getDLat(GPXPoint center, double height, double minLat,
			double maxLat) {
		GPXPoint pMin = new GPXPoint(center.getLon(), center.getLat() + minLat);
		GPXPoint pMax = new GPXPoint(center.getLon(), center.getLat() + maxLat);
		double moyLat = (minLat + maxLat) / 2.0;
		if (pMin.distanceTo(pMax) < height / 2000) {
			return moyLat;
		}
		GPXPoint pMoy = new GPXPoint(center.getLon(), center.getLat() + moyLat);
		if (center.distanceTo(pMoy) > height / 2.0) {
			return getDLat(center, height, minLat, moyLat);
		} else {
			return getDLat(center, height, moyLat, maxLat);
		}
	}

	private double getDLon(GPXPoint center, double width, double minLon,
			double maxLon) {
		GPXPoint pMin = new GPXPoint(center.getLon() + minLon, center.getLat());
		GPXPoint pMax = new GPXPoint(center.getLon() + maxLon, center.getLat());
		double moyLon = (minLon + maxLon) / 2.0;
		if (pMin.distanceTo(pMax) < width / 2000) {
			return moyLon;
		}
		GPXPoint pMoy = new GPXPoint(center.getLon() + moyLon, center.getLat());
		if (center.distanceTo(pMoy) > width / 2.0) {
			return getDLon(center, width, minLon, moyLon);
		} else {
			return getDLon(center, width, moyLon, maxLon);
		}
	}

	private void writeTileOld(int tileWidth, FileWriter fw, Tile tile)
			throws IOException {
		fw.write("<GroundOverlay>\n" + "<name>Another tile</name>\n"
				+ "<Icon>\n" + "  <href>");
		fw.write(tile.getUrl());
		fw.write("</href>\n" + "  <viewBoundScale>1</viewBoundScale>\n"
				+ "</Icon>\n" + "<gx:LatLonQuad>\n" + "  <coordinates>\n    ");
		fw.write(format(tile.getBg().getLon()) + ","
				+ format(tile.getBg().getLat()) + " ");
		fw.write(format(tile.getBd().getLon()) + ","
				+ format(tile.getBd().getLat()) + " ");
		fw.write(format(tile.getHd().getLon()) + ","
				+ format(tile.getHd().getLat()) + " ");
		fw.write(format(tile.getHg().getLon()) + ","
				+ format(tile.getHg().getLat()));
		int minLod = tileWidth / 2;
		int maxLod = tileWidth * 4;
		int minFade = tileWidth / 4;
		int maxFade = tileWidth;

		double north = Math.max(tile.getHd().getLat(), tile.getHg().getLat());
		double east = Math.max(tile.getHd().getLon(), tile.getBd().getLon());

		double south = Math.min(tile.getBd().getLat(), tile.getBg().getLat());
		double west = Math.min(tile.getBg().getLon(), tile.getHg().getLon());

		fw.write("\n  </coordinates>\n" + "</gx:LatLonQuad>\n" + "<Region>\n"
				+ "<Lod>\n" + "  <minLodPixels>"
				+ minLod
				+ "</minLodPixels><maxLodPixels>"
				+ maxLod
				+ "</maxLodPixels>\n"
				+ "  <minFadeExtent>"
				+ minFade
				+ "</minFadeExtent><maxFadeExtent>"
				+ maxFade
				+ "</maxFadeExtent>\n"
				+ "</Lod>\n"
				+ "<LatLonAltBox>\n"
				+ "  <north>"
				+ format(north)
				+ "</north>\n"
				+ "  <south>"
				+ format(south)
				+ "</south>\n"
				+ "  <east>"
				+ format(east)
				+ "</east>\n"
				+ "  <west>"
				+ format(west)
				+ "</west>\n"
				+ "</LatLonAltBox>\n" + "</Region>\n" + "</GroundOverlay>");
	}

	private String format(double lon) {
		return df.format(lon);
	}

	private String newKmlFileName() {
		return Integer.toHexString(random.nextInt()) + ".kml";
	}

	public static void main(String[] args) throws Exception {
		// String tile = "oijekeoijgregiojregioerj.png";
		// String newUrl = tile.substring(0, tile.length() - 4) + ".jpg";
		// System.out.println(newUrl);

		List<Tile> tiles = new ArrayList<Tile>();
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				Tile t = new Tile();
				t.setX(i + 40);
				t.setY(j + 171);
				tiles.add(t);
			}
		}
		processQuads(null, tiles);
		for (Tile tile : tiles) {
			System.out.println(tile.getName());
		}

		// String url =
		// "http://m1.viamichelin.com/mapsgene/dm/mapdirect;ZnJhX2NfMDE4NWtfcjA0;MDAwMDAwMDAzMjAwMDAwMDAyMDk=?";
		//
		// String[] split = url.split(";");
		// String dataSet = new String(Base64.decode(split[1].getBytes()));
		//
		// String subs = split[2].substring(0, split[2].length() - 1);
		// String tileName = new String(Base64.decode(subs.getBytes()));
		//
		// int varLength = tileName.length() / 2;
		// int firstPart = Integer.parseInt(tileName.substring(0, varLength));
		// int secondPart = Integer.parseInt(tileName.substring(varLength));
		//
		// int x = firstPart;// (firstPart & 0x55555555) | (secondPart &
		// // 0xAAAAAAAA);
		// int y = secondPart;// (secondPart & 0x55555555) | (firstPart &
		// // 0xAAAAAAAA);
		//
		// String xStr = Integer.toString(x);
		// while (xStr.length() < 6) {
		// xStr = "0" + xStr;
		// }
		// String yStr = Integer.toString(y);
		// while (yStr.length() < 6) {
		// yStr = "0" + yStr;
		// }
		//
		// tileName = xStr + "_" + yStr;
		//
		// String subFolder = dataSet + "/" + xStr + "/"
		// + yStr.substring(0, yStr.length() - 2) + "/";
		//
		// String complete = subFolder + tileName + ".png";
		//
		// String tileName2 = complete.substring(complete.lastIndexOf('/') + 1);
		// tileName2 = tileName2.substring(0, tileName2.indexOf('.'));
		//
		// System.out.println(subFolder);
		// System.out.println(tileName);
		// System.out.println(tileName2);
	}

	private void getFile(File cacheFolder, String url, Tile tile)
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
		if (!cachedFile.exists()) {
			setInfo("Retreiving tile " + tileName);

			FileOutputStream fos = new FileOutputStream(cachedFile);
			URL remote = new URL(url);
			InputStream is = remote.openStream();

			copyStream(fos, is);
			fos.close();
		}

		tile.setUrl(subFolder + tileName + ".png");
		tile.setName(tileName);
		tile.setX(x);
		tile.setY(y);
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
