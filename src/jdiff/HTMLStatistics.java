package jdiff;

import java.util.*;
import java.io.*;

/**
 * Emit an HTML file containing statistics about the differences.
 * Statistical information only appears if the -stats argument is used.
 *
 * @author Matthew Doar, doar@pobox.com
 */
public class HTMLStatistics {

    /** Constructor. */
    public HTMLStatistics(HTMLReportGenerator h) {
        h_ = h;
    }   

    /** The HTMLReportGenerator instance used to write HTML. */
    private HTMLReportGenerator h_ = null;

    /** 
     * Emit the statistics HTML file.
     */
    public void emitStatistics(String filename, APIDiff apiDiff) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            h_.reportFile = new PrintWriter(fos);
            // Write out the HTML header
            h_.writeStartHTMLHeader();
            // Write out the title
            h_.writeHTMLTitle("JDiff Statistics");
            h_.writeStyleSheetRef();
            h_.writeText("</HEAD>");
            h_.writeText("<BODY>");

            // Write a customized navigation bar for the statistics page
            h_.writeText("<!-- Start of nav bar -->");
            h_.writeText("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">");
            h_.writeText("<TR>");
            h_.writeText("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">");
            h_.writeText("  <TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">");
            h_.writeText("    <TR ALIGN=\"center\" VALIGN=\"top\">");
            // Always have a link to the Javadoc files
            h_.writeText("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + h_.newDocPrefix + "index.html\" target=\"_top\"><FONT CLASS=\"NavBarFont1\"><B><tt>" + apiDiff.newAPIName_ + "</tt></B></FONT></A>&nbsp;</TD>");
            h_.writeText("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + h_.reportFileName + "-summary" + h_.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>");
            h_.writeText("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>");
            h_.writeText("      <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>");
            h_.writeText("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\"> &nbsp;<FONT CLASS=\"NavBarFont1Rev\"><B>Statistics</B></FONT>&nbsp;</TD>");
            h_.writeText("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_help" + h_.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>");
            h_.writeText("    </TR>");
            h_.writeText("  </TABLE>");
            h_.writeText("</TD>");

            // The right hand side title
            h_.writeText("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM><b>Generated by<br><a href=\"" + JDiff.jDiffLocation + "\" class=\"staysblack\" target=\"_top\">JDiff</a></b></EM></TD>");
            h_.writeText("</TR>");
            
            // Links for frames and no frames
            h_.writeText("<TR>");
            h_.writeText("  <TD BGCOLOR=\"" + h_.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
            h_.writeText("</TD>");
            h_.writeText("  <TD BGCOLOR=\"" + h_.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
            h_.writeText("  <A HREF=\"" + "../" + h_.reportFileName + h_.reportFileExt + "\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;");
            h_.writeText("  &nbsp;<A HREF=\"jdiff_statistics" + h_.reportFileExt + "\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>");
            h_.writeText("</TR>");
            
            h_.writeText("</TABLE>");
            h_.writeText("<HR>");
            h_.writeText ("<!-- End of nav bar -->");

            h_.writeText("<center>");        
            h_.writeText("<H1>JDiff Statistics</H1>");
            h_.writeText("</center>");        

            h_.writeText("<BLOCKQUOTE>");
            h_.writeText("The percent change statistic reported for all elements in each API is defined recursively as follows:<br>");
            h_.writeText("<pre>"); 
            h_.writeText("Percentage difference = 100 * (added + removed + 2*changed)");
            h_.writeText("                        -----------------------------------");
            h_.writeText("                        sum of public elements in BOTH APIs");
            h_.writeText("</pre>"); 
            h_.writeText("Where <code>added</code> is the number of packages added, <code>removed</code> is the number of packages removed, and <code>changed</code> is the number of packages changed.");
            h_.writeText("This definition is applied recursively for the classes and their program elements, so the value for a changed package will be less than 1, unless every class in that package has changed.");
            h_.writeText("The definition ensures that if all packages are removed and all new packages are");
            h_.writeText("added, the change will be 100%. Values are rounded here, so a value of 0% indicates a percentage difference of less than 0.5%.");

            h_.writeText("<p>The overall difference between the two APIs is approximately " + (int)(apiDiff.pdiff) + "%.");
            h_.writeText("</BLOCKQUOTE>");

            h_.writeText("<h3>Sections</h3>");
            h_.writeText("<a href=\"#packages\">Packages</a> sorted by percentage difference<br>");
            h_.writeText("<a href=\"#classes\">Classes and <i>Interfaces</i></a> sorted by percentage difference<br>");
            h_.writeText("<a href=\"#numbers\">Differences</a> by number and type<br>");

            h_.writeText("<hr>");
            h_.writeText("<a name=\"packages\"></a>");
            h_.writeText("<h2>Packages Sorted By Percentage Difference</h2>");
            emitPackagesByDiff(apiDiff);

            h_.writeText("<hr>");
            h_.writeText("<a name=\"classes\"></a>");
            h_.writeText("<h2>Classes and <i>Interfaces</i> Sorted By Percentage Difference</h2>");
            emitClassesByDiff(apiDiff);

            h_.writeText("<hr>");
            h_.writeText("<a name=\"numbers\"></a>");
            h_.writeText("<h2>Differences By Number and Type</h2>");
            h_.writeText("<BLOCKQUOTE>");
            // TODO should there be an option to include the sub totals?
            h_.writeText("The numbers of program elements (packages, classes. constructors, methods and fields) which are recorded as removed, added or changed includes only the highest-level program elements. That is, if a class with two methods was added, the number of methods added does not include those two methods, but the number of classes added does include that class.");
            h_.writeText("</BLOCKQUOTE>");

            emitNumbersByElement(apiDiff);

            h_.writeText("</HTML>");
            h_.reportFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + filename);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Emit all packages sorted by percentage difference, and a histogram
     * of the values.
     */
    public void emitPackagesByDiff(APIDiff apiDiff) {
        
        Collections.sort(apiDiff.packagesChanged, new ComparePkgPdiffs());

        // Write out the table start
        h_.writeText("<TABLE BORDER=\"1\" WIDTH=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("<TR WIDTH=\"20%\">");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage<br>Difference</b></FONT></TD>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Package</b></FONT></TD>");
        h_.writeText("</TR>");

        int[] hist = new int[101];
        for (int i = 0; i < 101; i++) {
            hist[i] = 0;
        }

        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkg = (PackageDiff)(iter.next());
            int bucket = (int)(pkg.pdiff);
            hist[bucket]++;
            h_.writeText("<TR>");
            if (bucket != 0)
                h_.writeText("  <TD ALIGN=\"center\">" + bucket + "</TD>");
            else
                h_.writeText("  <TD ALIGN=\"center\">&lt;1</TD>");
            h_.writeText("  <TD><A HREF=\"pkg_" + pkg.name_ + h_.reportFileExt + "\">" + pkg.name_ + "</A></TD>");
            h_.writeText("</TR>");
        }

        h_.writeText("</TABLE>");
        
        // Emit the histogram of the results
        h_.writeText("<hr>");
        h_.writeText("<p><a name=\"packages_hist\"></a>");
        h_.writeText("<TABLE BORDER=\"1\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("<TR>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage<br>Difference</b></FONT></TD>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Frequency</b></FONT></TD>");
        h_.writeText("  <TD width=\"300\" ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage Frequency</b></FONT></TD>");
        h_.writeText("</TR>");

        double total = 0;
        for (int i = 0; i < 101; i++) {
            total += hist[i];
        }
        for (int i = 0; i < 101; i++) {
            if (hist[i] != 0) {
                h_.writeText("<TR>");
                h_.writeText("  <TD ALIGN=\"center\">" + i + "</TD>");
                h_.writeText("  <TD>" + (hist[i]/total) + "</TD>");
                h_.writeText("  <TD><img src=\"black.gif\" height=20 width=" + (hist[i]*300/total) + "></TD>");
                h_.writeText("</TR>");
            }
        }
        // Repeat the data in a format which is easier for spreadsheets
        h_.writeText("<!-- START_PACKAGE_HISTOGRAM");
        for (int i = 0; i < 101; i++) {
            if (hist[i] != 0) {
                h_.writeText(i + "," + (hist[i]/total));
            }
        }
        h_.writeText("END_PACKAGE_HISTOGRAM -->");
        
        h_.writeText("</TABLE>");
    }

    /**
     * Emit all classes sorted by percentage difference, and a histogram
     * of the values..
     */
    public void emitClassesByDiff(APIDiff apiDiff) {
        // Add all the changed classes to a list
        List allChangedClasses = new ArrayList();
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkg = (PackageDiff)(iter.next());
            if (pkg.classesChanged != null) {
                // Add the package name to the class name
                List cc = new ArrayList(pkg.classesChanged);
                Iterator iter2 = cc.iterator();
                while (iter2.hasNext()) {
                    ClassDiff classDiff = (ClassDiff)(iter2.next());
                    classDiff.name_ = pkg.name_ + "." + classDiff.name_;
                }
                allChangedClasses.addAll(cc);
            }
        }
        Collections.sort(allChangedClasses, new CompareClassPdiffs());

        // Write out the table start
        h_.writeText("<TABLE BORDER=\"1\" WIDTH=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("<TR WIDTH=\"20%\">");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage<br>Difference</b></FONT></TD>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Class or <i>Interface</i></b></FONT></TD>");
        h_.writeText("</TR>");

        int[] hist = new int[101];
        for (int i = 0; i < 101; i++) {
            hist[i] = 0;
        }

        iter = allChangedClasses.iterator();
        while (iter.hasNext()) {
            ClassDiff classDiff = (ClassDiff)(iter.next());
            int bucket = (int)(classDiff.pdiff);
            hist[bucket]++;
            h_.writeText("<TR>");
            if (bucket != 0)
                h_.writeText("  <TD ALIGN=\"center\">" + bucket + "</TD>");
            else
                h_.writeText("  <TD ALIGN=\"center\">&lt;1</TD>");
            h_.writeText("  <TD><A HREF=\"" + classDiff.name_ + h_.reportFileExt + "\">");
            if (classDiff.isInterface_)
                h_.writeText("<i>" + classDiff.name_ + "</i></A></TD>");
            else
                h_.writeText(classDiff.name_ + "</A></TD>");
            h_.writeText("</TR>");
        }

        h_.writeText("</TABLE>");

        // Emit the histogram of the results
        h_.writeText("<hr>");
        h_.writeText("<p><a name=\"classes_hist\"></a>");
        h_.writeText("<TABLE BORDER=\"1\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("<TR>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage<br>Difference</b></FONT></TD>");
        h_.writeText("  <TD ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Frequency</b></FONT></TD>");
        h_.writeText("  <TD width=\"300\" ALIGN=\"center\" bgcolor=\"#EEEEFF\"><FONT size=\"+1\"><b>Percentage Frequency</b></FONT></TD>");
        h_.writeText("</TR>");

        double total = 0;
        for (int i = 0; i < 101; i++) {
            total += hist[i];
        }
        for (int i = 0; i < 101; i++) {
            if (hist[i] != 0) {
                h_.writeText("<TR>");
                h_.writeText("  <TD ALIGN=\"center\">" + i + "</TD>");
                h_.writeText("  <TD>" + (hist[i]/total) + "</TD>");
                h_.writeText("  <TD><img src=\"black.gif\" height=20 width=" + (hist[i]*300/total) + "></TD>");
                h_.writeText("</TR>");
            }
        }
        // Repeat the data in a format which is easier for spreadsheets
        h_.writeText("<!-- START_CLASS_HISTOGRAM");
        for (int i = 0; i < 101; i++) {
            if (hist[i] != 0) {
                h_.writeText(i + "," + (hist[i]/total));
            }
        }
        h_.writeText("END_CLASS_HISTOGRAM -->");
        
        h_.writeText("</TABLE>");
    }

    /**
     * Emit a table of numbers of removals, additions and changes by
     * package, class, constructor, method and field.
     */
    public void emitNumbersByElement(APIDiff apiDiff) {

        // Local variables to hold the values
        int numPackagesRemoved = apiDiff.packagesRemoved.size();
        int numPackagesAdded = apiDiff.packagesAdded.size();
        int numPackagesChanged = apiDiff.packagesChanged.size();

        int numClassesRemoved = 0;
        int numClassesAdded = 0;
        int numClassesChanged = 0;

        int numCtorsRemoved = 0;
        int numCtorsAdded = 0;
        int numCtorsChanged = 0;

        int numMethodsRemoved = 0;
        int numMethodsAdded = 0;
        int numMethodsChanged = 0;

        int numFieldsRemoved = 0;
        int numFieldsAdded = 0;
        int numFieldsChanged = 0;

        int numRemoved = 0;
        int numAdded = 0;
        int numChanged = 0;

        // Calculate the values
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkg = (PackageDiff)(iter.next());
            numClassesRemoved += pkg.classesRemoved.size();
            numClassesAdded += pkg.classesAdded.size();
            numClassesChanged += pkg.classesChanged.size();

            Iterator iter2 = pkg.classesChanged.iterator();
            while (iter2.hasNext()) {
                 ClassDiff classDiff = (ClassDiff)(iter2.next());
                 numCtorsRemoved += classDiff.ctorsRemoved.size();
                 numCtorsAdded += classDiff.ctorsAdded.size();
                 numCtorsChanged += classDiff.ctorsChanged.size();
                 
                 numMethodsRemoved += classDiff.methodsRemoved.size();
                 numMethodsAdded += classDiff.methodsAdded.size();
                 numMethodsChanged += classDiff.methodsChanged.size();
                 
                 numFieldsRemoved += classDiff.fieldsRemoved.size();
                 numFieldsAdded += classDiff.fieldsAdded.size();
                 numFieldsChanged += classDiff.fieldsChanged.size();
            }
        }
        

        // Write out the table
        h_.writeText("<TABLE BORDER=\"1\" WIDTH=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("<TR>");
        h_.writeText("  <TD COLSPAN=5 ALIGN=\"center\" NOWRAP bgcolor=\"#EEEEFF\"><FONT size=\"+1\">");
        h_.writeText("  <B>Number of Differences</B></FONT></TD>");
        h_.writeText("</TR>");
        h_.writeText("<TR>");
        h_.writeText("  <TD>&nbsp;</TD>");
        h_.writeText("  <TD ALIGN=\"center\"><b>Removals</b></TD>");
        h_.writeText("  <TD ALIGN=\"center\"><b>Additions</b></TD>");
        h_.writeText("  <TD ALIGN=\"center\"><b>Changes</b></TD>");
        h_.writeText("  <TD ALIGN=\"center\"><b>Total</b></TD>");
        h_.writeText("</TR>");

        h_.writeText("<TR>");
        h_.writeText("  <TD>Packages</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numPackagesRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numPackagesAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numPackagesChanged + "</TD>");
        int numPackages = numPackagesRemoved + numPackagesAdded + numPackagesChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + numPackages + "</TD>");
        h_.writeText("</TR>");

        numRemoved += numPackagesRemoved;
        numAdded += numPackagesAdded;
        numChanged += numPackagesChanged;

        h_.writeText("<TR>");
        h_.writeText("  <TD>Classes and <i>Interfaces</i></TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numClassesRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numClassesAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numClassesChanged + "</TD>");
        int numClasses = numClassesRemoved + numClassesAdded + numClassesChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + numClasses + "</TD>");
        h_.writeText("</TR>");

        numRemoved += numClassesRemoved;
        numAdded += numClassesAdded;
        numChanged += numClassesChanged;

        h_.writeText("<TR>");
        h_.writeText("  <TD>Constructors</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numCtorsRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numCtorsAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numCtorsChanged + "</TD>");
        int numCtors = numCtorsRemoved + numCtorsAdded + numCtorsChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + numCtors + "</TD>");
        h_.writeText("</TR>");

        numRemoved += numCtorsRemoved;
        numAdded += numCtorsAdded;
        numChanged += numCtorsChanged;

        h_.writeText("<TR>");
        h_.writeText("  <TD>Methods</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numMethodsRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numMethodsAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numMethodsChanged + "</TD>");
        int numMethods = numMethodsRemoved + numMethodsAdded + numMethodsChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + numMethods + "</TD>");
        h_.writeText("</TR>");

        numRemoved += numMethodsRemoved;
        numAdded += numMethodsAdded;
        numChanged += numMethodsChanged;

        h_.writeText("<TR>");
        h_.writeText("  <TD>Fields</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numFieldsRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numFieldsAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numFieldsChanged + "</TD>");
        int numFields = numFieldsRemoved + numFieldsAdded + numFieldsChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + numFields + "</TD>");
        h_.writeText("</TR>");

        numRemoved += numFieldsRemoved;
        numAdded += numFieldsAdded;
        numChanged += numFieldsChanged;

        h_.writeText("<TR>");
        h_.writeText("  <TD><b>Total</b></TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numRemoved + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numAdded + "</TD>");
        h_.writeText("  <TD ALIGN=\"right\">" + numChanged + "</TD>");
        int total = numRemoved + numAdded + numChanged;
        h_.writeText("  <TD ALIGN=\"right\">" + total + "</TD>");
        h_.writeText("</TR>");

        h_.writeText("</TABLE>");
    }

}
