package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.PointField;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
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
public class GPXParser {
    public List<GPXPath> parsePaths(InputStream is) throws Exception {
        return parsePaths(is, null);
    }

    public List<GPXPath> parsePaths(InputStream is, String defaultName) throws Exception {
        return parsePaths(is, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, defaultName);
    }

    public List<GPXPath> parsePaths(File file) throws Exception {
        return parsePaths(file, null);
    }

    public List<GPXPath> parsePaths(File file, String defaultName) throws Exception {
        return parsePaths(file, (db, f) -> {
            try {
                return db.parse(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, defaultName);
    }

    private <T> List<GPXPath> parsePaths(T file, BiFunction<DocumentBuilder, T, Document> parser, String defaultName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document gpxDocument = parser.apply(db, file);
        List<GPXPath> paths = new ArrayList<>();
        String metadataName = getMetadataName(gpxDocument.getDocumentElement());
        if (metadataName == null || metadataName.isEmpty()) {
            metadataName = defaultName;
        }
        processElement(gpxDocument.getDocumentElement(), metadataName, paths);
        for (GPXPath gpxPath : paths) {
            gpxPath.computeArrays(ValueKind.source);
        }
        return paths;
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

    private void processElement(Element element, String metadataName, List<GPXPath> paths) {
        String tagName = element.getTagName().toLowerCase();

        if (tagName.equals("trk") || tagName.equals("rte")) {
            String name = getPathName(element, metadataName, paths);
            log.info("Parsing {}", name);
            GPXPath currentPath = new GPXPath(name);
            paths.add(currentPath);
        }

        if (tagName.equals("trkpt") || tagName.equals("rtept")) {
            processPoint(element, paths);
        } else {
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    processElement((Element) node, metadataName, paths);
                }
            }
        }
    }

    private String getPathName(final Element element, final String metadataName, final List<GPXPath> paths) {
        Element nameElement = findElement(element, "name");
        String baseName = "";
        if (nameElement != null) {
            baseName = nameElement.getTextContent();
        }
        if (baseName == null || baseName.isEmpty()) {
            baseName = metadataName;
        }
        if (baseName == null || baseName.isEmpty()) {
            baseName = "track";
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
        } while (isNameUsed(name, paths));
        return name;
    }

    private boolean isNameUsed(final String name, final List<GPXPath> paths) {
        for (GPXPath path : paths) {
            if (name.equals(path.getName())) {
                return true;
            }
        }
        return false;
    }

    private void processPoint(Element element, List<GPXPath> paths) {
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
        p.setTime(date, ValueKind.source);
        Element extensions = findElement(element, "extensions");
        getExtensionValues(p, extensions);
        paths.get(paths.size() - 1).addPoint(p);
    }

    private void getExtensionValues(Point p, Element extensions) {
        if (extensions != null) {
            NodeList childNodes = extensions.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element child = (Element) node;
                    try {
                        double value = Double.parseDouble(child.getTextContent());
                        String tagName = child.getTagName();
                        PointField pointField = PointField.fromGpxTag(tagName);
                        if (pointField != null) {
                            p.put(pointField, value, Unit.DOUBLE_ANY, ValueKind.source);
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
            if (node instanceof Element) {
                Element potEle = (Element) node;
                if (potEle.getTagName().toLowerCase().equals(string)) {
                    ele = potEle;
                }
            }
        }
        return ele;
    }

}