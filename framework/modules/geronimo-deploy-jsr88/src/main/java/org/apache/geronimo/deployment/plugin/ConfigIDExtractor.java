/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.util.BundleUtil;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Knows how to suck a Config ID out of a module and/or plan
 *
 * @version $Rev$ $Date$
 */
public class ConfigIDExtractor {

    private static final Logger log = LoggerFactory.getLogger(ConfigIDExtractor.class);
    
    private static final String APPLICATION_SYMBOLICNAME="Application-SymbolicName";
    
    private static final String APPLICATION_VERION="Application-Version";

    /**
     * Attempt to calculate the Geronimo ModuleID for a J2EE application
     * module.
     *
     * Given a File representing an archive (which may be a JAR file or a
     * directory laid out like a JAR file), identify it's J2EE module type
     * based on which (if any) deployment descriptor is present, and then look
     * for a Geronimo deployment plan in the usual place, and if one is found,
     * retrieve the configId from the Geronimo deployment plan.
     *
     * todo: Handle Spring and other weird deployment types?
     *
     * @param module A Jar file or directory representing a J2EE module
     * @return The configId in the Geronimo deployment plan for this module,
     *         or null if no Geronimo deployment plan was identified.
     */
    public static String extractModuleIdFromArchive(File module) throws IOException, DeploymentException {
        if(!module.canRead()) {
            throw new DeploymentException("Not a readable file ("+module.getAbsolutePath()+")");
        }
        if(module.isDirectory()) {
            File target;
            if(new File(module, "WEB-INF/web.xml").canRead() || new File(module, "WEB-INF/geronimo-web.xml").canRead()) {
                target = new File(module, "WEB-INF/geronimo-web.xml");
            } else if(new File(module, "META-INF/application.xml").canRead() || new File(module, "META-INF/geronimo-application.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-application.xml");
            } else if (new File(module, "META-INF/ejb-jar.xml").canRead() || new File(module, "META-INF/openejb-jar.xml").canRead()) {
            	target = new File(module, "META-INF/openejb-jar.xml");
            } else if(new File(module, "META-INF/ra.xml").canRead() || new File(module, "META-INF/geronimo-ra.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-ra.xml");
            } else if(new File(module, "META-INF/application-client.xml").canRead() || new File(module, "META-INF/geronimo-application-client.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-application-client.xml");
            }  else if(new File(module,"META-INF/APPLICATION.MF").canRead()) {
                target = new File(module,"META-INF/APPLICATION.MF");
            }  else {
                target = new File(module, "META-INF/geronimo-service.xml");
            }
            if(target.canRead()) {
                Reader in = new BufferedReader(new FileReader(target));
                
                String name = null;
                if (target.getName().endsWith("xml")) {
                    name = extractModuleIdFromPlan(in);
                } else if (target.getName().endsWith("MF")) {
                    name = extractModuleIdFromAPPLICATION_MF(new FileInputStream(target));
                }
                
                if(name != null) {
                    Artifact artifact = Artifact.create(name);
                    if(artifact.getArtifactId() == null) {
                        name = new Artifact(artifact.getGroupId(), module.getName(), artifact.getVersion(), artifact.getType()).toString();
                    }
                }
                return name;
            }
        } else {
            if(!JarUtils.isZipFile(module)) {
                throw new DeploymentException(module.getAbsolutePath()+" is neither a JAR file nor a directory!");
            }
            JarFile input = new JarFile(module);
            //todo: instead of looking for specific file names here, do something generic.
            //      Perhaps load a DConfigBeanRoot and look for a configId property on the first child,
            //      though that would probably be a little heavyweight.
            try {
                JarEntry entry;
                if(input.getJarEntry("WEB-INF/web.xml") != null || input.getJarEntry("WEB-INF/geronimo-web.xml") != null) {
                    entry = input.getJarEntry("WEB-INF/geronimo-web.xml");
                } else if(input.getJarEntry("META-INF/application.xml") != null || input.getJarEntry("META-INF/geronimo-application.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-application.xml");
                } else if(input.getJarEntry("META-INF/ejb-jar.xml") != null || input.getJarEntry("META-INF/openejb-jar.xml") != null) {
                	entry = input.getJarEntry("META-INF/openejb-jar.xml");
                } else if(input.getJarEntry("META-INF/ra.xml") != null || input.getJarEntry("META-INF/geronimo-ra.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-ra.xml");
                } else if(input.getJarEntry("META-INF/application-client.xml") != null || input.getJarEntry("META-INF/geronimo-application-client.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-application-client.xml");
                }  else if(input.getJarEntry("META-INF/APPLICATION.MF") != null) {
                    entry = input.getJarEntry("META-INF/APPLICATION.MF");
                } else {
                    entry = input.getJarEntry("META-INF/geronimo-service.xml");
                }
                
                if(entry != null) {
                    Reader in = new BufferedReader(new InputStreamReader(input.getInputStream(entry)));
                    
                    String name = null;

                    if (entry.getName().endsWith("xml")) {
                        name = extractModuleIdFromPlan(in);
                    } else if (entry.getName().endsWith("MF")) {
                        name = extractModuleIdFromAPPLICATION_MF(input.getInputStream(entry));
                    }
                    
                    if(name != null) {
                        Artifact artifact = Artifact.create(name);
                        if(artifact.getArtifactId() == null) {
                            name = new Artifact(artifact.getGroupId(), module.getName(), artifact.getVersion(), artifact.getType()).toString();
                        }
                    }
                    return name;
                }
            } finally {
                input.close();
            }
        }
        return null;
    }

