package io.github.glandais.gpx.io.read;

import io.github.glandais.gpx.data.*;
import io.github.glandais.gpx.io.GPXField;
import jakarta.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
@Singleton
@Slf4j
public class GPXFileReader {

    public static final String DEFAULT_NAME = "gpx";

    public GPX parseGPX(InputStream is) throws Exception {
        return parseGPX(is, null, false);
    }

    public GPX parseGPX(InputStream is, String forcedName, boolean erasePathNames) throws Exception {
        return parseGPX(is.readAllBytes(), forcedName, erasePathNames, "input stream");
    }

    public GPX parseGPX(File file) throws Exception {
        return parseGPX(file, null, false);
    }

    public GPX parseGPX(File file, String forcedName, boolean erasePathNames) throws Exception {
        return parseGPX(Files.readAllBytes(file.toPath()), forcedName, erasePathNames, file.getPath());
    }

    /**
     * Parses strictly first. Only when that fails on malformed XML do we retry once on repaired
     * bytes, through the same hardened builder. Surviving repair grants no extra trust: a DOCTYPE is
     * still refused on the second pass. When repair does not help, the original error is what the
     * caller sees.
     */
    private GPX parseGPX(byte[] raw, String forcedName, boolean erasePathNames, String source) throws Exception {
        Document gpxDocument;
        try {
            gpxDocument = parseDocument(raw);
        } catch (SAXException | IOException strictFailure) {
            byte[] repaired = GpxXmlRepair.repair(raw);
            if (repaired == null) {
                throw strictFailure;
            }
            try {
                gpxDocument = parseDocument(repaired);
            } catch (SAXException | IOException repairFailure) {
                throw strictFailure;
            }
            log.warn("Repaired malformed XML in {}: {}", source, strictFailure.getMessage());
        }
        return toGPX(gpxDocument, forcedName, erasePathNames);
    }

    private Document parseDocument(byte[] raw) throws ParserConfigurationException, SAXException, IOException {
        return newSecureDocumentBuilder().parse(new ByteArrayInputStream(raw));
    }

    private GPX toGPX(Document gpxDocument, String forcedName, boolean erasePathNames) {
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

    /**
     * Builds a {@link DocumentBuilder} hardened against XXE: no DOCTYPE, no external entities, no
     * entity-expansion bombs. GPX has no legitimate use for any of those, and the parsed text is
     * routinely echoed back to callers, so a resolved entity would be a file-read / SSRF primitive.
     */
    private static DocumentBuilder newSecureDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Rejects any document containing a DOCTYPE outright — the simplest, strongest guard.
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        dbf.setExpandEntityReferences(false);
        dbf.setXIncludeAware(false);
        dbf.setNamespaceAware(false);
        return dbf.newDocumentBuilder();
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
