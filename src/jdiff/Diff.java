package jdiff;

import java.io.*;
import java.util.*;

/** 
 * Class to generate colored differences between two sections of HTML text.
 *
 * @author Matthew Doar, doar@pobox.com
 */
class Diff {

    /** 
     * Emit the differences between the two strings into the file
     * for the current package.
     * 
     * @param id A unique identifier for each documentation 
     *           change.
     */ 
    static String emitDocDiffs(String oldDoc, String newDoc, String id) {
        // Generate the string which will link to this set of diffs
        if (noDocDiffs)
            return "Documentation changed from ";
        if (oldDoc == null || newDoc == null) {
            return "Documentation changed from ";
        }

        if (diffFile == null) {
            // Create the name for the output file
            String fullReportFileName = HTMLReportGenerator.reportFileName;
            if (HTMLReportGenerator.outputDir != null)
                fullReportFileName = HTMLReportGenerator.outputDir + 
                    JDiff.DIR_SEP + 
                    HTMLReportGenerator.reportFileName;
            // Create the directory if it doesn't exist
            File opdir = new File(fullReportFileName);
            if (!opdir.mkdir() && !opdir.exists()) {
                System.out.println("Error: could not create the subdirectory '" + fullReportFileName + "'");
                System.exit(3);
            }
            String fullDiffFileName = fullReportFileName + 
                JDiff.DIR_SEP + diffFileName + 
                HTMLReportGenerator.reportFileExt;
            // Create the output file
            try {
                FileOutputStream fos = new FileOutputStream(fullDiffFileName);
                diffFile = new PrintWriter(fos);

                // Write the HTML header
                diffFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"\"http://www.w3.org/TR/REC-html40/frameset.dtd\">");
                diffFile.println("<HTML>");
                diffFile.println("<HEAD>");
                diffFile.println("<meta name=\"generator\" content=\"JDiff v" + JDiff.version + "\">");
                diffFile.println("<!-- Generated by the JDiff JavaDoc doclet -->");
                diffFile.println("<!-- (" + JDiff.jDiffLocation + ") -->");
                diffFile.println("<!-- on " + new Date() + " -->");
                diffFile.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + "../" + "stylesheet-jdiff.css\" TITLE=\"Style\">");
                diffFile.println("</HEAD>");
                diffFile.println("<BODY>");
                
                diffFile.println("<TITLE>");
                diffFile.println("Documentation Differences");
                diffFile.println("</TITLE>");

                // Write the navigation bar
                diffFile.println("<!-- Start of nav bar -->");
                diffFile.println("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">");
                diffFile.println("<TR>");
                diffFile.println("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">");
                diffFile.println("  <TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">");
                diffFile.println("    <TR ALIGN=\"center\" VALIGN=\"top\">");
                // Always have a link to the Javadoc files
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + HTMLReportGenerator.newDocPrefix + "index.html\" target=\"_top\"><FONT CLASS=\"NavBarFont1\"><B><tt>" + APIDiff.newAPIName_ + "</tt></B></FONT></A>&nbsp;</TD>");
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + HTMLReportGenerator.reportFileName + "-summary" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>");
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>");
                diffFile.println("      <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>");
                if (HTMLReportGenerator.doStats) {
                    diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_statistics" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Statistics</B></FONT></A>&nbsp;</TD>");
                }
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_help" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>");
                diffFile.println("    </TR>");
                diffFile.println("  </TABLE>");
                diffFile.println("</TD>");
                
                // The right hand side title
                diffFile.println("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM><b>Generated by<br><a href=\"" + JDiff.jDiffLocation + "\" class=\"staysblack\" target=\"_top\">JDiff</a></b></EM></TD>");
                diffFile.println("</TR>");
                
                // Links for frames and no frames
                diffFile.println("<TR>");
                diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
                diffFile.println("</TD>");
                diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
                diffFile.println("  <A HREF=\"" + "../" + HTMLReportGenerator.reportFileName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;");
                diffFile.println("  &nbsp;<A HREF=\"" + diffFileName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>");
                diffFile.println("</TR>");
                
                diffFile.println("</TABLE>");
                diffFile.println("<HR>");
                diffFile.println("<!-- End of nav bar -->");
                

                diffFile.println("<h2>");
                diffFile.println("Documentation Differences");
                diffFile.println("</h2>");
                diffFile.println();
                diffFile.println("<blockquote>");
                diffFile.println("This file contains all the changes in documentation as colored differences.");
                diffFile.println(" Note that an HTML error in the new documentation may cause the display of other documentation changes to be presented incorrectly. For instance, failure to close a &ltcode&gt; tag will cause all subsequent paragraphs to be displayed differently.");
                diffFile.println("</blockquote>");
                diffFile.println("<hr>");
                diffFile.println();
                
            } catch(IOException e) {
                System.out.println("IO Error while attempting to create " + fullDiffFileName);
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }

        // Generate the differences. 
        generateDiffs(oldDoc, newDoc, id);

        return "Documentation <a href=\"" + diffFileName + 
            HTMLReportGenerator.reportFileExt + "#" + id + 
            "\">changed</a> from ";
    }
    
    /** 
     * Emit the HTML footer and close the diff file. 
     */
    public static void closeDiffFile() { 
        if (diffFile != null) {
            // Write the HTML footer
            diffFile.println();
            diffFile.println("</BODY>");
            diffFile.println("</HTML>");
            diffFile.close();
        }
    }

    /** 
     * Generate the differences. 
     */
    static void generateDiffs(String oldDoc, String newDoc, String id) {
        String[] oldDocWords = parseDoc(oldDoc);
        String[] newDocWords = parseDoc(newDoc);

        DiffMyers diff = new DiffMyers(oldDocWords, newDocWords);
        DiffMyers.change script = diff.diff_2(false);
        diffFile.println("<A NAME=\"" + id + "\"></A><b>" + id + "</b><br><br>");
        script = mergeDiffs(oldDocWords, newDocWords, script);
        // Generate the differences in blockquotes to cope with unterminated 
        // HTML tags
        diffFile.println("<blockquote>");
        writeDiffs(oldDocWords, newDocWords, script);
        diffFile.println("</blockquote>");
        diffFile.println("<hr width=\"50%\">");
    }

    /** 
     * Convert the string to an array of strings, but don't break HTML tags up.
     */
    static String[] parseDoc(String doc) {
        String delimiters = " .,;:?!(){}[]\"'~@#$%^&*+=_-|\\<>";
        StringTokenizer st = new StringTokenizer(doc, delimiters, true);
        List docList = new ArrayList();
        boolean inTag = false;
        String tag = null;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (!inTag) {
                if (tok.compareTo("<") == 0) {
                    inTag = true;
                    tag = tok;
                } else { 
                    docList.add(tok);
                }
            } else {
                // Add all tokens to the tag until the closing > is seen
                if (tok.compareTo(">") == 0) {
                    inTag = false;
                    tag += tok;
                    docList.add(tag);
                } else { 
                    tag += tok;
                }
            }
        }     
        String[] docWords = new String[docList.size()];
        docWords = (String[])docList.toArray(docWords);
        return docWords;
    }

