package jdiff;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.taskdefs.Javadoc.DocletInfo;
import org.apache.tools.ant.taskdefs.Javadoc.DocletParam;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;

/**
 * An Ant task to produce a simple JDiff report. More complex reports still
 * need parameters that are controlled by the Ant Javadoc task.
 */
public class JDiffAntTask {

    public void execute() throws BuildException {
	//String message = project.getProperty("basedir");
        //project.log("Starting from " + message, Project.MSG_INFO);

	jdiffHome = project.getProperty("JDIFF_HOME");
	if (jdiffHome == null || jdiffHome.compareTo("") == 0 | 
	    jdiffHome.compareTo("(not set)") == 0) {
	    throw new BuildException("Error: invalid JDIFF_HOME property. Set it in the build file to the directory where jdiff is installed");
	}

	jdiffClassPath = jdiffHome + DIR_SEP + "lib" + DIR_SEP + "jdiff.jar" +
	    System.getProperty("path.separator") +
	    jdiffHome + DIR_SEP + "lib" + DIR_SEP + "xerces.jar";

	// TODO detect and set verboseAnt

	// Create, if necessary, the directory for the JDiff HTML report
        if (!destdir.mkdir() && !destdir.exists()) {
	    throw new BuildException(getDestdir() + " is not a valid directory");
	} else {
	    project.log(" Report location: " + getDestdir() + DIR_SEP 
			+ "changes.html", Project.MSG_INFO);
	}
	// TODO could also record the parameters used for JDiff here?
 	
	// Check that there are indeed two projects to compare. If there
	// are no directories in the project, let Javadoc do the complaining
	if (oldProject == null || newProject == null) {
	    throw new BuildException("Error: two projects are needed, one <old> and one <new>");
	}

	// Display the directories being compared, and some name information
	if (getVerbose()) {
	    project.log("Older version: " + oldProject.getName(), 
			Project.MSG_INFO);
	    project.log("Included directories for older version:", 
			Project.MSG_INFO);
	    DirectoryScanner ds = 
		oldProject.getDirset().getDirectoryScanner(project);
	    String[] files = ds.getIncludedDirectories();
	    for (int i = 0; i < files.length; i++) {
		project.log(" " + files[i], Project.MSG_INFO);
	    }
	    ds = null;
	    
	    project.log("Newer version: " + newProject.getName(), 
			Project.MSG_INFO);
	    project.log("Included directories for newer version:", 
			Project.MSG_INFO);
	    ds = newProject.getDirset().getDirectoryScanner(project);
	    files = ds.getIncludedDirectories();
	    for (int i = 0; i < files.length; i++) {
		project.log(" " + files[i], Project.MSG_INFO);
	    }
	}

	// Call Javadoc twice to generate Javadoc for each project
	generateJavadoc(oldProject.getJavadoc(), oldProject.getDirset(), 
			oldProject.getName());
	generateJavadoc(newProject.getJavadoc(), newProject.getDirset(), 
			newProject.getName());

	// Call Javadoc three times for JDiff.
	generateXML(oldProject.getDirset(), oldProject.getName());
	generateXML(newProject.getDirset(), newProject.getName());
	compareXML(oldProject.getName(), newProject.getName());

	// Repeat some useful information
	project.log(" Report location: " + getDestdir() + DIR_SEP 
		    + "changes.html", Project.MSG_INFO);
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to generate the XML representation of a project's source files.
     *
     * @param dirset The set of directories to be treated as pacakge names
     * @param apiname The name of the project, also use for the XML file
     */
    public void generateXML(DirSet dirSet, String apiname) {
	// Create a fresh new Javadoc task
	Javadoc jd = new Javadoc();
	jd.setProject(project); // Vital, otherwise Ant crashes
	jd.setTaskName("Analyzing " + apiname);
	jd.init();

	// First set up some parameters for the Javadoc task
	if (verboseAnt) {
	    jd.setVerbose(true);
	}
	jd.setDestdir(getDestdir());
	// TODO Add each of the root directories of the dirsets as source paths
	jd.setSourcepath(new Path(project, dirSet.getDir(project).toString()));

	// Tell Javadoc which packages we want to scan. JDiff works with 
	// packagenames, not sourcefiles.
	DirectoryScanner dirScanner = dirSet.getDirectoryScanner(project);
	String[] files = dirScanner.getIncludedDirectories();
	String packageList = ""; 
	// TODO nasty slow hack to create comma separated list
	// TODO also need to remove common ones: com com/foo etc
	for (int i = 0; i < files.length; i++) {
	    if (i != 0){
		packageList = packageList + ",";
	    }
	    packageList = packageList + files[i];
	}
	jd.setPackagenames(packageList);
	
	// Create the DocletInfo first so we have a way to use it to add params
	DocletInfo dInfo = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	jd.setDocletPath(new Path(project, jdiffClassPath));
	
	// Now set up some parameters for the JDiff doclet.
	DocletParam dp1 = dInfo.createParam();
	dp1.setName("-apiname");
	dp1.setValue(apiname);
	DocletParam dp2 = dInfo.createParam();
	dp2.setName("-baseURI");
	dp2.setValue("http://www.w3.org");
	// Put the generated file in the same directory as the report
	DocletParam dp3 = dInfo.createParam();
	dp3.setName("-apidir");
	dp3.setValue(getDestdir().toString());
	
	// Execute the Javadoc command to generate the XML file.
	jd.perform();
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to compare the XML representations of two instances of a project's 
     * source files, and generate an HTML report summarizing the differences.
     *
     * @param oldapiname The name of the older version of the project
     * @param newapiname The name of the newer version of the project
     */
    public void compareXML(String oldapiname, String newapiname) {
	// Create a fresh new Javadoc task
	Javadoc jd = new Javadoc();
	jd.setProject(project); // Vital, otherwise Ant crashes
	jd.setTaskName("Comparing");
	jd.init();
	
	// First set up some parameters for the Javadoc task
	if (verboseAnt) {
	    jd.setVerbose(true);
	}
	jd.setPrivate(true);
	jd.setDestdir(getDestdir());

	// Tell Javadoc which files we want to scan - a dummy file in this case
	jd.setSourcefiles(jdiffHome + DIR_SEP + "lib" + DIR_SEP + "Null.java");
	
	// Create the DocletInfo first so we have a way to use it to add params
	DocletInfo dInfo = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	jd.setDocletPath(new Path(project, jdiffClassPath));
	
	// Now set up some parameters for the JDiff doclet.
	DocletParam dp1 = dInfo.createParam();
	dp1.setName("-oldapi");
	dp1.setValue(oldapiname);
	DocletParam dp2 = dInfo.createParam();
	dp2.setName("-newapi");
	dp2.setValue(newapiname);
	// Get the generated XML files from the same directory as the report
	DocletParam dp3 = dInfo.createParam();
	dp3.setName("-oldapidir");
	dp3.setValue(getDestdir().toString());
	DocletParam dp4 = dInfo.createParam();
	dp4.setName("-newapidir");
	dp4.setValue(getDestdir().toString());

	// Assume that Javadoc reports already exist in ../"apiname"
	DocletParam dp5 = dInfo.createParam();
	dp5.setName("-javadocold");
	dp5.setValue(".." + DIR_SEP + oldapiname + DIR_SEP);
	DocletParam dp6 = dInfo.createParam();
	dp6.setName("-javadocnew");
	dp6.setValue(".." + DIR_SEP + newapiname + DIR_SEP);
	
	if (getStats()) {
	    // There are no arguments to this argument
	    dInfo.createParam().setName("-stats");
	    // TODO also have to copy image files for the stats pages
	}

	if (getDocchanges()) {
	    // There are no arguments to this argument
	    dInfo.createParam().setName("-docchanges");
	}

	// Execute the Javadoc command to compare the two XML files
	jd.perform();
    }

    /**
     * Generate the Javadoc for the project. If you want to generate
     * the Javadoc report for the project with different parameters from the
     * simple ones used here, then use the Javadoc Ant task directly, and 
     * set the javadoc attribute to the "old" or "new" element.
     *
     * @param javadoc The location of an existing Javadoc report, if any.
     * @param dirset The set of directories to be treated as pacakge names
     * @param apiname The name of the project, also use for the XML file
     */
    public void generateJavadoc(String javadoc, DirSet dirSet, String apiname) {	
	if (javadoc != null && javadoc.compareTo("generated") != 0) {
	    project.log("Configured to use existing Javadoc located in " +  
			javadoc, Project.MSG_INFO);
	    return;
	}

	// Create a fresh new Javadoc task
	Javadoc jd = new Javadoc();
	jd.setProject(project); // Vital, otherwise Ant crashes
	jd.setTaskName("Javadoc for " + apiname);
	jd.init();

	// Set up some parameters for the Javadoc task
	String dest = getDestdir().toString() + DIR_SEP + apiname;
	jd.setDestdir(new File(dest));
	jd.setPrivate(true);
	jd.setSourcepath(new Path(project, dirSet.getDir(project).toString()));

	// Tell Javadoc which packages we want to scan. JDiff works with 
	// packagenames, not sourcefiles.
	DirectoryScanner dirScanner = dirSet.getDirectoryScanner(project);
	String[] files = dirScanner.getIncludedDirectories();
	String packageList = ""; 
	// TODO nasty slow hack to create comma separated list
	// TODO also need to remove common ones: com com/foo etc
	for (int i = 0; i < files.length; i++) {
	    if (i != 0){
		packageList = packageList + ",";
	    }
	    packageList = packageList + files[i];
	}
	jd.setPackagenames(packageList);

	// Execute the Javadoc command to generate a regular Javadoc report
	jd.perform();
    }

    /**
     * The JDiff Ant task does not inherit from an Ant task, such as the 
     * Javadoc task, though this is usually how most Tasks are
     * written. This is because JDiff needs to run Javadoc three times
     * (twice for generating XML, once for generating HTML). The
     * Javadoc task has not easy way to reset its list of packages, so
     * we needed to be able to crate new Javadoc task objects.
     */
    private Project project;

    /**
     * Used as part of Ant's startup.
     */
    public void setProject(Project proj) {
        project = proj;
    }

    static String DIR_SEP = System.getProperty("file.separator");

    /**
     * JDIFF_HOME must be set as a property in the Ant build file.
     * It should be set to the root JDiff directory, ie. the one above 
     * wherever jdiff/lib/jdiff.jar is found.
     */
    private String jdiffHome = "(not set)";

    /**
     * The classpath used by Javadoc to find jdiff.jar and xerces.jar.
     */
    private String jdiffClassPath = "(not set)";

    /* ***************************************************************** */
    /* * Objects and methods which are related to attributes           * */
    /* ***************************************************************** */

    /** 
     * The destination directory for the generated report.
     * The default is "./jdiff_report".
     */
    private File destdir = new File("jdiff_report");

    /**
     * Used to store the destdir attribute of the JDiff task XML element.
     */
    public void setDestdir(File value) {
	this.destdir = value;
    }

    public File getDestdir() {
	return this.destdir;
    }

    /** 
     * Increases the JDiff Ant task logging verbosity if set with "yes", "on" 
     * or true". Default has to be false.
     * To increase verbosity of Javadoc, start Ant with -v or -verbose.
     */ 
    private boolean verbose = false;

    public void setVerbose(boolean value) {
	this.verbose = value;
    }

    public boolean getVerbose() {
	return this.verbose;
    }

    /** 
     * Set if ant was started with -v or -verbose 
     */
    private boolean verboseAnt = false;

    /** 
     * Add the -docchanges argument, to track changes in Javadoc documentation
     * as well as changes in classes etc.
     */ 
    private boolean docchanges = false;

    public void setDocchanges(boolean value) {
	this.docchanges = value;
    }

    public boolean getDocchanges() {
	return this.docchanges;
    }

    /** 
     * Add statistics to the report if set. Default can only be false.
     */ 
    private boolean stats = false;

    public void setStats(boolean value) {
	this.stats = value;
    }

    public boolean getStats() {
	return this.stats;
    }

    /* ***************************************************************** */
    /* * Classes and objects which are related to elements             * */
    /* ***************************************************************** */

    /**
     * A ProjectInfo-derived object for the older version of the project
     */
    private ProjectInfo oldProject = null;

    /**
     * Used to store the child element named "old", which is under the 
     * JDiff task XML element.
     */
    public void addConfiguredOld(ProjectInfo projInfo) {
	oldProject = projInfo;
    }

    /**
     * A ProjectInfo-derived object for the newer version of the project
     */
    private ProjectInfo newProject = null;

    /**
     * Used to store the child element named "new", which is under the 
     * JDiff task XML element.
     */
    public void addConfiguredNew(ProjectInfo projInfo) {
	newProject = projInfo;
    }

    /**
     * This class handles the information about a project, whether it is
     * the older or newer version.
     */
    public static class ProjectInfo {
	/** 
	 * The name of the project. This is used (without spaces) as the 
	 * base of the name of the file which contains the XML representing 
	 * the project.
	 */
	private String name;

	public void setName(String value) {
	    name = value;
	}

	public String getName() {
	    return name;
	}

	/** 
	 * The location of the Javadoc HTML for this project. Default value
	 * is "generate", which will cause the Javadoc to be generated in 
	 * a subdirectory named "name" in the task's destdir directory.
	 */
	private String javadoc;

	public void setJavadoc(String value) {
	    System.out.println("*** in here with "+ value);
	    javadoc = value;
	}

	public String getJavadoc() {
	    return javadoc;
	}

 	/** TODO multiple dirset calls TODO filesets for classes?
	 * These are the directories which contain the packages which make 
	 * up the project. Filesets are not supported by JDiff.
	 */
	private DirSet dirset = null;

	public void setDirset(DirSet value) {
	    dirset = value;
	}

	public DirSet getDirset() {
	    return dirset;
	}

	/** 
	 * Used to store the child element named "dirset", which is under the 
	 * "old" or "new" XML elements.
	 */
	public void addDirset(DirSet aDirset) {
	    setDirset(aDirset);
	}
	
    }
    /*
TODO link checked with linklint-2.3.5 -error -warn /@ and all is fine in the example except for the link in the Javadoc text for SuperProduct2.0/com/acme/sp/package.html. Looks like the old @link not generating the correct href when it in a package bug?
     */
}
