REM This would scan all the private classes and members too
REM javadoc -private -excludeclass private -excludemember private 

REM Remember to set the sourcepath to find the Java source files on your system
setlocal
set SRCPATH=C:\bravo\nagus\export\java

javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -apiname BW401 -sourcepath .;%SRCPATH% com.vitria.analyzer.client com.vitria.analyzer.collect com.vitria.analyzer.common com.vitria.analyzer.runtime com.vitria.analyzer.service com.vitria.analyzer.type com.vitria.bclient com.vitria.bpe.activity com.vitria.bpe.exception com.vitria.bpe.process com.vitria.bpe.runtime com.vitria.bpe.server com.vitria.bservlet com.vitria.connectors.datalators.simpletranslator com.vitria.connectors.email com.vitria.connectors.file com.vitria.connectors.http com.vitria.connectors.request com.vitria.container com.vitria.diag com.vitria.dsserv com.vitria.fc.archiver com.vitria.fc.data com.vitria.fc.diag com.vitria.fc.directory com.vitria.fc.flow com.vitria.fc.folder com.vitria.fc.io com.vitria.fc.meta com.vitria.fc.object com.vitria.fc.record com.vitria.fc.thread com.vitria.fc.trans com.vitria.fc.xlate com.vitria.ide.propertyeditors com.vitria.install com.vitria.jct com.vitria.modeling.common com.vitria.msg com.vitria.project com.vitria.roi.exchange com.vitria.roi.javanative com.vitria.roi.marshal com.vitria.roi.object com.vitria.rstore com.vitria.web.awi
