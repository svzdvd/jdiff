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
 * Class to handle configuration files for Javadoc and Jdiff.
 *
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com
 */
public class Config {

    /** The instance of the Config object which is populated from the file. */ 
    private static Config config_ = null;

    /** Default constructor. */
    public Config() {
    }

    /** 
     * Read the file where the XML for the configuration is stored.
     */
    public static void readFile(String filename) {
        // If the file does not exist, return
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("Error: configuration file " + filename + " not found");
            return;
        }

        try {
            config_ = new Config();
            DefaultHandler handler = new ConfigHandler(config_);
            XMLReader parser = null;
            try {
                parser = (XMLReader)Class.forName("org.apache.xerces.parsers.SAXParser").newInstance();
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Could not find class 'org.apache.xerces.parsers.SAXParser'");
                System.exit(1);
            } catch (InstantiationException ie) {
                System.out.println("Could not instantiate 'org.apache.xerces.parsers.SAXParser': " + ie);
                ie.printStackTrace();
                System.exit(1);
            } catch (IllegalAccessException iae) {
                System.out.println("IllegalAccessException creating an instance of 'org.apache.xerces.parsers.SAXParser': " + iae);
                iae.printStackTrace();
                System.exit(1);
            }
            if (XMLToAPI.validateXML) {
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            }
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            parser.parse(filename);
        } catch(org.xml.sax.SAXNotRecognizedException snre) {
            System.out.println("SAX Parser does not recognize feature: " + snre);
            snre.printStackTrace();
            System.exit(1);
        } catch(org.xml.sax.SAXNotSupportedException snse) {
            System.out.println("SAX Parser feature is not supported: " + snse);
            snse.printStackTrace();
            System.exit(1);
        } catch(org.xml.sax.SAXException saxe) {
            System.out.println("SAX Exception parsing file '" + filename + "' : " + saxe);
            saxe.printStackTrace();
            System.exit(1);
        } catch(java.io.IOException ioe) {
            System.out.println("IOException parsing file '" + filename + "' : " + ioe);
            ioe.printStackTrace();
            System.exit(1);
        }
    } //readFile()

    /** 
     * Display the configuration in a form which could be passed 
     * directly to Javadoc if necessary. 
     */
    public void dump() {
        System.out.println("Configuration:");
        System.out.println(" " + name_);
        // TODO
    };

    /** Set the name of the old API. */
    public static void oldProductName(String name) {
    };

    /** Return the name of the old API. */
    public static String oldProductName() {
        return "";
    };

    /** Set the name of the new API. */
    public static void newProductName(String name) {
    };

    /** Return the name of the new API. */
    public static String newProductName() {
        return "";
    };

    /** Return the location of the source of the old API. */
    public static String oldSrcDirName() {
        return "";
    };

    /** Return the location of the source of the new API. */
    public static String newSrcDirName() {
        return "";
    };

    /** The name of this configuration. */
    public String name_ = null;

    /** The version of this configuration. */
    public String version_ = null;

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;
}
