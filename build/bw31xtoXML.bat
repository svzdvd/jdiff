REM This would scan all the private classes and members too
REM javadoc -private -excludeclass private -excludemember private 

REM Remember to set the sourcepath to find the Java source files on your system
setlocal
set SRCPATH=C:\apollo\nagus\export\java

javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -apiname BW3x -sourcepath .;%SRCPATH% com.vitria.analyzer.client com.vitria.analyzer.collect com.vitria.analyzer.common com.vitria.analyzer.runtime com.vitria.analyzer.service com.vitria.analyzer.type com.vitria.bclient com.vitria.bpe.audit com.vitria.bpe.client com.vitria.bpe.process com.vitria.bpe.server com.vitria.bpe.worklist com.vitria.bservlet com.vitria.cmodel com.vitria.connectors.common com.vitria.connectors.datalators.simpletranslator com.vitria.connectors.http com.vitria.connectors.request com.vitria.dsserv com.vitria.est.chanbeans com.vitria.est.widget com.vitria.fc.data com.vitria.diag com.vitria.fc.diag com.vitria.fc.flow com.vitria.fc.folder com.vitria.fc.io com.vitria.fc.meta com.vitria.fc.object com.vitria.fc.trans com.vitria.fc.thread com.vitria.fc.record com.vitria.install com.vitria.jct com.vitria.mime com.vitria.preferences com.vitria.roi.exchange com.vitria.roi.javanative com.vitria.roi.marshal com.vitria.roi.object com.vitria.rstore com.vitria.web.awi
