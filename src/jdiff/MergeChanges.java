package jdiff;

import java.util.*;

/**
 * Convert some remove and add operations into change operations.
 *
 * Once the numbers of members removed and added are known
 * we can deduce more information about changes. For instance, if there are
 * two methods with the same name, and one or more of them has a 
 * parameter type change, then this can only be reported as removing 
 * the old version(s) and adding the new version(s), because there are 
 * multiple methods with the same name. 
 *
 * However, if only <i>one</i> method with a given name is removed, and  
 * only <i>one</i> method with the same name is added, we can convert these
 * operations to a change operation. For constructors, this is true if 
 * the types are the same. For fields, the field names have to be the same.
 *
 * @author Matthew Doar, doar@pobox.com
 */
class MergeChanges {

    /**
     * Convert some remove and add operations into change operations.
     *
     * Note that if a single thread modifies a collection directly while it is 
     * iterating over the collection with a fail-fast iterator, the iterator 
     * will throw java.util.ConcurrentModificationException   
     */
    public static void mergeRemoveAdd(APIDiff apiDiff) {
        // Go through all the ClassDiff objects searching for the above cases.
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            Iterator iter2 = pkgDiff.classesChanged.iterator();
            while (iter2.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iter2.next());
                // Note: using iterators to step through the members gives a
                // ConcurrentModificationException exception with large files.
                // Constructors
                ConstructorAPI[] ctorArr = new ConstructorAPI[classDiff.ctorsRemoved.size()];
                ctorArr = (ConstructorAPI[])classDiff.ctorsRemoved.toArray(ctorArr);
                for (int ctorIdx = 0; ctorIdx < ctorArr.length; ctorIdx++) {
                    ConstructorAPI removedCtor = ctorArr[ctorIdx];
                    mergeRemoveAddCtor(removedCtor, classDiff);
                }
                // Methods
                MethodAPI[] methodArr = new MethodAPI[classDiff.methodsRemoved.size()];
                methodArr = (MethodAPI[])classDiff.methodsRemoved.toArray(methodArr);
                for (int methodIdx = 0; methodIdx < methodArr.length; methodIdx++) {
                    MethodAPI removedMethod = methodArr[methodIdx];
                    mergeRemoveAddMethod(removedMethod, classDiff);
                }
                // Fields
                FieldAPI[] fieldArr = new FieldAPI[classDiff.fieldsRemoved.size()];
                fieldArr = (FieldAPI[])classDiff.fieldsRemoved.toArray(fieldArr);
                for (int fieldIdx = 0; fieldIdx < fieldArr.length; fieldIdx++) {
                    FieldAPI removedField = fieldArr[fieldIdx];
                    mergeRemoveAddField(removedField, classDiff);
                }
            }
        }        
    }

    /**
     * Convert some removed and added constructors into changed constructors.
     */
    public static void mergeRemoveAddCtor(ConstructorAPI removedCtor, ClassDiff classDiff) {
        // Search on the type of the constructor
        int startRemoved = classDiff.ctorsRemoved.indexOf(removedCtor);
        int endRemoved = classDiff.ctorsRemoved.lastIndexOf(removedCtor);
        int startAdded = classDiff.ctorsAdded.indexOf(removedCtor);
        int endAdded = classDiff.ctorsAdded.lastIndexOf(removedCtor);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            // There is only one constructor with the type of the
            // removedCtor in both the removed and added constructors.
            ConstructorAPI addedCtor = (ConstructorAPI)(classDiff.ctorsAdded.get(startAdded));
            // Create a MemberDiff for this change
            MemberDiff ctorDiff = new MemberDiff(classDiff.name_);
            ctorDiff.oldType_ = removedCtor.type_;
            ctorDiff.newType_ = addedCtor.type_; // Should be the same as removedCtor.type
            ctorDiff.oldExceptions_ = removedCtor.exceptions_;
            ctorDiff.newExceptions_ = addedCtor.exceptions_;
            ctorDiff.addModifiersChange(removedCtor.modifiers_.diff(addedCtor.modifiers_));
            classDiff.ctorsChanged.add(ctorDiff);
            // Now remove the entries from the remove and add lists
            classDiff.ctorsRemoved.remove(startRemoved);
            classDiff.ctorsAdded.remove(startAdded);
            if (trace && ctorDiff.modifiersChange_ != null)
                System.out.println("Merged the removal and addition of constructor into one change: " + ctorDiff.modifiersChange_);
        }
    }

    /**
     * Convert some removed and added methods into changed methods.
     */
    public static void mergeRemoveAddMethod(MethodAPI removedMethod, ClassDiff classDiff) {
        // Search on the name of the method
        int startRemoved = classDiff.methodsRemoved.indexOf(removedMethod);
        int endRemoved = classDiff.methodsRemoved.lastIndexOf(removedMethod);
        int startAdded = classDiff.methodsAdded.indexOf(removedMethod);
        int endAdded = classDiff.methodsAdded.lastIndexOf(removedMethod);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            // There is only one method with the name of the
            // removedMethod in both the removed and added methods.
            MethodAPI addedMethod = (MethodAPI)(classDiff.methodsAdded.get(startAdded));
            // Create a MemberDiff for this change
            MemberDiff methodDiff = new MemberDiff(removedMethod.name_);
            methodDiff.oldType_ = removedMethod.returnType_;
            methodDiff.newType_ = addedMethod.returnType_;
            methodDiff.oldSignature_ = removedMethod.getSignature();
            methodDiff.newSignature_ = addedMethod.getSignature();
            methodDiff.oldExceptions_ = removedMethod.exceptions_;
            methodDiff.newExceptions_ = addedMethod.exceptions_;
            methodDiff.addModifiersChange(removedMethod.modifiers_.diff(addedMethod.modifiers_));
            classDiff.methodsChanged.add(methodDiff);
            // Now remove the entries from the remove and add lists
            classDiff.methodsRemoved.remove(startRemoved);
            classDiff.methodsAdded.remove(startAdded);
            if (trace) {
                System.out.println("Merged the removal and addition of method " + 
                                   removedMethod.name_ + 
                                   " into one change");
            }
        }
    }

    /**
     * Convert some removed and added fields into changed fields.
     */
    public static void mergeRemoveAddField(FieldAPI removedField, ClassDiff classDiff) {
        // Search on the name of the field
        int startRemoved = classDiff.fieldsRemoved.indexOf(removedField);
        int endRemoved = classDiff.fieldsRemoved.lastIndexOf(removedField);
        int startAdded = classDiff.fieldsAdded.indexOf(removedField);
        int endAdded = classDiff.fieldsAdded.lastIndexOf(removedField);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            // There is only one field with the name of the
            // removedField in both the removed and added fields.
            FieldAPI addedField = (FieldAPI)(classDiff.fieldsAdded.get(startAdded));
            // Create a MemberDiff for this change
            MemberDiff fieldDiff = new MemberDiff(removedField.name_);
            fieldDiff.oldType_ = removedField.type_;
            fieldDiff.newType_ = addedField.type_;
            fieldDiff.addModifiersChange(removedField.modifiers_.diff(addedField.modifiers_));
            classDiff.fieldsChanged.add(fieldDiff);
            // Now remove the entries from the remove and add lists
            classDiff.fieldsRemoved.remove(startRemoved);
            classDiff.fieldsAdded.remove(startAdded);
            if (trace) {
                System.out.println("Merged the removal and addition of field " + 
                                   removedField.name_ + 
                                   " into one change");
            }
        }
    }

    /** Set to enable increased logging verbosity for debugging. */
    private static boolean trace = false;

}
