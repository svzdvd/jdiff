package jdiff;

import java.io.*;
import java.util.*;

/* For SAX XML parsing */
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.*;

/**
 * Creates a Comments from an XML file. The Comments object is the internal 
 * representation of the comments for the changes.
 * All methods in this class for populating a Comments object are static.
 * 
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com
 */
public class Comments {

    /** 
     * All the possible comments known about, accessible by the commentID.
     */
    public static Hashtable allPossibleComments = new Hashtable();

    /** The old Comments object which is populated from the file read in. */ 
    private static Comments oldComments_ = null;

    /** Default constructor. */
    public Comments() {
        commentsList_ = new ArrayList(); // SingleComment[]
    }   
  
    // The list of comments elements associated with this objects
    public List commentsList_ = null; // SingleComment[]

    /** 
     * Read the file where the XML for comments about the changes between
     * the old API and new API is stored and create a Comments object for 
     * it. The Comments object may be null if no file exists.
     */
    public static Comments readFile(String filename) {
        // If validation is desired, write out the appropriate comments.xsd 
        // file in the same directory as the comments XML file.
        if (XMLToAPI.validateXML) {
            writeXSD(filename);
        }

        // If the file does not exist, return null
        File f = new File(filename);
        if (!f.exists())
            return null;

        // The instance of the Comments object which is populated from the file. 
        oldComments_ = new Comments();
        try {
            DefaultHandler handler = new CommentsHandler(oldComments_);
            XMLReader parser = null;
            try {
                String parserName = System.getProperty("org.xml.sax.driver");
                if (parserName == null) {
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                } else {
                    // Let the underlying mechanisms try to work out which 
                    // class to instantiate
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                }
            } catch (SAXException saxe) {
                System.out.println("SAXException: " + saxe);
                saxe.printStackTrace();
                System.exit(1);
            }

            if (XMLToAPI.validateXML) {
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            }
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            parser.parse(new InputSource(new FileInputStream(new File(filename))));
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

        Collections.sort(oldComments_.commentsList_);
        return oldComments_;
    } //readFile()

    /**
     * Write the XML Schema file used for validation.
     */
    public static void writeXSD(String filename) {
        String xsdFileName = filename;
        int idx = xsdFileName.lastIndexOf('\\');
        int idx2 = xsdFileName.lastIndexOf('/');
        if (idx == -1 && idx2 == -1) {
            xsdFileName = "";
        } else if (idx == -1 && idx2 != -1) {
            xsdFileName = xsdFileName.substring(0, idx2+1);
        } else if (idx != -1  && idx2 == -1) {
            xsdFileName = xsdFileName.substring(0, idx+1);
        } else if (idx != -1  && idx2 != -1) {
            int max = idx2 > idx ? idx2 : idx;
            xsdFileName = xsdFileName.substring(0, max+1);
        }
        xsdFileName += "comments.xsd";
        try {
            FileOutputStream fos = new FileOutputStream(xsdFileName);
            PrintWriter xsdFile = new PrintWriter(fos);
            // The contents of the comments.xsd file
            xsdFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
            xsdFile.println("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
            xsdFile.println();
            xsdFile.println("<xsd:annotation>");
            xsdFile.println("  <xsd:documentation>");
            xsdFile.println("  Schema for JDiff comments.");
            xsdFile.println("  </xsd:documentation>");
            xsdFile.println("</xsd:annotation>");
            xsdFile.println();
            xsdFile.println("<xsd:element name=\"comments\" type=\"commentsType\"/>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentsType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"comment\" type=\"commentType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"jdversion\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"identifier\" type=\"identifierType\" minOccurs='1' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"text\" type=\"xsd:string\" minOccurs='1' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"identifierType\">");
            xsdFile.println("  <xsd:attribute name=\"id\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("</xsd:schema>");
            xsdFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + xsdFileName);
            System.out.println("Error: " +  e.getMessage());
            System.exit(1);
        }
    }

//
// Methods to add data to a Comments object. Called by the XML parser and the 
// report generator.
//

    /**
     * Add the SingleComment object to the list of comments kept by this 
     * object. 
     */
    public void addComment(SingleComment comment) {
        commentsList_.add(comment); 
    }

//
// Methods to get data from a Comments object. Called by the report generator
//

    /** 
     * The text placed into XML comments file where there is no comment yet.
     * It never appears in reports.
     */
    public static final String placeHolderText = "InsertCommentsHere";
    
    /** 
     * Return the comment associated with the given id in the Comment object.
     * If there is no such comment, return the placeHolderText.
     */
    public static String getComment(Comments comments, String id) {
        if (comments == null)
            return placeHolderText;
        SingleComment key = new SingleComment(id, null);
        int idx = Collections.binarySearch(comments.commentsList_, key);
        if (idx < 0) {
            return placeHolderText;
        } else {
            int startIdx = comments.commentsList_.indexOf(key);
            int endIdx = comments.commentsList_.indexOf(key);
            int numIdx = endIdx - startIdx + 1;
            if (numIdx != 1) {
                System.out.println("Warning: " + numIdx + " identical ids in the existing comments file. Using the first instance.");
            }
            SingleComment singleComment = (SingleComment)(comments.commentsList_.get(idx));
            // Convert @link tags to links
            return singleComment.text_;
        }
    }

    /** 
     * Convert @link tags to HTML links. 
     * Doesn't generate links to methods very well due to the difficulty of 
     * generating the required but unspecified signature.
     * It may not work with links to inner classes, and links from one
     * package.html to another package.html file will not work.
     * Links to named anchors and relative links also fail.
     */
    public static String convertAtLinks(String text, String currentElement, 
                                        PackageAPI pkg, ClassAPI cls) {
        if (text == null)
            return null;
        String res = "";
        StringTokenizer st = new StringTokenizer(text, " ,!?\r\n\t");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.compareTo("{@link") == 0) {
                tok = st.nextToken();  // "com.foo.bar}" or "com.foo.bar baz}"
                if (tok.endsWith("}.") || tok.endsWith("}")) {
                    // No label in this link
                    String name = tok.substring(0, tok.length()-1);
                    // Can occur if the link ends }.
                    if (name.endsWith("}"))
                        name = name.substring(0, name.length()-1);
                    String ref = name;
                    if (ref.indexOf(".") == -1) {
                        // Prepend the package and/or class name
                        if (currentElement.compareTo("class") == 0 ||
                            currentElement.compareTo("interface") == 0) {
                            ref = pkg.name_ + "." + cls.name_ + "." + ref;
                        } else if (currentElement.compareTo("package") == 0) {
                            ref = pkg.name_ + "." + ref;
                        }
                    }
                    ref = ref.replace('.', '/');
                    res += " <A HREF=\"" + HTMLReportGenerator.newDocPrefix + 
                        ref + ".html\" target=\"_top\">" + name + "</A>";
                } else {
                    // Expecting a label in this link
                    String ref = tok;
                    ref = ref.replace('.', '/');
                    if (ref.indexOf(".") == -1) {
                        // Prepend the package and/or class name
                        if (currentElement.compareTo("class") == 0 ||
                            currentElement.compareTo("interface") == 0) {
                            ref = pkg.name_ + "." + cls.name_ + "." + ref;
                        } else if (currentElement.compareTo("package") == 0) {
                            ref = pkg.name_ + "." + ref;
                        }
                    }
                    // Get the label
                    if (st.hasMoreTokens()) {
                        tok = st.nextToken();  // "baz}" or "baz}."
                    } else {
                        System.out.println("Warning: incorrectly formatted @link in text: " + text);
                    }
                    String name = tok.substring(0, tok.length()-1);
                    // Can occur if the link ends }.
                    if (name.endsWith("}"))
                        name = name.substring(0, name.length()-1);
                    res += " <A HREF=\"" + HTMLReportGenerator.newDocPrefix +
                        ref + ".html\" target=\"_top\">" + name + "</A>";
                }
            } else {
                res += " " + tok; // Changes whitespace only, trimmed later
            }
        }
        return res.trim();
    }

//
// Methods to write a Comments object out to a file.
//

