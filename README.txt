
                           JDiff Doclet

                           Matthew Doar
                          doar@pobox.com

The JDiff doclet is used to generate a report describing the
difference between two public Java APIs. For more information on
the doclet and instructions for using it, see:

examples/README.html

./jdiff.html contains the reference page for JDiff. 

An example of using JDiff to compare the public APIs of J2SE1.2 and
J2SE1.3 can be seen at: 

http://javadiff.sourceforge.net/jdiff/jdkchanges/changes.html

or via http://www.pobox.com/~doar and the SourceForge javadiff project 
homepage.

VERSION:

The latest version of JDiff can be downloaded at

http://sourceforge.net/projects/javadiff/

SYSTEM REQUIREMENTS:

JDiff has been tested with J2SE1.2, J2SE1.3 and J2SE1.4B3.

You need to have xerces.jar (used for XML parsing) from the Apache
project on your classpath. The version tested with was Xerces 1.4.2.
This is included in this release, or can be downloaded from
http://www.apache.org.

JDiff is licensed under the Lesser GNU General Public License (LGPL).
See the file LICENSE.txt.

ACKNOWLEDGEMENTS

JDiff uses Stuart D. Gathman's Java translation of Gene Myers' O(ND) 
difference algorithm.

Many thanks to the reviewers at Sun and Vitria who gave feedback on early
versions of JDiff output, and also to Arturo Fuente for his consistently
fine cigars which helped inspire much of this work.
