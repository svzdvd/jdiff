package jdiff;

import com.sun.javadoc.*;
//import com.sun.tools.doclets.standard.Standard;

import java.util.*;
//import java.text.SimpleDateFormat;
import java.io.*;

/**
 * Converts a Javadoc RootDoc object into a representation in an 
 * XML file.
 *
 * @author Matthew Doar, doar@pobox.com
 * @version 1.0
 */
public class RootDocToXML {

    /** Default constructor. */
    public RootDocToXML() {
    }

    /**
     * Write the XML representation of the API to a file.
     *
     * @param root  the RootDoc object passed by Javadoc
     * @return true if no problems encountered
     */
    public static boolean writeXML(RootDoc root) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFileName);
            outputFile = new PrintWriter(fos);
            if (root.specifiedPackages().length != 0) {
                RootDocToXML apiWriter = new RootDocToXML();
                apiWriter.emitXMLHeader();
                apiWriter.logOptions();
                apiWriter.processPackages(root);
                apiWriter.emitXMLFooter();
            }
            outputFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + outputFileName);
            System.out.println("Error: " +  e.getMessage());
            System.exit(1);
        }
        return true;
    }

    /**
     * Write the options which were used to generate this XML file
     * out as XML comments.
     */
    public void logOptions() {
        outputFile.print("<!-- ");
        outputFile.print(" Command line arguments = " + Options.cmdOptions);
        outputFile.println(" -->");
    }

    /**
     * Process each package and the classes/interfaces within it.
     *
     * @param pd  an array of PackageDoc objects
     */
    public void processPackages(RootDoc root) {
        PackageDoc[] pd = root.specifiedPackages();
        for (int i = 0; pd != null && i < pd.length; i++) {
            String pkgName = pd[i].name();
            
            if (trace) System.out.println("PROCESSING PACKAGE: " + pkgName);
            outputFile.println("<package name=\"" + pkgName + "\">");

            int tagCount = pd[i].tags().length;
            if (trace) System.out.println("#tags: " + tagCount);
            
            List classList = new LinkedList(Arrays.asList(pd[i].allClasses()));
            Collections.sort(classList);
            ClassDoc[] classes = new ClassDoc[classList.size()];
            classes = (ClassDoc[])classList.toArray(classes);
            processClasses(classes, pkgName);

            addPkgDocumentation(root, pd[i], 2);

            outputFile.println("</package>");
        }

        // Deal with classes which have no package
        ClassDoc[] cd = null;//TODO root.specifiedClasses();
        for (int i = 0; cd != null && i < cd.length; i++) {
            String pkgName = "NoPackage";
            
            outputFile.println("<package name=\"" + pkgName + "\">");

            List classList = new LinkedList(Arrays.asList(cd));
            Collections.sort(classList);
            ClassDoc[] classes = new ClassDoc[classList.size()];
            classes = (ClassDoc[])classList.toArray(classes);
            processClasses(classes, pkgName);

            outputFile.println("</package>");
        }
    } // processPackages
    
    /**
     * Process classes and interfaces.
     *
     * @param cd An array of ClassDoc objects.
     */
    public void processClasses(ClassDoc[] cd, String pkgName) {
        if (cd.length == 0)
            return;
        if (trace) System.out.println("PROCESSING CLASSES, number=" + cd.length);
        for (int i = 0; i < cd.length; i++) {
            String className = cd[i].name();
            if (trace) System.out.println("PROCESSING CLASS/IFC: " + className);
            // Only save the shown elements
            if (!shownElement(cd[i], classVisibilityLevel))
                continue;
            boolean isInterface = false;
            if (cd[i].isInterface())
                isInterface = true;
            if (isInterface) {
                outputFile.println("  <!-- start interface " + pkgName + "." + className + " -->");
                outputFile.print("  <interface name=\"" + className + "\"");
            } else {
                outputFile.println("  <!-- start class " + pkgName + "." + className + " -->");
                outputFile.print("  <class name=\"" + className + "\"");
            }
            // Add attributes to the class element
            ClassDoc parent = cd[i].superclass();
            if (parent != null)
                outputFile.println(" extends=\"" + parent.qualifiedName() + "\"");
            outputFile.println("    abstract=\"" + cd[i].isAbstract() + "\"");
            addCommonModifiers(cd[i], 4);
            outputFile.println(">");
            // Process class members. (Treat inner classes as members.)
            processInterfaces(cd[i].interfaces());
            processConstructors(cd[i].constructors());
            processMethods(cd[i], cd[i].methods());
            processFields(cd[i].fields());

            addDocumentation(cd[i], 4);

            if (isInterface) {
                outputFile.println("  </interface>");
                outputFile.println("  <!-- end interface " + pkgName + "." + className + " -->");
            } else {
                outputFile.println("  </class>");
                outputFile.println("  <!-- end class " + pkgName + "." + className + " -->");
            }
            // Inner classes have already been added.
            /*
              ClassDoc[] ic = cd[i].innerClasses();
              for (int k = 0; k < ic.length; k++) {
              System.out.println("Inner class " + k + ", name = " + ic[k].name());
              } 
            */
        }//for
    }//processClasses()
    
    /**
     * Add qualifiers for the program element as attributes.
     *
     * @param ped The given program element.
     */
    public void addCommonModifiers(ProgramElementDoc ped, int indent) {
        // Static and final and visibility on one line
        for (int i = 0; i < indent; i++) outputFile.print(" ");
        outputFile.print("static=\"" + ped.isStatic() + "\"");
        outputFile.print(" final=\"" + ped.isFinal() + "\"");
        // Visibility
        String visibility = null;
        if (ped.isPublic())
            visibility = "public";
        else if (ped.isProtected())
            visibility = "protected";
        else if (ped.isPackagePrivate())
            visibility = "package";
        else if (ped.isPrivate())
            visibility = "private";
        outputFile.println(" visibility=\"" + visibility + "\"");

        // Deprecation on its own line
        for (int i = 0; i < indent; i++) outputFile.print(" ");
        boolean isDeprecated = false;
        Tag[] ta = ((Doc)ped).tags("deprecated");
        if (ta.length != 0) {
            isDeprecated = true;
        }
        if (ta.length > 1) {
            System.out.println("JDiff: warning: multiple @deprecated tags found in comments for " + ped.name() + ". Using the first one only.");
            System.out.println("Text is: " + ((Doc)ped).getRawCommentText());
        }
        if (isDeprecated) {
            String text = ta[0].text(); // Use only one @deprecated tag
            if (text != null && text.compareTo("") != 0) {
                int idx = endOfFirstSentence(text);
                if (idx == 0) {
                    // No useful comment
                    outputFile.print("deprecated=\"deprecated, no comment\"");
                } else {
                    String fs = null;
                    if (idx == -1)
                        fs = text;
                    else
                        fs = text.substring(0, idx+1);
                    String st = API.hideHTMLTags(fs);
                    outputFile.print("deprecated=\"" + st + "\"");
                }
            } else {
                outputFile.print("deprecated=\"deprecated, no comment\"");
            }
        } else {
            outputFile.print("deprecated=\"not deprecated\"");
        }

    } //addQualifiers()

    /**
     * Process the interfaces implemented by the class.
     *
     * @param ifaces An array of ClassDoc objects
     */
    public void processInterfaces(ClassDoc[] ifaces) {
        if (trace) System.out.println("PROCESSING INTERFACES, number=" + ifaces.length);
        for (int i = 0; i < ifaces.length; i++) {
            String ifaceName = ifaces[i].qualifiedName();
            if (trace) System.out.println("PROCESSING INTERFACE: " + ifaceName);
            outputFile.println("    <implements name=\"" + ifaceName + "\">");
            outputFile.println("    </implements>");
        }//for
    }//processInterfaces()
    
    /**
     * Process the constructors in the class.
     *
     * @param ct An array of ConstructorDoc objects
     */
    public void processConstructors(ConstructorDoc[] ct) {
        if (trace) System.out.println("PROCESSING CONSTRUCTORS, number=" + ct.length);
        for (int i = 0; i < ct.length; i++) {
            String ctorName = ct[i].name();
            if (trace) System.out.println("PROCESSING CONSTRUCTOR: " + ctorName);
            // Only save the shown elements
            if (!shownElement(ct[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <constructor name=\"" + ctorName + "\"");

            Parameter[] params = ct[i].parameters();
            boolean first = true;
            if (params.length != 0) {
                outputFile.print(" type=\"");
                for (int j = 0; j < params.length; j++) {
                    if (!first)
                        outputFile.print(", ");
                    emitType(params[j].type());
                    first = false;
                }
                outputFile.println("\"");
            } else
                outputFile.println();
            addCommonModifiers(ct[i], 6);
            outputFile.println(">");
            
            // Generate the exception elements if any exceptions are thrown
            processExceptions(ct[i].thrownExceptions());

            addDocumentation(ct[i], 6);

            outputFile.println("    </constructor>");
        }//for
    }//processConstructors()
    
    /**
     * Process all exceptions thrown by a constructor or method.
     *
     * @param cd An array of ClassDoc objects
     */
    public void processExceptions(ClassDoc[] cd) {
        if (trace) System.out.println("PROCESSING EXCEPTIONS, number=" + cd.length);
        for (int i = 0; i < cd.length; i++) {
            String exceptionName = cd[i].name();
            if (trace) System.out.println("PROCESSING EXCEPTION: " + exceptionName);
            outputFile.println("      <exception name=\"" + exceptionName + "\"/>");
        }//for
    }//processExceptions()
    
    /**
     * Process the methods in the class.
     *
     * @param md An array of MethodDoc objects
     */
    public void processMethods(ClassDoc cd, MethodDoc[] md) {
        if (trace) System.out.println("PROCESSING " +cd.name()+" METHODS, number = " + md.length);
        for (int i = 0; i < md.length; i++) {
            String methodName = md[i].name();
            if (trace) System.out.println("PROCESSING METHOD: " + methodName);
            // Skip <init> and <clinit>
            if (methodName.startsWith("<"))
                continue;
            // Only save the shown elements
            if (!shownElement(md[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <method name=\"" + methodName + "\"");
            Type retType = md[i].returnType();
            if (retType.qualifiedTypeName().compareTo("void") == 0) {
                // Don't add a return attribute if the return type is void
                outputFile.println();
            } else {
                outputFile.print(" return=\"");
                emitType(retType);
                outputFile.println("\"");
            }
            outputFile.print("      abstract=\"" + md[i].isAbstract() + "\"");
            outputFile.print(" native=\"" + md[i].isNative() + "\"");
            outputFile.println(" synchronized=\"" + md[i].isSynchronized() + "\"");
            addCommonModifiers(md[i], 6);
            outputFile.println(">");
            // Generate the parameter elements, if any
            Parameter[] params = md[i].parameters();
            for (int j = 0; j < params.length; j++) {
                outputFile.print("      <param name=\"" + params[j].name() + "\"");
                outputFile.print(" type=\"");
                emitType(params[j].type());
                outputFile.println("\"/>");
            }

            // Generate the exception elements if any exceptions are thrown
            processExceptions(md[i].thrownExceptions());

            addDocumentation(md[i], 6);

            outputFile.println("    </method>");
        }//for
    }//processMethods()

    /**
     * Process the fields in the class.
     *
     * @param fd An array of FieldDoc objects
     */
    public void processFields(FieldDoc[] fd) {
        if (trace) System.out.println("PROCESSING FIELDS, number=" + fd.length);
        for (int i = 0; i < fd.length; i++) {
            String fieldName = fd[i].name();
            if (trace) System.out.println("PROCESSING FIELD: " + fieldName);
            // Only save the shown elements
            if (!shownElement(fd[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <field name=\"" + fieldName + "\"");
            outputFile.print(" type=\"");
            emitType(fd[i].type());
            outputFile.println("\"");
            outputFile.print("      transient=\"" + fd[i].isTransient() + "\"");
            outputFile.println(" volatile=\"" + fd[i].isVolatile() + "\"");
            addCommonModifiers(fd[i], 6);
            outputFile.println(">");

            addDocumentation(fd[i], 6);

            outputFile.println("    </field>");

        }//for
    }//processFields()
    
    /**
     * Emit the type name. Removed any prefixed warnings about ambiguity.
     * The type maybe an array.
     *
     * @param type A Type object.
     */
    public void emitType(Type type) {
        String name = type.qualifiedTypeName();
        if (name.startsWith("<<ambiguous>>"))
            name = name.substring(13);
        outputFile.print(name + type.dimension());
    }

    /**
     * Emit the XML header.
     */
    public void emitXMLHeader() {
        outputFile.println("<?xml version=\"1.0\"?>");
        outputFile.println("<!-- Generated by the JDiff JavaDoc doclet -->");
        outputFile.println("<!-- (" + JDiff.jDiffLocation + ") -->");
        outputFile.println("<!-- on " + new Date() + " -->");
        outputFile.println();
        outputFile.println("<!-- XML Schema is used, but XHTML transitional DTD is needed for nbsp -->");
        outputFile.println("<!-- entity definitions etc.-->");
        outputFile.println("<!DOCTYPE api");
        outputFile.println("     PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        outputFile.println("     \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        outputFile.println("<api");
        outputFile.println("  xmlns:xsi='http://www.w3.org/2000/10/XMLSchema-instance'");
        outputFile.println("  xsi:noNamespaceSchemaLocation='api.xsd'");
        outputFile.println("  name=\"" + apiIdentifier + "\"");
        outputFile.println("  jdversion=\"" + JDiff.version + "\">");
        outputFile.println();
    }

    /**
     * Emit the XML footer.
     */
    public void emitXMLFooter() {
        outputFile.println();
        outputFile.println("</api>");
    }

    /** 
     * Determine if the program element is shown, according to the given 
     * level of visibility. 
     *
     * @param ped The given program element.
     * @param visLevel The desired visibility level; "public", "protected",
     *   "package" or "private".
     * @return boolean Set if this element is shown.
     */
    public boolean shownElement(ProgramElementDoc ped, String visLevel) {
        // If a doc block contains @exclude or a similar such tag, 
        // then don't display it.
        if (doExclude && excludeTag != null) {
            String rct = ((Doc)ped).getRawCommentText();
            int idx = rct.indexOf(excludeTag);
            if (idx != -1)
                return false;
        }
        if (visLevel.compareTo("private") == 0)
            return true;
        // Show all that is not private 
        if (visLevel.compareTo("package") == 0)
            return !ped.isPrivate();
        // Show all that is not private or package
        if (visLevel.compareTo("protected") == 0)
            return !(ped.isPrivate() || ped.isPackagePrivate());
        // Show all that is not private or package or protected,
        // i.e. all that is public
        if (visLevel.compareTo("public") == 0)
            return ped.isPublic();
        return false;
    } //shownElement()
    
    /** 
     * Strip out non-printing characters, replacing them with a character 
     * which will not change where the end of the first sentence is found.
     * This character is the hash mark, '&#035;'.
     */
    public String stripNonPrintingChars(String s, Doc doc) {
        if (!stripNonPrintables)
            return s;
        char[] sa = s.toCharArray();
        for (int i = 0; i < sa.length; i++) {
            char c = sa[i];
            // TODO still have an issue with Unicode: 0xfc in java.lang.String.toUpperCase comments "Fahrvergn" && c != '�' && c != ''
//            if (Character.isDefined(c))
            if (Character.isLetterOrDigit(c))
                continue;
            // There must be a better way that is still platform independent!
            if (c == ' ' ||
                c == '.' ||
                c == ',' ||
                c == '\r' ||
                c == '\t' ||
                c == '\n' ||
                c == '!' ||
                c == '?' ||
                c == ';' ||
                c == ':' ||
                c == '[' ||
                c == ']' ||
                c == '(' ||
                c == ')' ||
                c == '~' ||
                c == '@' ||
                c == '#' ||
                c == '$' ||
                c == '%' ||
                c == '^' ||
                c == '&' ||
                c == '*' ||
                c == '-' ||
                c == '=' ||
                c == '+' ||
                c == '_' ||
                c == '|' ||
                c == '\\' ||
                c == '/' ||
                c == '\'' ||
                c == '}' ||
                c == '{' ||
                c == '"' ||
                c == '<' ||
                c == '>' ||
                c == '`'
                )
                continue;
/* Doesn't seem to return the expected values?
            int val = Character.getNumericValue(c);
//            if (s.indexOf("which is also a test for non-printable") != -1)
//                System.out.println("** Char " + i + "[" + c + "], val =" + val); //DEBUG
            // Ranges from http://www.unicode.org/unicode/reports/tr20/
            // Should really replace 0x2028 and  0x2029 with <br/>
            if (val == 0x0 ||
                inRange(val, 0x2028, 0x2029) || 
                inRange(val, 0x202A, 0x202E) || 
                inRange(val, 0x206A, 0x206F) || 
                inRange(val, 0xFFF9, 0xFFFC) || 
                inRange(val, 0xE0000, 0xE007F)) {
                if (trace) {
                    System.out.println("Warning: changed non-printing character  " + sa[i] + " in " + doc.name()); 
                }
                sa[i] = '#';
            }
*/
            // Replace the non-printable character with a printable character
            // which does not change the end of the first sentence
            sa[i] = '#';
        }
        return new String(sa);
    }

    /** Return true if val is in the range [min|max], inclusive. */
    public boolean inRange(int val, int min, int max) {
        if (val < min)
            return false;
        if (val > max)
            return false;
        return true;
    }

    /** 
     * Add at least the first sentence from a doc block to the API. This is
     * used by the report generator if no comment is provided.
     * Need to make sure that HTML tags are not confused with XML tags.
     * This could be done by stuffing the &lt; character to another string
     * or by handling HTML in the parser. This second option seems neater. Note that
     * XML expects all element tags to have either a closing "/>" or a matching
     * end element tag. Due to the difficulties of converting incorrect HTML
     * to XHTML, the first option is used.
     */
    public void addDocumentation(ProgramElementDoc ped, int indent) {
        String rct = ((Doc)ped).getRawCommentText();
        if (rct != null) {
            rct = stripNonPrintingChars(rct, (Doc)ped);
            rct = rct.trim();
            if (rct.compareTo("") != 0 && 
                rct.indexOf(Comments.placeHolderText) == -1 &&
                rct.indexOf("InsertOtherCommentsHere") == -1) {
                int idx = endOfFirstSentence(rct);
                if (idx == 0)
                    return;
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("<doc>");
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                String firstSentence = null;
                if (idx == -1)
                    firstSentence = rct;
                else
                    firstSentence = rct.substring(0, idx+1);
                boolean checkForAts = false;
                if (checkForAts && firstSentence.indexOf("@") != -1 && 
                    firstSentence.indexOf("@link") == -1) {
                    System.out.println("Warning: @ tag seen in comment: " + 
                                       firstSentence);
                }
                String firstSentenceNoTags = API.stuffHTMLTags(firstSentence);
                outputFile.println(firstSentenceNoTags);
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("</doc>");
            }
        }
    }

    /** 
     * Add at least the first sentence from a doc block for a package to the API. This is
     * used by the report generator if no comment is provided.
     * The default source tree may not include the package.html files, so
     * this may be unavailable in many cases.
     * Need to make sure that HTML tags are not confused with XML tags.
     * This could be done by stuffing the &lt; character to another string
     * or by handling HTML in the parser. This second option is neater. Note that
     * XML expects all element tags to have either a closing "/>" or a matching
     * end element tag.  Due to the difficulties of converting incorrect HTML
     * to XHTML, the first option is used.
     */
    public void addPkgDocumentation(RootDoc root, PackageDoc pd, int indent) {
        String rct = null;
        String filename = pd.name();
        try {
            // See if the source path was specified as part of the
            // options and prepend it if it was.
            String srcLocation = null;
            String[][] options = root.options();
            for (int opt = 0; opt < options.length; opt++) {
                if ((options[opt][0]).compareTo("-sourcepath") == 0) {
                    srcLocation = options[opt][1];
                    break;
                }
            }
            filename = filename.replace('.', JDiff.DIR_SEP.charAt(0));
            if (srcLocation != null) {
                // Make a relative location absolute 
                if (srcLocation.startsWith("..")) {
                    String curDir = System.getProperty("user.dir");
                    while (srcLocation.startsWith("..")) {
                        srcLocation = srcLocation.substring(3);
                        int idx = curDir.lastIndexOf(JDiff.DIR_SEP);
                        curDir = curDir.substring(0, idx+1);
                    }
                    srcLocation = curDir + srcLocation;
                }
                filename = srcLocation + JDiff.DIR_SEP + filename;
            }
            // Try both ".htm" and ".html"
            filename += JDiff.DIR_SEP + "package.htm";
            File f2 = new File(filename);
            if (!f2.exists()) {
                filename += "l";
            }
            FileInputStream f = new FileInputStream(filename);
            BufferedReader d = new BufferedReader(new InputStreamReader(f));
            String str = d.readLine();
            while(str != null) {
                if (str.startsWith("<body") || 
                    str.startsWith("<BODY") || 
                    str.startsWith("</body>") || 
                    str.startsWith("</BODY>") ) {
                    str = d.readLine();
                    continue; // Ignore these lines
                }
                if (rct == null)
                    rct = str + "\n";
                else
                    rct += str + "\n";
                str = d.readLine();
            }
        }  catch(java.io.FileNotFoundException e) {
            // If it doesn't exist, that's fine
            if (trace)
                System.out.println("No package level documentation file at '" + filename + "'");
        } catch(java.io.IOException e) {
            System.out.println("Error reading file \"" + filename + "\": " + e.getMessage());
            System.exit(5);
        }     
        if (rct != null) {
            rct = stripNonPrintingChars(rct, (Doc)pd);
            rct = rct.trim();
            if (rct.compareTo("") != 0 &&
                rct.indexOf(Comments.placeHolderText) == -1 &&
                rct.indexOf("InsertOtherCommentsHere") == -1) {
                int idx = endOfFirstSentence(rct);
                if (idx == 0)
                    return;
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("<doc>");
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                String firstSentence = null;
                if (idx == -1)
                    firstSentence = rct;
                else
                    firstSentence = rct.substring(0, idx+1);
                String firstSentenceNoTags = API.stuffHTMLTags(firstSentence);
                outputFile.println(firstSentenceNoTags);
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("</doc>");
            }
        }
    }

    /** 
     * Find the index of the end of the first sentence in the given text,
     * when writing out to an XML file.
     * This is an extended version of the algorithm used by the DocCheck 
     * JavaDoc doclet. It checks for @tags too.
     *
     * @param text The text to be searched.
     * @return The index of the end of the first sentence. If there is no
     *         end, return -1. If there is no useful text, return 0.
     *         If the whole doc block comment is wanted (default), return -1.
     */
    public static int endOfFirstSentence(String text) {
        return endOfFirstSentence(text, true);
    }

    /** 
     * Find the index of the end of the first sentence in the given text.
     * This is an extended version of the algorithm used by the DocCheck 
     * JavaDoc doclet. It checks for @tags too.
     *
     * @param text The text to be searched.
     * @param writingToXML Set to true when writing out XML.
     * @return The index of the end of the first sentence. If there is no
     *         end, return -1. If there is no useful text, return 0.
     *         If the whole doc block comment is wanted (default), return -1.
     */
    public static int endOfFirstSentence(String text, boolean writingToXML) {
        if (saveAllDocs && writingToXML)
            return -1;
        int index = -1;  // Use the brute force approach.
        index = minIndex(index, text.indexOf("? " ));
        index = minIndex(index, text.indexOf("?\t"));
        index = minIndex(index, text.indexOf("?\n"));
        index = minIndex(index, text.indexOf("?\r"));
        index = minIndex(index, text.indexOf("?\f"));
        index = minIndex(index, text.indexOf("! " ));
        index = minIndex(index, text.indexOf("!\t"));
        index = minIndex(index, text.indexOf("!\n"));
        index = minIndex(index, text.indexOf("!\r"));
        index = minIndex(index, text.indexOf("!\f"));
        index = minIndex(index, text.indexOf(". " ));
        index = minIndex(index, text.indexOf(".\t"));
        index = minIndex(index, text.indexOf(".\n"));
        index = minIndex(index, text.indexOf(".\r"));
        index = minIndex(index, text.indexOf(".\f"));
        index = minIndex(index, text.indexOf("@param"));
        index = minIndex(index, text.indexOf("@return"));
        index = minIndex(index, text.indexOf("@throw"));
        index = minIndex(index, text.indexOf("@serial"));
        index = minIndex(index, text.indexOf("@exception"));
        index = minIndex(index, text.indexOf("@deprecate"));
        index = minIndex(index, text.indexOf("@author"));
        index = minIndex(index, text.indexOf("@since"));
        index = minIndex(index, text.indexOf("@see"));
        index = minIndex(index, text.indexOf("@version"));
        if (doExclude && excludeTag != null)
            index = minIndex(index, text.indexOf(excludeTag));
        index = minIndex(index, text.indexOf("@vtexclude"));
        index = minIndex(index, text.indexOf("@vtinclude"));
        index = minIndex(index, text.indexOf("<p>", 2)); // Not at start
        index = minIndex(index, text.indexOf("<P>", 2)); // Not at start
        index = minIndex(index, text.indexOf("<blockquote", 2));  // Not at start
        index = minIndex(index, text.indexOf("<pre")); // May contain anything!
        // Avoid the char at the start of a tag in some cases
        if (index != -1 &&  
            (text.charAt(index) == '@' || text.charAt(index) == '<')) {
            if (index != 0)
                index--;
        }
        
/* Not used for jdiff, since tags are explicitly checked for above.
        // Look for a sentence terminated by an HTML tag.
        index = minIndex(index, text.indexOf(".<"));
        if (index == -1) {
            // If period-whitespace etc was not found, check to see if
            // last character is a period,
            int endIndex = text.length()-1;
            if (text.charAt(endIndex) == '.' ||
                text.charAt(endIndex) == '?' ||
                text.charAt(endIndex) == '!') 
                index = endIndex;
        }
*/
        return index;
    }
    
    /**
     * Return the minimum of two indexes if > -1, and return -1
     * only if both indexes = -1.
     * @param i an int index
     * @param j an int index
     * @return an int equal to the minimum index > -1, or -1
     */
    public static int minIndex(int i, int j) {
        if (i == -1) return j;
        if (j == -1) return i;
        return Math.min(i,j);
    }
    
    /** 
     * The name of the file where the XML representing the API will be 
     * stored. 
     */
    public static String outputFileName = null;

    /** 
     * The identifier of the API being written out in XML, e.g. 
     * &quotSuperProduct 1.3&quot;. 
     */
    public static String apiIdentifier = null;

    /** 
     * The file where the XML representing the API will be stored. 
     */
    private static PrintWriter outputFile = null;
    
    /** 
     * Do not display a class  with a lower level of visibility than this. 
     * Default is to display all public and protected classes.
     */
    public static String classVisibilityLevel = "protected";

    /** 
     * Do not display a member with a lower level of visibility than this. 
     * Default is to display all public and protected members 
     * (constructors, methods, fields).
     */
    public static String memberVisibilityLevel = "protected";

    /** 
     * If set, then save the entire contents of a doc block comment in the 
     * API file. If not set, then just save the first sentence. Default is 
     * that this is not set, since some documentation may have characters 
     * which the XML parser does not treat as valid XML.
     */
    public static boolean saveAllDocs = false;

    /** 
     * If set, exclude program elements marked with whatever the exclude tag
     * is specified as, e.g. "@exclude".
     */
    public static boolean doExclude = false;

    /** 
     * Exclude program elements marked with this String, e.g. "@exclude".
     */
    public static String excludeTag = null;

    /** 
     * If set, then strip out non-printing characters from documentation.
     * Default is that this is set.
     */
    static boolean stripNonPrintables = true;

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;

} //RootDocToXML