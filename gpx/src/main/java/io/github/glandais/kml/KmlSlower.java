package io.github.glandais.kml;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class KmlSlower {
    private static final DecimalFormat df = new DecimalFormat("0.00#########################", new DecimalFormatSymbols(Locale.ENGLISH));

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document kmlFile = db.parse(new File(args[0]));
        processElement(kmlFile, kmlFile.getDocumentElement());

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(kmlFile);
        StreamResult result = new StreamResult(new File(args[1]));
        transformer.transform(source, result);
    }

    private static void processElement(Document document, Element element) throws DOMException, ParseException {
        String tagName = element.getTagName().toLowerCase();
        System.out.println(tagName);
        if (tagName.equals("gx:duration")) {
            double timespan = df.parse(element.getTextContent()).doubleValue();
            timespan = timespan / 1.3;
            element.setTextContent(df.format(timespan));
        }
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                processElement(document, (Element) node);
            }
        }
    }
}
