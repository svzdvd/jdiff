package jdiff;

import java.io.*;
import java.util.*;

/** 
 * Class to represent a package, analogous to PackageDoc in the 
 * JavaDoc doclet API. 
 *
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this package.
 *
 * @author Matthew Doar, doar@pobox.com
 */
class PackageAPI implements Comparable {

    /** Full qualified name of the package. */
    public String name_;

    /** Classes within this package. */
    public List classes_;  // ClassAPI[]

    /** The doc block, default is null. */
    public String doc_ = null;

    /** Constructor. */
    public PackageAPI(String name) {
        name_ = name;
        classes_ = new ArrayList(); // ClassAPI[]
    }

    /** Compare two PackageAPI objects by name. */
    public int compareTo(Object o) {
        return name_.compareTo(((PackageAPI)o).name_);
    }
}
