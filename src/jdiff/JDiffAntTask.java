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

    private Project project;

    public void setProject(Project proj) {
        project = proj;
    }

    public void execute() throws BuildException {
	// TODO check if debug is set
	//String message = project.getProperty("ant.project.name");
        //project.log("Starting '" + message, Project.MSG_INFO);

	// Check for and create the directory for the JDiff HTML report
        if (!destdir.mkdir() && !destdir.exists()) {
	    throw new BuildException(getDestdir() + " is not a valid directory");
	} else {
	    project.log("Output directory: " + getDestdir(), Project.MSG_INFO);
	}
 	
	// TODO check there are two projects to compare

	project.log("Old name: " + oldProject.getName(), Project.MSG_INFO);
	// Display the files being compared
	DirectoryScanner ds = oldProject.getDirset().getDirectoryScanner(project);
	String[] files = ds.getIncludedDirectories();
	for (int i = 0; i < files.length; i++) {
	    project.log(" " + files[i], Project.MSG_INFO);
	}
	ds = null;

	project.log("New name: " + newProject.getName(), Project.MSG_INFO);
	ds = newProject.getDirset().getDirectoryScanner(project);
	files = ds.getIncludedDirectories();
	for (int i = 0; i < files.length; i++) {
	    project.log(" " + files[i], Project.MSG_INFO);
	}

	// TODO generalise
	generateXML(oldProject.getDirset(), "SuperProduct 1.0", "/home/matt/jdiff/examples/SuperProduct1.0");
	generateXML(newProject.getDirset(), "SuperProduct 2.0", "/home/matt/jdiff/examples/SuperProduct2.0");
	compareXML("SuperProduct 1.0", "SuperProduct 2.0");

	// TODO copy some images files too
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to generate the XML representation of a project's source files.
     */
    // TODO remove root
    public void generateXML(DirSet fs, String apiname, String root) {
	// Create a Javadoc task which can be reset
	Javadoc jd = new Javadoc();
	jd.setProject(project);
	jd.setTaskName("JDiffAntTask");
	jd.init();

	// Tell Javadoc which files we want to scan. 
	// JDiff works with packagenames, not sourcefiles.
	jd.setSourcepath(new Path(project, root));
	DirectoryScanner ds = fs.getDirectoryScanner(project);
	String[] files = ds.getIncludedDirectories();
	String fss = ""; // TODO nasty slow hack to create comma separated list
	for (int i = 0; i < files.length; i++) {
	    if (i != 0){
		fss = fss + ",";
	    }
	    fss = fss + files[i];
	}
	jd.setPackagenames(fss);
	jd.setDestdir(getDestdir());
	
	// Creaste this first so we have a handle on the doclet
	DocletInfo d = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	// TODO generalise this
	jd.setDocletPath(new Path(project, "/home/matt/jdiff/lib/jdiff.jar:/home/matt/jdiff/lib/xerces.jar"));
	
	DocletParam dp1 = d.createParam();
	dp1.setName("-apiname");
	dp1.setValue(apiname);
	DocletParam dp2 = d.createParam();
	dp2.setName("-baseURI");
	dp2.setValue("http://www.w3.org");
	
	// Execute the Javadoc command to generate the XML representation of 
	// the old version.
	jd.perform();
    }

    /**
     * Convenient method to create a Javadoc task, configure it and run it
     * to compare the XML representations of two instances of a project's 
     * source files, and generate an HTML report summarizing the differences.
     */
    public void compareXML(String oldapiname, String newapiname) {
	// Create a Javadoc task which can be reset
	Javadoc jd = new Javadoc();
	jd.setProject(project);
	jd.setTaskName("JDiffAntTask");
	jd.init();
	
	// Tell Javadoc which files we want to scan - a dummy file in this case
	jd.setSourcefiles("/home/matt/jdiff/lib/Null.java");
	// Add some more options to Javadoc
	jd.setPrivate(true);
	jd.setDestdir(getDestdir());
	
	// Creaste this first so we have a handle on the doclet
	DocletInfo d = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	// TODO generalise this
	jd.setDocletPath(new Path(project, "/home/matt/jdiff/lib/jdiff.jar:/home/matt/jdiff/lib/xerces.jar"));
	
	DocletParam dp1 = d.createParam();
	dp1.setName("-oldapi");
	dp1.setValue(oldapiname);
	DocletParam dp2 = d.createParam();
	dp2.setName("-newapi");
	dp2.setValue(newapiname);
	
	// Execute the Javadoc command to generate the XML representation of 
	// the old version.
	jd.perform();
    }

    /** No default value for the directory where the reports go is provided. */
    private File destdir;

    private OldProject oldProject = null;
    private NewProject newProject = null;

    public void setDestdir(File value) {
	this.destdir = value;
    }

    public File getDestdir() {
	return this.destdir;
    }

    public void addConfiguredOld(OldProject anOld) {
	oldProject = anOld;
    }

    public void addConfiguredNew(NewProject aNew) {
	newProject = aNew;
    }

    public static class OldProject extends ProjectInfo {
    }

    public static class NewProject extends ProjectInfo {
    }

    public static class ProjectInfo {
	/** The name of the project */
	private String name;
	public void setName(String value) {
	    name = value;
	}
	public String getName() {
	    return name;
	}

 	/** The files to be compared as part of the project */
	private FileSet fileset = null;
	public void setFileset(FileSet value) {
	    fileset = value;
	}
	public FileSet getFileset() {
	    return fileset;
	}

	/** Called when the fileset element is encountered */
	public void addFileset(FileSet aFileset) {
	    // TODO check only one fileset allowed?
	    setFileset(aFileset);
	}

 	/** The files to be compared as part of the project TODO could share */
	private DirSet dirset = null;
	public void setDirset(DirSet value) {
	    dirset = value;
	}
	public DirSet getDirset() {
	    return dirset;
	}

	/** Called when the dirset element is encountered */
	public void addDirset(DirSet aDirset) {
	    setDirset(aDirset);
	}
	
	public void execute() {
	}
    }

}