    /**
     * Write the XML representation of comments to a file.
     *
     * @param outputFileName The name of the comments file.
     * @param oldComments The old comments on the changed APIs.
     * @param newComments The new comments on the changed APIs.
     * @return true if no problems encountered
     */
    public static boolean writeFile(String outputFileName, 
                                    Comments newComments) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFileName);
            outputFile = new PrintWriter(fos);
            newComments.emitXMLHeader(outputFileName);
            newComments.emitComments();
            newComments.emitXMLFooter();
            outputFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + outputFileName);
            System.out.println("Error: "+ e.getMessage());
            System.exit(1);
        }
        return true;
    }
    
    /**
     * Write the Comments object out in XML.
     */
    public void emitComments() {
        Iterator iter = commentsList_.iterator();
        while (iter.hasNext()) {
            SingleComment currComment = (SingleComment)(iter.next());
            if (!currComment.isUsed_)
                outputFile.println("<!-- This comment is no longer used ");
            outputFile.println("<comment>");
            outputFile.println("  <identifier id=\"" + currComment.id_ + "\"/>");
            outputFile.println("  <text>");
            outputFile.println("    " + currComment.text_);
            outputFile.println("  </text>");
            outputFile.println("</comment>");
            if (!currComment.isUsed_)
                outputFile.println("-->");
        }        
    }

    /** 
     * Dump the contents of a Comments object out for inspection.
     */
    public void dump() {
        Iterator iter = commentsList_.iterator();
        int i = 0;
        while (iter.hasNext()) {
            i++;
            SingleComment currComment = (SingleComment)(iter.next());
            System.out.println("Comment " + i);
            System.out.println("id = " + currComment.id_);
            System.out.println("text = \"" + currComment.text_ + "\"");
            System.out.println("isUsed = " + currComment.isUsed_);
        }        
    }

    /**
     * Emit messages about which comments are now unused and which are new.
     */
    public static void noteDifferences(Comments oldComments, Comments newComments) {
        if (oldComments == null) {
            System.out.println("Note: all the comments have been newly generated");
            return;
        }
        
        // See which comment ids are no longer used and add those entries to 
        // the new comments, marking them as unused.
        Iterator iter = oldComments.commentsList_.iterator();
        while (iter.hasNext()) {
            SingleComment oldComment = (SingleComment)(iter.next());
            int idx = Collections.binarySearch(newComments.commentsList_, oldComment);
            if (idx < 0) {
                System.out.println("Warning: comment \"" + oldComment.id_ + "\" is no longer used.");
                oldComment.isUsed_ = false;
                newComments.commentsList_.add(oldComment);
            }
        }        
        
    }
    
    /**
     * Emit the XML header.
     */
    public void emitXMLHeader(String filename) {
        outputFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
        outputFile.println("<comments");
        outputFile.println("  xmlns:xsi='" + RootDocToXML.baseURI + "/2001/XMLSchema-instance'");
        outputFile.println("  xsi:noNamespaceSchemaLocation='comments.xsd'");
        // Extract the identifier from the filename by removing the suffix
        int idx = filename.lastIndexOf('.');
        String apiIdentifier = filename.substring(0, idx);
        // Also remove the output directory and directory separator if present
        if (HTMLReportGenerator.outputDir != null)
            apiIdentifier = apiIdentifier.substring(HTMLReportGenerator.outputDir.length()+1);
        // Also remove "user_comments_for_"
        apiIdentifier = apiIdentifier.substring(18);
        outputFile.println("  name=\"" + apiIdentifier + "\"");
        outputFile.println("  jdversion=\"" + JDiff.version + "\">");
        outputFile.println();
        outputFile.println("<!-- This file contains comments for a JDiff report. -->");
        outputFile.println("<!-- It is used only in generating the report, and does not need to ship with the final report. -->");
        outputFile.println();
        outputFile.println("<!-- The id attribute in an identifier element identifies the change as noted in the report. -->");
        outputFile.println("<!-- An id has the form package[.class[.[ctor|method|field].signature]], where [] indicates optional text. -->");
        outputFile.println("<!-- A comment element can have multiple identifier elements, which will -->");
        outputFile.println("<!-- will cause the same text to appear at each place in the report, but -->");
        outputFile.println("<!-- will be converted to separate comments when the comments file is used. -->");
        outputFile.println("<!-- HTML tags in the text field will appear in the report. -->");
    }

    /**
     * Emit the XML footer.
     */
    public void emitXMLFooter() {
        outputFile.println();
        outputFile.println("</comments>");
    }

    private static List oldAPIList = null;
    private static List newAPIList = null;

    /** 
     * Return true if the given HTML tag has no separate </tag> end element. 
     */
    public static boolean isMinimizedTag(String tag) {
        if (tag.equalsIgnoreCase("p") ||
            tag.equalsIgnoreCase("br") ||
            tag.equalsIgnoreCase("hr") ||
            tag.equalsIgnoreCase("tr") || // Is sometimes minimized
            tag.equalsIgnoreCase("th") || // Is sometimes minimized
            tag.equalsIgnoreCase("td") || // Is sometimes minimized
            tag.equalsIgnoreCase("dt") || // Is sometimes minimized
            tag.equalsIgnoreCase("dd") || // Is sometimes minimized
            tag.equalsIgnoreCase("img") || // Is sometimes minimized
            tag.equalsIgnoreCase("code") || // Is sometimes minimized (error)
            tag.equalsIgnoreCase("font") || // Is sometimes minimized (error)
            tag.equalsIgnoreCase("ul") || // Is sometimes minimized
            tag.equalsIgnoreCase("ol") || // Is sometimes minimized
            tag.equalsIgnoreCase("li") // Is sometimes minimized
            )
            return true;
        return false;
    }

    /** 
     * The file where the XML representing the new Comments object is stored. 
     */
    private static PrintWriter outputFile = null;
    
}