    /**
     * Attempt to calculate the Geronimo ModuleID for a Geronimo deployment
     * plan.
     *
     * @param plan A Geronimo deployment plan (which must be an XML file).
     * @return The configId in the Geronimo deployment plan for this module.
     */
    public static String extractModuleIdFromPlan(File plan) throws IOException {
        if(plan.isDirectory() || !plan.canRead()) {
            throw new IllegalArgumentException(plan.getAbsolutePath()+" is not a readable XML file!");
        }
        Reader in = new BufferedReader(new FileReader(plan));
        return extractModuleIdFromPlan(in);
    }

    /**
     * Given a list of all available TargetModuleIDs and the name of a module,
     * find the TargetModuleIDs that represent that module.
     *
     * @param allModules  The list of all available modules
     * @param name        The module name to search for
     * @param fromPlan    Should be true if the module name was loaded from a
     *                    deployment plan (thus no group means the default
     *                    group) or false if the module name was provided by
     *                    the user (thus no group means any group).
     *
     * @throws DeploymentException If no TargetModuleIDs have that module.
     */
    public static Collection<TargetModuleID> identifyTargetModuleIDs(TargetModuleID[] allModules, String name, boolean fromPlan) throws DeploymentException {
        List<TargetModuleID> list = new LinkedList<TargetModuleID>();
        int pos;
        if((pos = name.indexOf('|')) > -1) {
            String target = name.substring(0, pos);
            String module = name.substring(pos+1);
            Artifact artifact = Artifact.create(module);
            artifact = new Artifact(artifact.getGroupId() == null && fromPlan ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                    artifact.getArtifactId(), fromPlan ? (Version)null : artifact.getVersion(), artifact.getType());
            // First pass: exact match
            for(int i=0; i<allModules.length; i++) {
                if(allModules[i].getTarget().getName().equals(target) && artifact.matches(Artifact.create(allModules[i].getModuleID()))) {
                    list.add(allModules[i]);
                }
            }
        }
        if(!list.isEmpty()) {
            return list;
        }
        // second pass: module matches
        Artifact artifact;
        if (name.indexOf("/") > -1) {
            artifact = Artifact.create(name);
            artifact = new Artifact(artifact.getGroupId() == null && fromPlan ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                    artifact.getArtifactId(), fromPlan ? (Version)null : artifact.getVersion(), artifact.getType());
        } else {
            artifact = new Artifact(fromPlan ? Artifact.DEFAULT_GROUP_ID : null, name, (Version)null, null);
        }
        for(int i = 0; i < allModules.length; i++) {
            if(artifact.matches(Artifact.create(allModules[i].getModuleID()))) {
                list.add(allModules[i]);
            }
        }
        if(list.isEmpty()) {
            throw new DeploymentException(name+" does not appear to be a the name of a module " +
                    "available on the selected server. Perhaps it has already been " +
                    "stopped or undeployed?  If you're trying to specify a " +
                    "TargetModuleID, use the syntax TargetName|ModuleName instead. " +
                    "If you're not sure what's running, try the list-modules command.");
        }
        return list;
    }

    
    private static String extractModuleIdFromAPPLICATION_MF(InputStream in) throws IOException,DeploymentException {

        Manifest appMf = new Manifest(in);
        
        String artifactID = appMf.getMainAttributes().getValue(APPLICATION_SYMBOLICNAME);

        String artifactVersion = appMf.getMainAttributes().getValue(APPLICATION_VERION);;
        
        if (artifactID == null || artifactVersion == null) {
            throw new DeploymentException("Could not determine artifact or version with APPLICATION.MF of your EBA application");
        }
        
        org.osgi.framework.Version version=new org.osgi.framework.Version(artifactVersion.trim());

        return new Artifact(BundleUtil.EBA_GROUP_ID, artifactID.trim(), BundleUtil.getVersion(version), "eba").toString();
    } 
    
    
    
