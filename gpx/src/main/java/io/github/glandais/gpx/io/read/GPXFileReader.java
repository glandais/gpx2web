package io.github.glandais.gpx.io.read;

import io.github.glandais.gpx.data.*;
import io.github.glandais.gpx.io.GPXField;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
@Singleton
@Slf4j
public class GPXFileReader {

    public static final String DEFAULT_NAME = "gpx";

    public GPX parseGPX(InputStream is) throws Exception {
        return parseGPX(is, null, false);
    }

    public GPX parseGPX(InputStream is, String forcedName, boolean erasePathNames) throws Exception {
        return parseGPX(is, forcedName, erasePathNames, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public GPX parseGPX(File file) throws Exception {
        return parseGPX(file, null, false);
    }

    public GPX parseGPX(File file, String forcedName, boolean erasePathNames) throws Exception {
        return parseGPX(file, forcedName, erasePathNames, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> GPX parseGPX(
            T file, String forcedName, boolean erasePathNames, BiFunction<DocumentBuilder, T, Document> parser)
            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document gpxDocument = parser.apply(db, file);
        String gpxName;
        if (forcedName != null) {
            gpxName = forcedName;
        } else {
            gpxName = getMetadataName(gpxDocument.getDocumentElement());
        }
        GPX gpx = new GPX(gpxName, new ArrayList<>(), new ArrayList<>());
        processElement(gpxDocument.getDocumentElement(), forcedName, erasePathNames, gpx);

        List<GPXPath> paths = gpx.paths().stream()
                .filter(gpxPath -> gpxPath.getPoints().size() >= 2)
                .toList();

        if (gpxName == null) {
            if (!paths.isEmpty()) {
                gpxName = paths.get(0).getName();
            } else {
                gpxName = DEFAULT_NAME;
            }
        }

        gpx = new GPX(gpxName, paths, gpx.waypoints());

        for (GPXPath gpxPath : gpx.paths()) {
            gpxPath.computeArrays();
        }
        return gpx;
    }

    private String getMetadataName(Element element) {
        String tagName = element.getTagName().toLowerCase();

        String result = null;
        if (tagName.equals("metadata")) {
            Element nameElement = findElement(element, "name");
            if (nameElement != null) {
                result = nameElement.getTextContent();
            }
        } else {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (result == null && node instanceof Element) {
                    result = getMetadataName((Element) node);
                }
            }
        }
        return result;
    }

    private void processElement(Element element, String forcedName, boolean erasePathNames, GPX gpx) {
        String tagName = element.getTagName().toLowerCase();

        if (tagName.equals("trk") || tagName.equals("rte")) {
            String name = getPathName(element, forcedName, erasePathNames, gpx);
            log.debug("Parsing {}", name);
            GPXPath currentPath = new GPXPath(name, GPXPathType.getByTagName(tagName));
            gpx.paths().add(currentPath);
        }

        if (tagName.equals("wpt")) {
            processWaypoint(element, gpx);
        }

        if (tagName.equals("trkpt") || tagName.equals("rtept")) {
            processPoint(element, gpx);
        } else {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    processElement((Element) node, forcedName, erasePathNames, gpx);
                }
            }
        }
    }

    private String getPathName(final Element element, final String forcedName, boolean erasePathNames, final GPX gpx) {
        String baseName = "";
        if (forcedName != null && erasePathNames) {
            baseName = forcedName;
        } else {
            Element nameElement = findElement(element, "name");
            if (nameElement != null) {
                baseName = nameElement.getTextContent();
            }
            if (baseName == null || baseName.isEmpty()) {
                baseName = gpx.name();
            }
            if (baseName == null || baseName.isEmpty()) {
                baseName = DEFAULT_NAME;
            }
        }
        int i = 0;
        String name;
        do {
            if (i == 0) {
                name = baseName;
            } else {
                name = baseName + " " + i;
            }
            i++;
        } while (isNameUsed(name, gpx));
        return name;
    }

    private boolean isNameUsed(final String name, final GPX gpx) {
        for (GPXPath path : gpx.paths()) {
            if (name.equalsIgnoreCase(path.getName())) {
                return true;
            }
        }
        return false;
    }

    private void processWaypoint(Element element, GPX gpx) {
        double lon = Math.toRadians(Double.parseDouble(element.getAttribute("lon")));
        double lat = Math.toRadians(Double.parseDouble(element.getAttribute("lat")));
        Element nameElement = findElement(element, "name");
        String name = "";
        if (nameElement != null) {
            name = nameElement.getTextContent();
        }
        Point p = new Point();
        p.setLat(lat);
        p.setLon(lon);
        gpx.waypoints().add(new GPXWaypoint(name, p));
    }

    private void processPoint(Element element, GPX gpx) {
        double lon = Math.toRadians(Double.parseDouble(element.getAttribute("lon")));
        double lat = Math.toRadians(Double.parseDouble(element.getAttribute("lat")));
        Element timeElement = findElement(element, "time");
        Element eleElement = findElement(element, "ele");
        Instant date = Instant.EPOCH;
        if (timeElement != null) {
            String dateString = timeElement.getTextContent();
            try {
                date = Instant.parse(dateString);
            } catch (Exception e) {
                // oops
            }
        }
        double ele = 0;
        if (eleElement != null) {
            ele = Double.parseDouble(eleElement.getTextContent());
        }
        Point p = new Point();
        p.setLon(lon);
        p.setLat(lat);
        p.setEle(ele);
        p.setInstant(null, date);
        Element powerInWatts = findElement(element, "PowerInWatts");
        if (powerInWatts != null) {
            double value = Double.parseDouble(powerInWatts.getTextContent());
            p.setPower(value);
        }
        Element extensions = findElement(element, "extensions");
        getExtensionValues(p, extensions);
        List<GPXPath> paths = gpx.paths();
        paths.get(paths.size() - 1).addPoint(p);
    }

    private void getExtensionValues(Point p, Element extensions) {
        if (extensions != null) {
            NodeList childNodes = extensions.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element child) {
                    try {
                        double value = Double.parseDouble(child.getTextContent());
                        String tagName = child.getTagName();
                        GPXField pointField = GPXField.fromGpxTag(tagName);
                        if (pointField != null) {
                            p.put(pointField.getPropertyKey(), value);
                            // } else {
                            // p.putDebug(tagName, value, Unit.DOUBLE_ANY);
                        }
                    } catch (NumberFormatException e) {
                        // oops
                    }
                    getExtensionValues(p, child);
                }
            }
        }
    }

    private Element findElement(Element element, String string) {
        Element ele = null;
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element potEle) {
                if (potEle.getTagName().equalsIgnoreCase(string)) {
                    ele = potEle;
                }
            }
        }
        return ele;
    }
}
