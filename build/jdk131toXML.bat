REM This would scan all the private classes and members too
REM javadoc -private -excludeclass private -excludemember private 

REM Remember to set the sourcepath to find the Java source files on your system
setlocal
set SRCPATH=C:\jdktests\1.3.1\src
REM Packages java.awt.dnd.peer and java.text.resources appear in the source 
REM code, but not in the API documentation, so are not compared here.

javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname J2SE1.3.1 -sourcepath .;%SRCPATH% java.applet java.awt java.awt.color java.awt.datatransfer java.awt.dnd java.awt.event java.awt.font java.awt.geom java.awt.im java.awt.im.spi java.awt.image java.awt.image.renderable java.awt.print java.beans java.beans.beancontext java.io java.lang java.lang.ref java.lang.reflect java.math java.net java.rmi java.rmi.activation java.rmi.dgc java.rmi.registry java.rmi.server java.security java.security.acl java.security.cert java.security.interfaces java.security.spec java.sql java.text java.util java.util.jar java.util.zip javax.accessibility javax.naming javax.naming.directory javax.naming.event javax.naming.ldap javax.naming.spi javax.swing javax.swing.border javax.swing.colorchooser javax.swing.event javax.swing.filechooser javax.swing.plaf javax.swing.plaf.basic javax.swing.plaf.metal javax.swing.plaf.multi javax.swing.table javax.swing.text javax.swing.text.html javax.swing.text.html.parser javax.swing.text.rtf javax.swing.tree javax.swing.undo org.omg.CORBA org.omg.CORBA.DynAnyPackage org.omg.CORBA.ORBPackage org.omg.CORBA.portable org.omg.CORBA.TypeCodePackage org.omg.CORBA_2_3 org.omg.CORBA_2_3.portable org.omg.CosNaming org.omg.CosNaming.NamingContextPackage org.omg.SendingContext org.omg.stub.java.rmi