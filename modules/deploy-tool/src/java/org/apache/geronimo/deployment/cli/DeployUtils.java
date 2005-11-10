/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.cli;

import java.io.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.apache.geronimo.common.DeploymentException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Various helpers for deployment.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DeployUtils {
    /**
     * Split up an output line so it indents at beginning and end (to fit in a
     * typical terminal) and doesn't break in the middle of a word.
     * @param source The unformatted String
     * @param indent The number of characters to indent on the left
     * @param endCol The maximum width of the entire line in characters,
     *               including indent (indent 10 with endCol 70 results
     *               in 60 "usable" characters).
     * @return
     */
    public static String reformat(String source, int indent, int endCol) {
        if(endCol-indent < 10) {
            throw new IllegalArgumentException("This is ridiculous!");
        }
        StringBuffer buf = new StringBuffer((int)(source.length()*1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        try {
            BufferedReader in = new BufferedReader(new StringReader(source));
            String line;
            int pos;
            while((line = in.readLine()) != null) {
                if(buf.length() > 0) {
                    buf.append('\n');
                }
                while(line.length() > 0) {
                    line = prefix + line;
                    if(line.length() > endCol) {
                        pos = line.lastIndexOf(' ', endCol);
                        if(pos < indent) {
                            pos = line.indexOf(' ', endCol);
                            if(pos < indent) {
                                pos = line.length();
                            }
                        }
                        buf.append(line.substring(0, pos)).append('\n');
                        if(pos < line.length()-1) {
                            line = line.substring(pos+1);
                        } else {
                            break;
                        }
                    } else {
                        buf.append(line).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("This should be impossible");
        }
        return buf.toString();
    }

    private static String buildIndent(int indent) {
        StringBuffer buf = new StringBuffer(indent);
        for(int i=0; i<indent; i++) {
            buf.append(' ');
        }
        return buf.toString();
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
     * @param module A Jar file or directory representing a J2EE module
     * @return The configId in the Geronimo deployment plan for this module,
     *         or null if no Geronimo deployment plan was identified.
     */
    public static String extractModuleIdFromArchive(File module) throws IOException, DeploymentException {
        if(!module.canRead()) {
            throw new IllegalArgumentException("Not a readable file");
        }
        if(module.isDirectory()) {
            File target = null;
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
            }
            if(target != null && target.canRead()) {
                Reader in = new BufferedReader(new FileReader(target));
                return extractModuleIdFromPlan(in);
            }
        } else {
            if(!isJarFile(module)) {
                throw new IllegalArgumentException(module.getAbsolutePath()+" is neither a JAR file nor a directory!");
            }
            JarFile input = new JarFile(module);
            //todo: instead of looking for specific file names here, do something generic.
            //      Perhaps load a DConfigBeanRoot and look for a configId property on the first child,
            //      though that would probably be a little heavyweight.
            try {
                JarEntry entry = null;
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

    private static class ConfigIdHandler extends DefaultHandler {
        private String configId;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(configId == null) {
                configId = attributes.getValue("configId");
            }
        }
    }
}
