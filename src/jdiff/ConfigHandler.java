package jdiff;

import java.io.*;
import java.util.*;

/* For SAX XML parsing */
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handle the parsing of an XML configuration file.
 *
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com
 */
class ConfigHandler extends DefaultHandler {

    /** The instance of the Config object which is populated from the file. */ 
    private Config config_ = null;

    /** Set if in text. */
    private boolean inText = false;

    /** The current text which is being assembled from chunks. */
    private String currentText = null;
    
    /** Which API is currently being parsed. 1 = old, 2 = new. */
    private int apiNum = 0;
    
    /** Default constructor. */
    public ConfigHandler(Config config) {
        config_ = config;
    }   

    public void startDocument() {
    }
    
    public void endDocument() {
        // Display the configuration in a form which could be passed 
        // directly to Javadoc if necessary.
        config_.dump();
    }

    public void startElement(java.lang.String uri, java.lang.String localName,
                             java.lang.String qName, Attributes attributes) {
        if (localName.compareTo("config") == 0) {
            String configName = attributes.getValue("name");
            config_.name_ = configName;
            String version = attributes.getValue("jdversion");
            config_.version_ = version;
        } else if (localName.compareTo("api") == 0) {
            apiNum++;
            if (apiNum > 2) {
                // TODO problem with too many api elements
            }
            String apiName = attributes.getValue("name");
            if (apiNum == 1)
                Config.oldProductName(apiName);
            else
                Config.newProductName(apiName);
            // TODO
        } else if (localName.compareTo("javadoc") == 0) {
            // TODO add the javadoc args
        } else if (localName.compareTo("source") == 0) {
            // TODO add the source path (may be more than one)
            String location = attributes.getValue("location");
            String packages = attributes.getValue("packages");
        } else if (localName.compareTo("jdiff") == 0) {
            String baseURI = attributes.getValue("baseURI");
            if (baseURI != null) {
                //TODO
            }
        } else if (localName.compareTo("report") == 0) {
            // TODO
        } else {
            System.out.println("Error: unknown element type: " + localName);
            System.exit(-1);
        }
    }
    
    public void endElement(java.lang.String uri, java.lang.String localName, 
                           java.lang.String qName) {
        // TODO
    }
    
    /** Deal with a chunk of text. The text may come in multiple chunks. */
    public void characters(char[] ch, int start, int length) {
        if (inText) {
            String chunk = new String(ch, start, length);
            if (currentText == null)
                currentText = chunk;
            else
                currentText += chunk;
        }
    }

    public void warning(SAXParseException e) {
        System.out.println("Warning (" + e.getLineNumber() + "): parsing XML configuration file:" + e);
        e.printStackTrace();
    }

    public void error(SAXParseException e) {
        System.out.println("Error (" + e.getLineNumber() + "): parsing XML configuration file:" + e);
        e.printStackTrace();
        System.exit(1);
    }
    
    public void fatalError(SAXParseException e) {
        System.out.println("Fatal Error (" + e.getLineNumber() + "): parsing XML configuration file:" + e);
        e.printStackTrace();
        System.exit(1);
    }    

    /** Set to enable increased logging verbosity for debugging. */
    private static final boolean trace = false;

}
