package jdiff;

import java.io.*;
import java.util.*;

/** 
 * Class to generate colored differences between two sections of HTML text.
 *
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com
 */
class Diff {

    /** 
     * Save the differences between the two strings in a DiffOutput object
     * for later use.
     * 
     * @param id A per-package unique identifier for each documentation 
     *           change.
     */ 
    static String saveDocDiffs(String pkgName, String oldDoc, String newDoc, 
                               String id, String title) {
        // Generate the string which will link to this set of diffs
        if (noDocDiffs)
            return "Documentation changed from ";
        if (oldDoc == null || newDoc == null) {
            return "Documentation changed from ";
        }

        // Generate the differences. 
        generateDiffs(pkgName, oldDoc, newDoc, id, title);

        return "Documentation <a href=\"" + diffFileName + pkgName +
            HTMLReportGenerator.reportFileExt + "#" + id + 
            "\">changed</a> from ";
    }
    
    /** 
     * Generate the differences. 
     */
    static void generateDiffs(String pkgName, String oldDoc, String newDoc, 
                              String id, String title) {
        String[] oldDocWords = parseDoc(oldDoc);
        String[] newDocWords = parseDoc(newDoc);

        DiffMyers diff = new DiffMyers(oldDocWords, newDocWords);
        DiffMyers.change script = diff.diff_2(false);
        script = mergeDiffs(oldDocWords, newDocWords, script);
        String text = "<A NAME=\"" + id + "\"></A><b>" + title + "</b><br><br>";
        // Generate the differences in blockquotes to cope with unterminated 
        // HTML tags
        text += "<blockquote>";
        text = addDiffs(oldDocWords, newDocWords, script, text);
        text += "</blockquote>";
        text += "<hr align=\"left\" width=\"50%\">";
        docDiffs.add(new DiffOutput(pkgName, id, title, text));
    }