    private static String extractModuleIdFromPlan(Reader plan) throws IOException {
        SAXParserFactory factory = XmlUtil.newSAXParserFactory();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        try {
            SAXParser parser = factory.newSAXParser();
            ConfigIdHandler handler = new ConfigIdHandler();
            parser.parse(new InputSource(plan), handler);
            if(handler.formatIs10) {
                log.warn("Geronimo deployment plan uses Geronimo 1.0 syntax.  Please update to Geronimo 1.1 syntax when possible.");
            }
            return handler.configId;
        } catch (ParserConfigurationException e) {
            throw (IOException)new IOException("Unable to read plan: "+e.getMessage()).initCause(e);
        } catch (SAXException e) {
            throw (IOException)new IOException("Unable to read plan: "+e.getMessage()).initCause(e);
        } finally {
            plan.close();
        }
    }

    private static class ConfigIdHandler extends DefaultHandler {
        private String configId;
        private boolean inConfigId;
        private String groupId = "", artifactId = "", version = "", type = "";
        private String inElement = null;
        private Stack parent = new Stack();
        private boolean formatIs10 = false;
        private String defaultType;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(defaultType == null && uri != null && !uri.equals("")) {
                setDefaultType(uri);
            }
            if(inConfigId) {
                if(localName.equals("groupId") || localName.equals("artifactId") || localName.equals("version") || localName.equals("type")) {
                    inElement = localName;
                }
            } else {
                if(parent.size() == 2 && localName.equals("moduleId")) {
                    inConfigId = true; // only document/environment/configId, not e.g. configId in nested plan in EAR
                } else {
                    if(parent.size() == 0 && attributes.getIndex("moduleId") > -1) {
                        configId = attributes.getValue("moduleId");
                        formatIs10 = true;
                    }
                }
            }
            parent.push(localName);
        }

        private void setDefaultType(String namespace) {
            if(namespace.indexOf("web") > -1) {
                defaultType = "war";
            } else if(namespace.indexOf("openejb") > -1) {
                defaultType = "jar";
            } else if(namespace.indexOf("connector") > -1) {
                defaultType = "rar";
            } else if(namespace.indexOf("application-client") > -1) {
                defaultType = "jar";
            } else if(namespace.indexOf("application") > -1) {
                defaultType = "ear";
            } else {
                defaultType = "car";
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(inElement != null) {
                formatIs10 = false;
                if(inElement.equals("groupId")) groupId += new String(ch, start, length);
                else if(inElement.equals("artifactId")) artifactId += new String(ch, start, length);
                else if(inElement.equals("version")) version += new String(ch, start, length);
                else if(inElement.equals("type")) type += new String(ch, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            inElement = null;
            if(inConfigId && localName.equals("moduleId")) {
                inConfigId = false;
            }
            if(parent.peek().equals(localName)) {
                parent.pop();
            } else {
                throw new IllegalStateException("End of "+localName+" but expecting "+parent.peek());
            }
        }

        public void endDocument() throws SAXException {
            if(!formatIs10) {
                if(type.equals("") && defaultType != null) {
                    type = defaultType;
                }
                configId = groupId+"/"+artifactId+"/"+version+"/"+type;
            }
            if(configId.equals("///")) {
                configId = null;
            }
        }
    }
}
