package jdiff;

import java.util.*;
import com.sun.javadoc.*;

/**
 * Changes between two packages.
 *
 * @author Matthew Doar, doar@pobox.com
 */
class PackageDiff {

    public String name_;

    /** Classes added in the new API. */
    public List classesAdded = null;
    /** Classes removed in the new API. */
    public List classesRemoved = null;
    /** Classes changed in the new API. */
    public List classesChanged = null;

    /** Default constructor. */
    public PackageDiff(String name) {
        name_ = name;
        classesAdded = new ArrayList(); // ClassAPI[]
        classesRemoved = new ArrayList(); // ClassAPI[]
        classesChanged = new ArrayList(); // ClassDiff[]
    }   
}
