package jdiff;

import com.sun.javadoc.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*; // Used for invoking Javadoc indirectly
import java.lang.Runtime;
import java.lang.Process;

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
        if (root != null)
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
        if (trace)
            System.out.println("Java version: " + javaVersion);
        if (javaVersion.startsWith("1.1") || 
            javaVersion.startsWith("1.2") || 
            javaVersion.startsWith("1.3")) {
            earlyJDK = true;
        }

        if (args.length != 4) {
            System.out.println("Usage: jdiff jdiff.JDiff <old product name> <old source directory> <new product name> <new source directory>");
            System.out.println("e.g. jdiff jdiff.JDiff SuperProduct1.0 C:\\jdiff\\examples\\SuperProduct1.0 SuperProduct2.0 C:\\jdiff\\examples\\SuperProduct2.0");
            return;
        }
        String oldProductName = args[0];
        String oldSrcDirName = args[1];
        String newProductName = args[2];
        String newSrcDirName = args[3];

        String oldPkgs = getTopDirs(oldSrcDirName);
        String newPkgs = getTopDirs(newSrcDirName);
        if (oldPkgs == null) {
            System.out.println("JDiff: no packages found for the old API");
            return;
        }
        if (newPkgs == null) {
            System.out.println("JDiff: no packages found for the new API");
            return;
        }

        // TODO Add a config file
        // TODO Define the packages more easily

        // Create three separate String[] argument objects for Javadoc
        List oldJavaDocArgs = new ArrayList();
        oldJavaDocArgs.add("-private");
        oldJavaDocArgs.add("-excludeclass");
        oldJavaDocArgs.add("private");
        oldJavaDocArgs.add("-excludemember");
        oldJavaDocArgs.add("private");
        // JDiff arguments
        oldJavaDocArgs.add("-apiname");
        oldJavaDocArgs.add("\"" + oldProductName + "\"");
        oldJavaDocArgs.add("-sourcepath");
        oldJavaDocArgs.add("\"" + oldSrcDirName + "\"");
        if (earlyJDK) {
            StringTokenizer st = new StringTokenizer(oldPkgs, ":");
            while (st.hasMoreTokens()) {
                oldJavaDocArgs.add(st.nextToken());
            }
        } else {
            oldJavaDocArgs.add("-subpackages");
            oldJavaDocArgs.add(oldPkgs);
        }
        String[] oldJavaDocArgsStr = new String[oldJavaDocArgs.size()];
        oldJavaDocArgsStr = (String[])oldJavaDocArgs.toArray(oldJavaDocArgsStr);

        List newJavaDocArgs = new ArrayList();
        newJavaDocArgs.add("-private");
        newJavaDocArgs.add("-excludeclass");
        newJavaDocArgs.add("private");
        newJavaDocArgs.add("-excludemember");
        newJavaDocArgs.add("private");
        // JDiff arguments
        newJavaDocArgs.add("-apiname");
        newJavaDocArgs.add("\"" + newProductName + "\"");
        newJavaDocArgs.add("-sourcepath");
        newJavaDocArgs.add("\"" + newSrcDirName + "\"");
        if (earlyJDK) {
            StringTokenizer st = new StringTokenizer(newPkgs, ":");
            while (st.hasMoreTokens()) {
                newJavaDocArgs.add(st.nextToken());
            }
        } else {
            newJavaDocArgs.add("-subpackages");
            newJavaDocArgs.add(newPkgs);
        }
        String[] newJavaDocArgsStr = new String[newJavaDocArgs.size()];
        newJavaDocArgsStr = (String[])newJavaDocArgs.toArray(newJavaDocArgsStr);

        String programName = "JDiff";
        String defaultDocletClassName = "jdiff.JDiff";

        // First generate the XML for the old API
        System.out.println("JDiff: Step 1 of 3. Creating XML representation of the old API");
        int rc = runJavadoc(programName, defaultDocletClassName, oldJavaDocArgsStr);
        if (rc != 0)
            return;

        // Then generate the XML for the new API
        System.out.println("JDiff: Step 2 of 3. reating XML representation of the old API");
        int rc2 = runJavadoc(programName, defaultDocletClassName, newJavaDocArgsStr);
        if (rc2 != 0)
            return;

        // Finally use the two XML files to generate the HTML report of 
        // the differences between the two APIs. 
        System.out.println("JDiff: Step 3 of 3. Comparing APIs");
        JDiff.compareAPIs = true;
        JDiff.writeXML = false;
        // This doesn't call Javadoc, so set the variables directly.
        // TODO would be neater to use the Options class
        HTMLReportGenerator.doStats = true; // -stats
        oldProductName = oldProductName.replace(' ', '_');
        JDiff.oldFileName = oldProductName + ".xml"; // -oldapi
        newProductName = newProductName.replace(' ', '_');
        JDiff.newFileName = newProductName + ".xml"; // -newapi
        // Call the doclet start method directly.
        start(null);
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
        if (earlyJDK) {
            // Assumes that the class files for JDiff are in the classpath
            // which was used to invoke JDiff directly. This does not work
            // from inside an applet.
            String allArgs = "javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath " + System.getProperty("java.class.path");
            for (int i = 0; i < args.length; i++) {
                allArgs += (" " + args[i]);
            }
            System.out.println("Javadoc command line:");
            System.out.println(allArgs);
        
            Process proc  = null;
            int retCode = -1;
            try {
                proc = Runtime.getRuntime().exec(allArgs);
                StreamReader op = new StreamReader(proc.getInputStream());
                StreamReader err = new StreamReader(proc.getErrorStream());
                op.start();
                err.start();
                retCode = proc.waitFor();
                op = null;
                err = null;
            } catch (SecurityException sex) {
                System.out.println("Permission error invoking Javadoc");
                sex.printStackTrace();
            } catch (IOException ioex) {
                System.out.println("IO Error invoking Javadoc");
                ioex.printStackTrace();
            } catch (InterruptedException iex) {
                // Do nothing
            }
            System.gc(); // Clean up after running Javadoc
            return retCode;
        }
        
        // Call Javadoc directly (this is only possible from J2SE1.4 onwards).
        System.out.println("Javadoc command line arguments:");
        for (int i = 0; i < args.length; i++) {
            System.out.print(" " + args[i]);
        }
        System.out.println();
        
        String className = null;
        try {
            className = "com.sun.tools.javadoc.Main";
            Class c = Class.forName(className);
            Class[] methodArgTypes = new Class[3];
            methodArgTypes[0] = String.class;
            methodArgTypes[1] = String.class;
            methodArgTypes[2] = args.getClass();
            Method javaDocMethod = c.getMethod("execute", methodArgTypes);
            Object[] methodArgs = new Object[3];
            methodArgs[0] = programName;
            methodArgs[1] = defaultDocletClassName;
            methodArgs[2] = args;
            // The object can be null because the method is static
            Integer res = (Integer)javaDocMethod.invoke(null, methodArgs);
            System.gc(); // Clean up after running Javadoc
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
        System.gc(); // Clean up after running Javadoc
        return -1;
    }

    /**
     * Create the lists of top-level directories in the given source
     * directories.
     *
     * @return A String with the top-level directories separated by colons,
     *         or null if none are found.
     */
    public static String getTopDirs(String dirName) {
        String res = null;
        boolean firstPkg = true;
        File dir = new File(dirName);
        if (dir.isDirectory()) {
            String[] packages = dir.list();
            for (int i = 0; i < packages.length; i++) {
                File potentialPkg = new File(dir + JDiff.DIR_SEP + packages[i]);
                if (potentialPkg.isDirectory() && packages[i].compareTo("CVS") != 0) {
                    if (firstPkg) {
                        res = packages[i];
                        firstPkg = false;
                    } else {
                        res += ":" + packages[i];
                    }
                }
            }
        }
        return res;
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

    /** The current JVM version. */
    static String javaVersion = System.getProperty("java.version");

    /** Set if the JDK version is prior to JDK1.4. */
    static boolean earlyJDK = false;;

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;

} //JDiff
