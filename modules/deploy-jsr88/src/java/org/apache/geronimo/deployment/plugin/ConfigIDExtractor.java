/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Stack;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Knows how to suck a Config ID out of a module and/or plan
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigIDExtractor {

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
            if(new File(module, "WEB-INF/web.xml").canRead()) {
                target = new File(module, "WEB-INF/geronimo-web.xml");
            } else if(new File(module, "META-INF/application.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-application.xml");
            } else if(new File(module, "META-INF/ejb-jar.xml").canRead()) {
                target = new File(module, "META-INF/openejb-jar.xml");
            } else if(new File(module, "META-INF/ra.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-ra.xml");
            } else if(new File(module, "META-INF/application-client.xml").canRead()) {
                target = new File(module, "META-INF/geronimo-application-client.xml");
            } else {
                target = new File(module, "META-INF/geronimo-service.xml");
            }
            if(target.canRead()) {
                Reader in = new BufferedReader(new FileReader(target));
                return extractModuleIdFromPlan(in);
            }
        } else {
            if(!isJarFile(module)) {
                throw new DeploymentException(module.getAbsolutePath()+" is neither a JAR file nor a directory!");
            }
            JarFile input = new JarFile(module);
            //todo: instead of looking for specific file names here, do something generic.
            //      Perhaps load a DConfigBeanRoot and look for a configId property on the first child,
            //      though that would probably be a little heavyweight.
            try {
                JarEntry entry;
                if(input.getJarEntry("WEB-INF/web.xml") != null) {
                    entry = input.getJarEntry("WEB-INF/geronimo-web.xml");
                } else if(input.getJarEntry("META-INF/application.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-application.xml");
                } else if(input.getJarEntry("META-INF/ejb-jar.xml") != null) {
                    entry = input.getJarEntry("META-INF/openejb-jar.xml");
                } else if(input.getJarEntry("META-INF/ra.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-ra.xml");
                } else if(input.getJarEntry("META-INF/application-client.xml") != null) {
                    entry = input.getJarEntry("META-INF/geronimo-application-client.xml");
                } else {
                    entry = input.getJarEntry("META-INF/geronimo-service.xml");
                }
                if(entry != null) {
                    Reader in = new BufferedReader(new InputStreamReader(input.getInputStream(entry)));
                    return extractModuleIdFromPlan(in);
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
     * @throws DeploymentException If no TargetModuleIDs have that module.
     */
    public static Collection identifyTargetModuleIDs(TargetModuleID[] allModules, String name) throws DeploymentException {
        List list = new LinkedList();
        int pos;
        if((pos = name.indexOf('|')) > -1) {
            String target = name.substring(0, pos);
            String module = name.substring(pos+1);
            Artifact artifact = Artifact.create(module);
            if(artifact.getGroupId() == null || artifact.getType() == null) {
                artifact = new Artifact(artifact.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getVersion(),
                        artifact.getType() == null ? "car" : artifact.getType());
            }
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
        Artifact artifact = Artifact.create(name);
        if(artifact.getGroupId() == null || artifact.getType() == null) {
            artifact = new Artifact(artifact.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(),
                    artifact.getType() == null ? "car" : artifact.getType());
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
    
    private static String extractModuleIdFromPlan(Reader plan) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        try {
            SAXParser parser = factory.newSAXParser();
            ConfigIdHandler handler = new ConfigIdHandler();
            parser.parse(new InputSource(plan), handler);
            return handler.configId;
        } catch (ParserConfigurationException e) {
            throw new IOException("Unable to read plan: "+e.getMessage());
        } catch (SAXException e) {
            throw new IOException("Unable to read plan: "+e.getMessage());
        } finally {
            plan.close();
        }
    }

    /**
     * Try to determine whether a file is a JAR File (or, at least, a ZIP file).
     */
    public static boolean isJarFile(File file) throws DeploymentException {
        if(file.isDirectory()) {
            return false;
        }
        if(!file.canRead()) {
            throw new DeploymentException("Cannot read file "+file.getAbsolutePath());
        }
        if(file.length() < 4) {
            return false;
        }
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            int test = in.readInt();
            in.close();
            return test == 0x504b0304;
        } catch(IOException e) {
            throw new DeploymentException("Cannot read from file "+file.getAbsolutePath(), e);
        }
    }

    private static class ConfigIdHandler extends DefaultHandler {
        private String configId;
        private boolean inConfigId;
        private String groupId = "", artifactId = "", version = "", type = "";
        private String inElement = null;
        private Stack parent = new Stack();

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(inConfigId) {
                if(localName.equals("groupId") || localName.equals("artifactId") || localName.equals("version") || localName.equals("type")) {
                    inElement = localName;
                }
            } else {
                if(parent.size() == 2 && localName.equals("configId")) {
                    inConfigId = true; // only document/environment/configId, not e.g. configId in nested plan in EAR
                }
            }
            parent.push(localName);
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(inElement != null) {
                if(inElement.equals("groupId")) groupId += new String(ch, start, length);
                else if(inElement.equals("artifactId")) artifactId += new String(ch, start, length);
                else if(inElement.equals("version")) version += new String(ch, start, length);
                else if(inElement.equals("type")) type += new String(ch, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            inElement = null;
            if(inConfigId && localName.equals("configId")) {
                inConfigId = false;
            }
            if(parent.peek().equals(localName)) {
                parent.pop();
            } else {
                throw new IllegalStateException("End of "+localName+" but expecting "+parent.peek());
            }
        }

        public void endDocument() throws SAXException {
            if(type.equals("")) {
                type = "car";
            }
            configId = groupId+"/"+artifactId+"/"+version+"/"+type;
        }
    }
}
