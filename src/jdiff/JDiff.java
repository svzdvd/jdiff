package jdiff;

import com.sun.javadoc.*;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.lang.reflect.*; // Used for invoking Javadoc indirectly

/**
 * Generates HTML describing the changes between two sets of Java source code.
 *
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com.
 */
public class JDiff extends Doclet {

    /**
     * Doclet-mandated start method. Everything begins here.
     *
     * @param root  a RootDoc object passed by Javadoc
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
     * @param root  the RootDoc object passed by Javadoc
     * @return true if no problems encountered within JDiff
     */
    protected boolean startGeneration(RootDoc newRoot) {
        long startTime = System.currentTimeMillis();

        // Open the file where the XML representing the API will be stored.
        // and generate the XML for the API into it.
        if (writeXML) {
            System.out.println("JDiff: writing the API to file '" + RootDocToXML.outputFileName + "'...");
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
     * This method is called by Javadoc to
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
     * Javadoc invokes this method with an array of options-arrays.
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
     * This method is only called when running JDiff as a standalone
     * application. Since this calls Javadoc directly, the use of J2SE1.4 
     * or later is implied.
     */
    public static void main(String[] args) {
        // TODO the fixed arguments below work for the test cases only
        // TODO echo out the arguments for ease of use
        // Create three separate String[] argument objects for Javadoc
        String[] oldJavaDocArgs = new String[13];
        oldJavaDocArgs[0] = "-private";
        oldJavaDocArgs[1] = "-excludeclass";
        oldJavaDocArgs[2] = "private";
        oldJavaDocArgs[3] = "-excludemember";
        oldJavaDocArgs[4] = "private";
        // JDiff arguments
        oldJavaDocArgs[5] = "-apiname";
        oldJavaDocArgs[6] = "Old Test API";
        oldJavaDocArgs[7] = "-sourcepath";
        oldJavaDocArgs[8] = "old";
        oldJavaDocArgs[9] = "RemovedPackage";
        oldJavaDocArgs[10] = "ChangedPackage";
        oldJavaDocArgs[11] = "ChangedPackageDoc";
        oldJavaDocArgs[12] = "ChangedPackageDoc2";

        String[] newJavaDocArgs = new String[13];
        newJavaDocArgs[0] = "-private";
        newJavaDocArgs[1] = "-excludeclass";
        newJavaDocArgs[2] = "private";
        newJavaDocArgs[3] = "-excludemember";
        newJavaDocArgs[4] = "private";
        // JDiff arguments
        newJavaDocArgs[5] = "-apiname";
        newJavaDocArgs[6] = "New Test API";
        newJavaDocArgs[7] = "-sourcepath";
        newJavaDocArgs[8] = "new";
        newJavaDocArgs[9] = "AddedPackage";
        newJavaDocArgs[10] = "ChangedPackage";
        newJavaDocArgs[11] = "ChangedPackageDoc";
        newJavaDocArgs[12] = "ChangedPackageDoc2";

        String[] diffJavaDocArgs = new String[12];
        diffJavaDocArgs[0] = "-d";
        diffJavaDocArgs[1] = "newdocs";
        // JDiff arguments
        diffJavaDocArgs[2] = "-stats";
        diffJavaDocArgs[3] = "-oldapi";
        diffJavaDocArgs[4] = "Old Test API";
        diffJavaDocArgs[5] = "-newapi";
        diffJavaDocArgs[6] = "New Test API";
        diffJavaDocArgs[7] = "-javadocold";
        diffJavaDocArgs[8] = "../../olddocs/";
        diffJavaDocArgs[9] = "-javadocnew";
        diffJavaDocArgs[10] = "../../newdocs/";
        diffJavaDocArgs[11] = "../lib/Null.java";

        String javaVersion = System.getProperty("java.version");
        if (trace)
            System.out.println("Java version: " + javaVersion);
        if (javaVersion.startsWith("1.1") || 
            javaVersion.startsWith("1.2") || 
            javaVersion.startsWith("1.3")) {
            System.out.println("Error: cannot run jdiff.JDiff directly with J2SE version " + javaVersion + ", since it is earlier than version 1.4. Call javadoc directly instead.");
            return;
        }

        // First generate the XML for the old API
        int rc = runJavadoc("JDiff", "jdiff.JDiff", oldJavaDocArgs);
        if (rc != 0)
            return;

        // Then generate the XML for the new API
        int rc2 = runJavadoc("JDiff", "jdiff.JDiff", newJavaDocArgs);
        if (rc2 != 0)
            return;

        // Finally use the two XML files to generate the HTML report of 
        // the differences between the two APIs.
        JDiff.compareAPIs = false;
        JDiff.writeXML = false;
        int rc3 = runJavadoc("JDiff", "jdiff.JDiff", diffJavaDocArgs);
        if (rc3 != 0)
            return;
    }

    /** 
     * Invoke Javadoc by reflection, so that J2SE1.3 can compile code which
     * uses methods defined in J2SE1.4. 
     *
     * @return The integer return code from running Javadoc.
     */
    public static int runJavadoc(String programName, 
                             String defaultDocletClassName, 
                             String[] args) {
        String className = null;
        try {
            className = "com.sun.tools.javadoc.Main";
            Class c = Class.forName(className);
            Class[] methodArgTypes = new Class[3];
            methodArgTypes[0] = String.class;
            methodArgTypes[1] = String.class;
            methodArgTypes[2] = args.getClass();
            Method javaDocMethod  = c.getMethod("execute", methodArgTypes);
            Object[] methodArgs = new Object[3];
            methodArgs[0] = programName;
            methodArgs[1] = defaultDocletClassName;
            methodArgs[2] = args;
            // The object can be null because the method is static
            Integer res = (Integer)javaDocMethod.invoke(null, methodArgs);
            return res.intValue();
        } catch (ClassNotFoundException e1) {
            System.err.println("Error: class \"" + className + "\"not found");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            System.err.println("Error: method \"execute\" not found");
            e2.printStackTrace();
        } catch (IllegalAccessException e4) {
            System.err.println("Error: class not permitted to be instantiated");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            System.err.println("Error: method \"execute\" could not be invoked");
            e5.printStackTrace();
        } catch (Exception e6) {
            System.err.println("Error: ");
            e6.printStackTrace();
        }
        return -1;
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

    /** A description for HTML META tags. */
    static final String jDiffDescription = "JDiff is a Javadoc doclet which generates an HTML report of all the packages, classes, constructors, methods, and fields which have been removed, added or changed in any way, including their documentation, when two APIs are compared.";
    /** Keywords for HTML META tags. */
    static final String jDiffKeywords = "diff, jdiff, javadiff, java diff, java difference, API difference, API diff, Javadoc, doclet";

    /** The current JDiff version. */
    static final String version = "1.0.6";

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;

} //JDiff