    /** 
     * Convert the string to an array of strings, but don't break HTML tags up.
     */
    static String[] parseDoc(String doc) {
        String delimiters = " .,;:?!(){}[]\"'~@#$%^&*+=_-|\\<>/";
        StringTokenizer st = new StringTokenizer(doc, delimiters, true);
        List docList = new ArrayList();
        boolean inTag = false;
        String tag = null;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (!inTag) {
                if (tok.compareTo("<") == 0) {
                    tag = tok;
                    if (st.hasMoreTokens()) {
                        // See if this really is a tag
                        tok = st.nextToken();
                        char ch = tok.charAt(0);
                        if (Character.isLetter(ch) || ch == '/') {
                            inTag = true;
                            tag += tok;
                        }
                    }
                    if (!inTag)
                      docList.add(tag);
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
        if (inTag) {
            // An unterminated tag, or more likely, < used instead of &lt;
            // There are no nested tags such as <a <b>> in HTML
            docList.add(tag);
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
     * Add the differences to the text passed in. The old documentation is 
     * edited using the edit script provided by the DiffMyers object.
     * Do not display diffs in HTML tags.
     *
     * @param oldDocWords The original documentation as a String array
     * @param newDocWords The new documentation as a String array
     * @return The text for this documentation difference
     */
    static String addDiffs(String[] oldDocWords, String[] newDocWords, 
                           DiffMyers.change script, String text) {
        String res = text;
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
                res += oldDocWords[i];
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
                            if (deleteEffect == 0)
                                res += "<strike>";
                            else if (deleteEffect == 1)
                                res += "<span style=\"background: #FFCCCC\">";
                            inStrike = true;
                        }
                        res += oldDocWords[i];
                    }
                }
                if (inStrike) {
                    if (deleteEffect == 0)
                        res += "</strike>";
                    else if (deleteEffect == 1)
                        res += "</span>";
                }
            }
            // Emit the inserted words, but do not emphasis new HTML tags
            if (inserts != 0) {
                boolean inEmph = false;
                for (int i = first1; i <= last1; i++) {
                    if (!newDocWords[i].startsWith("<") && 
                        !newDocWords[i].endsWith(">")) {
                        if (!inEmph) {
                            if (insertEffect == 0)
                                res += "<font color=\"red\">";
                            else if (insertEffect == 1)
                                res += "<span style=\"background: #FFFF00\">";
                            inEmph = true;
                        }
                    }
                    res += newDocWords[i];
                }
                if (inEmph) {
                    if (insertEffect == 0)
                        res += "</font>";
                    else if (insertEffect == 1)
                        res += "</span>";
                }
            }
        } //for (; hunk != null; hunk = hunk.link)
        // Print out the remaining part of the old text
        for (int i = startOld; i < oldDocWords.length; i++) {
            res += oldDocWords[i];
        }
        return res;
    }

    /** 
     * Emit all the documentation differences into one file per package.
     */ 
    static void emitDocDiffs(String fullReportFileName) {
        Collections.sort(docDiffs);
        Iterator iter = docDiffs.iterator();
        while (iter.hasNext()) {
            DiffOutput diffOutput = (DiffOutput)(iter.next());
            if (currPkgName == null || 
                currPkgName.compareTo(diffOutput.pkgName_) != 0) {
                // Open a different file for each package, add the HTML header,
                // the navigation bar and some preamble.
                if (currPkgName != null)
                    closeDiffFile(); // Close the existing file
                currPkgName = diffOutput.pkgName_;

                // Create the directory if it doesn't exist
                File opdir = new File(fullReportFileName);
                if (!opdir.mkdir() && !opdir.exists()) {
                    System.out.println("Error: could not create the subdirectory '" + fullReportFileName + "'");
                System.exit(3);
                }
                String fullDiffFileName = fullReportFileName + 
                    JDiff.DIR_SEP + diffFileName + currPkgName +
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
                    diffFile.println(currPkgName + " Documentation Differences");
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
                    diffFile.println("  &nbsp;<A HREF=\"" + diffFileName + currPkgName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>");
                    diffFile.println("</TR>");
                    
                    diffFile.println("</TABLE>");
                    diffFile.println("<HR>");
                    diffFile.println("<!-- End of nav bar -->");
                    
                    diffFile.println("<h2>");
                    diffFile.println(currPkgName + " Documentation Differences ");
                    diffFile.println("</h2>");
                    diffFile.println();
                    diffFile.println("<blockquote>");
                    diffFile.println("This file contains all the changes in documentation in the package <code>" + currPkgName + "</code> as colored differences.");
                    if (deleteEffect == 0)
                        diffFile.println("Deletions are shown <strike>like this</strike>, and");
                    else if (deleteEffect == 1)
                        diffFile.println("Deletions are shown <span style=\"background: #FFCCCC\">like this</span>, and");
                    if (insertEffect == 0)
                        diffFile.println("additions are shown in red <font color=\"red\">like this</font>.");
                    else if (insertEffect == 1)
                        diffFile.println("additions are shown <span style=\"background: #FFFF00\">like this</span>.");
                    diffFile.println("</blockquote>");
                    
                    diffFile.println("<blockquote>");
                    diffFile.println("If no deletions or additions are shown in an entry, the HTML tags will be what has changed. The <i>new</i> HTML tags are shown in the differences.");
                    diffFile.println("</blockquote>");
                    
                    diffFile.println("<blockquote>");
                    diffFile.println(" Note that an HTML error in the new documentation may cause the display of other documentation changes to be presented incorrectly. For instance, failure to close a &ltcode&gt; tag will cause all subsequent paragraphs to be displayed differently.");
                    diffFile.println("</blockquote>");
                    diffFile.println("<hr>");
                    diffFile.println();
                    
                } catch(IOException e) {
                    System.out.println("IO Error while attempting to create " + fullDiffFileName);
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                }
            } // if (currPkgName == null || currPkgName.compareTo(diffOutput.pkgName_) != 0)
            // Now add the documentation difference
            diffFile.println(diffOutput.text_);
        } // while (iter.hasNext())
        if (currPkgName != null)
            closeDiffFile(); // Close the existing file
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
     * Current file where documentation differences are written as colored
     * differences.
     */
    public static PrintWriter diffFile = null;

    /** 
     * Base name of the current file where documentation differences are 
     * written as colored differences.
     */
    public static String diffFileName = "docdiffs_";

    /** 
     * The name of the current package, used to create diffFileName.
     */
    private static String currPkgName = null;

    /** 
     * If set, then do not generate colored diffs for documentation. 
     * Default is false.
     */
    public static boolean noDocDiffs = false;

    /** 
     * Define the type of emphasis for deleted words.
     * 0 strikes the words through.
     * 1 outlines the words in light grey.
     */
    public static int deleteEffect = 0;

    /** 
     * Define the type of emphasis for inserted words.
     * 0 colors the words red.
     * 1 outlines the words in yellow, like a highlighter.
     */
    public static int insertEffect = 1;

    /** The list of documentation differences. */
    private static List docDiffs = new ArrayList(); // DiffOutput[]
        
    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;
        
}  
