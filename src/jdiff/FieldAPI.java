package jdiff;

import java.io.*;
import java.util.*;

/** 
 * Class to represent a field, analogous to FieldDoc in the 
 * Javadoc doclet API. 
 * 
 * The method used for Collection comparison (compareTo) must make its
 * comparison based upon everything that is known about this field.
 *
 * See the file LICENSE.txt for copyright details.
 * @author Matthew Doar, doar@pobox.com
 */
class FieldAPI implements Comparable {
    /** Name of the field. */
    public String name_;

    /** Type of the field. */
    public String type_;

    /** Set if this field is transient. */
    boolean isTransient_ = false;

    /** Set if this field is volatile. */
    boolean isVolatile_ = false;

    /** Modifiers for this class. */
    public Modifiers modifiers_;

    /** The doc block, default is null. */
    public String doc_ = null;

    /** Constructor. */
    public FieldAPI(String name, String type, 
                    boolean isTransient, boolean isVolatile, 
                    Modifiers modifiers) {
        name_ = name;
        type_ = type;
        isTransient_ = isTransient;
        isVolatile_ = isVolatile;
        modifiers_ = modifiers;
    }

    /** Compare two FieldAPI objects, including name, type and modifiers. */
    public int compareTo(Object o) {
        FieldAPI oFieldAPI = (FieldAPI)o;
        int comp = name_.compareTo(oFieldAPI.name_);
        if (comp != 0)
            return comp;
        comp = type_.compareTo(oFieldAPI.type_);
        if (comp != 0)
            return comp;
        if (isTransient_ != oFieldAPI.isTransient_) {
            return -1;
        }
        if (isVolatile_ != oFieldAPI.isVolatile_) {
            return -1;
        }
        comp = modifiers_.compareTo(oFieldAPI.modifiers_);
        if (comp != 0)
            return comp;
        if (APIComparator.docChanged(doc_, oFieldAPI.doc_))
            return -1;
        return 0;
    }
  
    /** 
     * Tests two fields, using just the field name, used by indexOf().
     */
    public boolean equals(Object o) {
        if (name_.compareTo(((FieldAPI)o).name_) == 0)
            return true;
        return false;
    }
}  
