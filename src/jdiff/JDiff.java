package jdiff;

import com.sun.javadoc.*;
import com.sun.tools.doclets.HtmlWriter;
import com.sun.tools.doclets.standard.Standard;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 * Generates HTML describing the changes between two sets of Java source code.
 *
 * @author Matthew Doar, doar@pobox.com.
 * @version 1.0
 */
public class JDiff extends Doclet {

    /**
     * Doclet-mandated start method. Everything begins here.
     *
     * @param root  a RootDoc object passed by JavaDoc
     * @return true if document generation succeeds
     */
    public static boolean start(RootDoc root) {
        System.out.println("JDiff: doclet started ...");
        JDiff jd = new JDiff();
        return jd.startGeneration(root);
    }

    /**
     * Generate the summary of the APIs.
     *
     * @param root  the RootDoc object passed by JavaDoc
     * @return true if no problems encountered within JDiff
     */
    protected boolean startGeneration(RootDoc newRoot) {
        long startTime = System.currentTimeMillis();

        // Open the file where the XML representing the new API will be stored.
        // and generate the XML for the new API into it.
        if (writeXML) {
            System.out.println("JDiff: writing the new API to file '" + RootDocToXML.outputFileName + "'...");
            RootDocToXML.writeXML(newRoot);           
        }

        if (compareAPIs) {
            // Check the file for the old API exists
            File f = new File(oldFileName);
            if (!f.exists()) {
                System.out.println("Error: file '" + oldFileName + "' does not exist for the old API");
                return false;
            }
            // Check the file for the new API exists
            f = new File(newFileName);
            if (!f.exists()) {
                System.out.println("Error: file '" + newFileName + "' does not exist for the new API");
                return false;
            }

            // Read the file where the XML representing the old API is stored
            // and create an API object for it.
            System.out.print("JDiff: reading the old API in from file '" + oldFileName + "'...");
            // Read the file in, but do not add any text to the global comments
            API oldAPI = XMLToAPI.readFile(oldFileName, false);
            
            // Read the file where the XML representing the new API is stored
            // and create an API object for it.
            System.out.print("JDiff: reading the new API in from file '" + newFileName + "'...");
            // Read the file in, and do add any text to the global comments
            API newAPI = XMLToAPI.readFile(newFileName, true);
            
            // Compare the old and new APIs.
            APIComparator comp = new APIComparator();
            
            comp.compareAPIs(oldAPI, newAPI);
            
            // Read the file where the XML for comments about the changes between
            // the old API and new API is stored and create a Comments object for 
            // it. The Comments object may be null if no file exists.
            int suffix = oldFileName.lastIndexOf('.');
            String commentsFileName = "user_comments_for_" + oldFileName.substring(0, suffix);
            suffix = newFileName.lastIndexOf('.');
            commentsFileName += "_to_" + newFileName.substring(0, suffix) + ".xml";
            commentsFileName = commentsFileName.replace(' ', '_');
            if (HTMLReportGenerator.outputDir != null)
                commentsFileName = HTMLReportGenerator.outputDir + DIR_SEP + commentsFileName;
            System.out.println("JDiff: reading the comments in from file '" + commentsFileName + "'...");
            Comments existingComments = Comments.readFile(commentsFileName);
            if (existingComments == null)
                System.out.println(" (this will be created)");
            
            // Generate an HTML report which summarises all the API differences.
            HTMLReportGenerator reporter = new HTMLReportGenerator();
            reporter.generate(comp, existingComments);
            
            // Emit messages about which comments are now unused and
            // which are new.
            Comments newComments = reporter.getNewComments();
            Comments.noteDifferences(existingComments, newComments);
            
            // Write the new comments out to the same file, with unused comments
            // now commented out.
            System.out.println("JDiff: writing the comments out to file '" + commentsFileName + "'...");
            Comments.writeFile(commentsFileName, newComments);
        }

        System.out.print("JDiff: finished (took " + (System.currentTimeMillis() - startTime)/1000 + "s");
        if (writeXML)
            System.out.println(", not including scanning the source files)."); 
        else if (compareAPIs)
            System.out.println(").");
       return true;
    }

//
// Option processing
// 

    /**
     * This method is called by JavaDoc to
     * parse the options it does not recognize. It then calls
     * {@link #validOptions} to validate them.
     *
     * @param option  a String containing an option
     * @return an int telling how many components that option has
     */
    public static int optionLength(String option) {
        return Options.optionLength(option);
    }

    /**
     * After parsing the available options using {@link #optionLength},
     * JavaDoc invokes this method with an array of options-arrays.
     *
     * @param options   an array of String arrays, one per option
     * @param reporter  a DocErrorReporter for generating error messages
     * @return true if no errors were found, and all options are
     *         valid
     */
    public static boolean validOptions(String[][] options, 
                                       DocErrorReporter reporter) {
        return Options.validOptions(options, reporter);
    }
    
    /** 
     * The name of the file where the XML representing the old API is
     * stored. 
     */
    static String oldFileName = "old_java.xml";

    /** 
     * The name of the file where the XML representing the new API is 
     * stored. 
     */
    static String newFileName = "new_java.xml";

    /** If set, then generate the XML for an API and exit. */
    static boolean writeXML = false;

    /** If set, then read in two XML files and compare their APIs. */
    static boolean compareAPIs = false;

    /** 
     * The file separator for the local filesystem, forward or backward slash. 
     */
    static String DIR_SEP = System.getProperty("file.separator");

    /** Details for where to find JDiff. */
    static final String jDiffLocation = "http://www.pobox.com/~doar/src/jdiff";
    /** Contact email address for the JDiff maintainer. */
    static final String authorEmail = "doar@pobox.com";

    /** The current JDiff version. */
    static final String version = "1.0.3";

} //JDiff
