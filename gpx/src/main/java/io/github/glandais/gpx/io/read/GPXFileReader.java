package io.github.glandais.gpx.io.read;

import io.github.glandais.gpx.data.*;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.io.GPXField;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Service
@Singleton
@Slf4j
public class GPXFileReader {
    public GPX parseGpx(InputStream is) throws Exception {
        return parseGpx(is, "path");
    }

    public GPX parseGpx(InputStream is, String defaultName) throws Exception {
        return parseGpx(is, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, defaultName);
    }

    public GPX parseGpx(File file) throws Exception {
        return parseGpx(file, "path");
    }

    public GPX parseGpx(File file, String defaultName) throws Exception {
        return parseGpx(file, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, defaultName);
    }

    private <T> GPX parseGpx(T file, BiFunction<DocumentBuilder, T, Document> parser, String defaultName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document gpxDocument = parser.apply(db, file);
        String metadataName = getMetadataName(gpxDocument.getDocumentElement());
        if (metadataName == null || metadataName.isEmpty()) {
            metadataName = defaultName;
        }
        GPX gpx = new GPX(metadataName, new ArrayList<>(), new ArrayList<>());
        processElement(gpxDocument.getDocumentElement(), metadataName, gpx);
        for (GPXPath gpxPath : gpx.paths()) {
            gpxPath.computeArrays(ValueKind.source);
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

    private void processElement(Element element, String defaultName, GPX gpx) {
        String tagName = element.getTagName().toLowerCase();

        if (tagName.equals("trk") || tagName.equals("rte")) {
            String name = getPathName(element, defaultName, gpx);
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
                    processElement((Element) node, defaultName, gpx);
                }
            }
        }
    }

    private String getPathName(final Element element, final String defaultName, final GPX gpx) {
        Element nameElement = findElement(element, "name");
        String baseName = "";
        if (nameElement != null) {
            baseName = nameElement.getTextContent();
        }
        if (baseName == null || baseName.isEmpty()) {
            baseName = defaultName;
        }
        if (baseName == null || baseName.isEmpty()) {
            baseName = "path";
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
        p.setEle(ele, ValueKind.source);
        p.setInstant(date, ValueKind.source);
        Element powerInWatts = findElement(element, "PowerInWatts");
        if (powerInWatts != null) {
            double value = Double.parseDouble(powerInWatts.getTextContent());
            p.setPower(value, ValueKind.source);
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
                            p.put(pointField.getPropertyKey(), ValueKind.source, value);
//                        } else {
                            //p.putDebug(tagName, value, Unit.DOUBLE_ANY);
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