    /** 
     * For improved readability, merge changes of the form 
     *  "delete 1, insert 1, space, delete 1, insert 1"
     * to 
     *  "delete 3, insert 3" (including the space).
     *
     * @param oldDocWords The original documentation as a String array
     * @param newDocWords The new documentation as a String array
     */
    static DiffMyers.change mergeDiffs(String[] oldDocWords, String[] newDocWords, 
                                       DiffMyers.change script) {
        if (script.link == null)
            return script; // Only one change
        DiffMyers.change hunk = script;
        DiffMyers.change lasthunk = null; // Set to the last potential hunk
        int startOld = 0;
        for (; hunk != null; hunk = hunk.link) {
            int deletes = hunk.deleted;
            int inserts = hunk.inserted;
            if (lasthunk == null) {
                if (deletes == 1 && inserts == 1) {
                    // This is the start of a potential merge
                    lasthunk = hunk;
                } 
                continue;
            } else {
                int first0 = hunk.line0; // Index of first deleted word
                int first1 = hunk.line1; // Index of first inserted word
                if (deletes == 1 && inserts == 1 && 
                    oldDocWords[first0 - 1].compareTo(" ") == 0 && 
                    newDocWords[first1 - 1].compareTo(" ") == 0 &&
                    first0 == lasthunk.line0 + lasthunk.deleted + 1 &&
                    first1 == lasthunk.line1 + lasthunk.inserted + 1) {
                    // Merge this change into the last change
                    lasthunk.deleted += 2;
                    lasthunk.inserted += 2;
                    lasthunk.link = hunk.link;
                } else {
                    lasthunk = null;
                }
            }
        }            
        return script;
    }

