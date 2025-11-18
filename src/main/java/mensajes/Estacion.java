package mensajes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.gson.Gson;

public class Estacion {
    private Double T;
    private Double H;
    private Double PM10;
    private Double SO2;
    private Double NO2;
    private Double O3;
    private transient Random rnd = new Random();

    public Estacion() {}
    
    public void generarValores() {
    	this.T = (double) (20 + rnd.nextInt(10));
        this.H = (double) (40 + rnd.nextInt(30));
        this.PM10 = (double) (10 + rnd.nextInt(50));
        this.SO2 = (double) (5 + rnd.nextInt(20));
        this.NO2 = (double) (10 + rnd.nextInt(40));
        this.O3 = (double) (50 + rnd.nextInt(50));
    }
    
    public Double getT() {
    	return T;
    }
    public Double getH() {
    	return H;
    }
    public Double getPM10() {
    	return PM10;
    }
    public Double getSO2() {
    	return SO2;
    }
    public Double getNO2() {
    	return NO2;
    }
    public Double getO3() {
    	return O3;
    }

    // Exportar a XML
    public String toXmlString() {
    	generarValores();
        String xml =
            "<Estacion>\n" +
            "    <T>" + T + "</T>\n" +
            "    <H>" + H + "</H>\n" +
            "    <PM10>" + PM10 + "</PM10>\n" +
            "    <SO2>" + SO2 + "</SO2>\n" +
            "    <NO2>" + NO2 + "</NO2>\n" +
            "    <O3>" + O3 + "</O3>\n" +
            "</Estacion>";
        return xml;
    }

    // Importar desde XML con validación XSD
    public void fromXmlString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;

            try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
                doc = builder.parse(is);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(
            	    getClass().getClassLoader().getResource("estacion.xsd")
            	);
            Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    System.err.println("⚠Advertencia: " + e.getMessage());
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException("Error de validación: " + e.getMessage());
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException("Error fatal: " + e.getMessage());
                }
            });

            validator.validate(new DOMSource(doc));

            this.T = parseDoubleOptional(doc, "T");
            this.H = parseDoubleOptional(doc, "H");
            this.PM10 = parseDoubleOptional(doc, "PM10");
            this.SO2 = parseDoubleOptional(doc, "SO2");
            this.NO2 = parseDoubleOptional(doc, "NO2");
            this.O3 = parseDoubleOptional(doc, "O3");

        } catch (SAXException e) {
            System.err.println("Error de validación XSD: " + e.getMessage());
        } catch (IOException | ParserConfigurationException e) {
            System.err.println("Error al procesar XML: " + e.getMessage());
        }
    }

    private Double parseDoubleOptional(Document doc, String tagName) {
        NodeList list = doc.getElementsByTagName(tagName);

        if (list.getLength() == 0)
            return null; // ahora es válido que falte

        String text = list.item(0).getTextContent().trim();

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor no numérico en <" + tagName + ">: " + text);
        }
    }
    
    // Exportar a JSON
    public String toJsonString() {
    	generarValores();
    	Gson gson = new Gson();
    	return gson.toJson(this);
    }
    
    // Importar de JSON SIN validación
    public void fromJsonString(String json) {
        try {
            Gson gson = new Gson();
            Estacion e = gson.fromJson(json, Estacion.class);
            if (e == null) {
                throw new IllegalArgumentException("JSON no contiene un objeto Estacion válido");
            }
            this.T = e.T;
            this.H = e.H;
            this.PM10 = e.PM10;
            this.SO2 = e.SO2;
            this.NO2 = e.NO2;
            this.O3 = e.O3;
        } catch (Exception e) {
            System.err.println("Error al parsear JSON: " + e.getMessage());
        }
    }
}