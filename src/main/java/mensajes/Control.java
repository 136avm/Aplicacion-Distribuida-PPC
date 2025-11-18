package mensajes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

public class Control {
	private String Comando;
	private String Destinatario;
	private String Valor;
	
	public Control() {}
	
	public Control(String Comando, String Destinatario, String Valor) {
		this.Comando = Comando;
		this.Destinatario = Destinatario;
		this.Valor = Valor;
	}
	
	public String getComando() {
		return this.Comando;
	}
	public String getDestinatario() {
		return this.Destinatario;
	}
	public String getValor() {
		return this.Valor;
	}
	
	// Exportar a XML
	public String toXmlString() {
        String xml =
            "<Control>\n" +
            "    <Comando>" + Comando + "</Comando>\n" +
            "    <Destinatario>" + Destinatario + "</Destinatario>\n" +
            "    <Valor>" + Valor + "</Valor>\n" +
            "</Control>";
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
            	    getClass().getClassLoader().getResource("control.xsd")
            	);
            Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    System.err.println("Advertencia: " + e.getMessage());
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

            this.Comando = parseStringSafe(doc, "Comando");
            this.Destinatario = parseStringSafe(doc, "Destinatario");
            this.Valor = parseStringSafe(doc, "Valor");

        } catch (SAXException e) {
            System.err.println("Error de validación XSD: " + e.getMessage());
        } catch (IOException | ParserConfigurationException e) {
            System.err.println("Error al procesar XML: " + e.getMessage());
        }
    }
    
    private String parseStringSafe(Document doc, String tagName) {
        NodeList list = doc.getElementsByTagName(tagName);
        if (list.getLength() == 0)
            throw new IllegalArgumentException("Falta el elemento <" + tagName + ">");

        String text = list.item(0).getTextContent().trim();
        if (text.isEmpty() && !tagName.equals("Valor"))
            throw new IllegalArgumentException("El elemento <" + tagName + "> está vacío");

        return text;
    }
    
    // Exportar a JSON
    public String toJsonString() {
    	Gson gson = new Gson();
    	return gson.toJson(this);
    }
    
    // Importar de JSON SIN validación
    public void fromJsonString(String json) {
        try {
            Gson gson = new Gson();
            Control c = gson.fromJson(json, Control.class);
            if (c == null) {
                throw new IllegalArgumentException("JSON no contiene un objeto Control válido");
            }
            this.Comando = c.Comando;
            this.Destinatario = c.Destinatario;
            this.Valor = c.Valor;
        } catch (Exception e) {
            System.err.println("Error al parsear JSON: " + e.getMessage());
        }
    }
}