    /** 
     * Write the differences to the diff file. The old documentation is 
     * edited using the edit script provided by the DiffMyers object.
     * Do not display diffs in HTML tags.
     *
     * @param oldDocWords The original documentation as a String array
     * @param newDocWords The new documentation as a String array
     */
    static void writeDiffs(String[] oldDocWords, String[] newDocWords, 
                           DiffMyers.change script) {
        DiffMyers.change hunk = script;
        int startOld = 0;
        if (trace) {
            System.out.println("Old Text:");
            for (int i = 0; i < oldDocWords.length; i++) {
                System.out.print(oldDocWords[i]);
            }
            System.out.println(":END");
            System.out.println("New Text:");
            for (int i = 0; i < newDocWords.length; i++) {
                System.out.print(newDocWords[i]);
            }
            System.out.println(":END");
        }

        for (; hunk != null; hunk = hunk.link) {
            int deletes = hunk.deleted;
            int inserts = hunk.inserted;
            if (deletes == 0 && inserts == 0) {
                continue; // Not clear how this would occur, but handle it
            }

            // Determine the range of word and delimiter numbers involved 
            // in each file.
            int first0 = hunk.line0; // Index of first deleted word
            // Index of last deleted word, invalid if deletes == 0
            int last0 = hunk.line0 + hunk.deleted - 1; 
            int first1 = hunk.line1; // Index of first inserted word
            // Index of last inserted word, invalid if inserts == 0
            int last1 = hunk.line1 + hunk.inserted - 1;
            
            if (trace) {
                System.out.println("HUNK: ");
                System.out.println("inserts: " + inserts);
                System.out.println("deletes: " + deletes);
                System.out.println("first0: " + first0);
                System.out.println("last0: " + last0);
                System.out.println("first1: " + first1);
                System.out.println("last1: " + last1);
            }

            // Emit the original document up to this change
            for (int i = startOld; i < first0; i++) {
                diffFile.print(oldDocWords[i]);
            }
            // Record where to start the next hunk from
            startOld = last0 + 1;
            // Emit the deleted words, but struck through
            // but do not emit deleted HTML tags
            if (deletes != 0) {
                boolean inStrike = false;
                for (int i = first0; i <= last0; i++) {
                    if (!oldDocWords[i].startsWith("<") && 
                        !oldDocWords[i].endsWith(">")) {
                        if (!inStrike) {
                            diffFile.print("<strike>");
                            inStrike = true;
                        }
                        diffFile.print(oldDocWords[i]);
                    }
                }
                if (inStrike) {
                    diffFile.print("</strike>");
                }
            }
            // Emit the inserted words in red, but do not emphasis new HTML tags
            if (inserts != 0) {
                boolean inEmph = false;
                for (int i = first1; i <= last1; i++) {
                    if (!newDocWords[i].startsWith("<") && 
                        !newDocWords[i].endsWith(">")) {
                        if (!inEmph) {
                            diffFile.print("<font color=\"red\">");
                            inEmph = true;
                        }
                    }
                    diffFile.print(newDocWords[i]);
                }
                if (inEmph) {
                    diffFile.print("</font>");
                }
            }
        } //for (; hunk != null; hunk = hunk.link)
        // Print out the remaining part of the old text
        for (int i = startOld; i < oldDocWords.length; i++) {
            diffFile.print(oldDocWords[i]);
        }
    }

    /** 
     * Current file where documentation differences are written as colored
     * differences.
     */
    public static PrintWriter diffFile = null;

    /** 
     * Base name of the current file where documentation differences are 
     * written as colored differences.
     */
    public static String diffFileName = "jdiff_docdiffs";

    /** 
     * If set, then do not generate colored diffs for documentation. 
     * Default is false.
     */
    public static boolean noDocDiffs = false;

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;
        
}  