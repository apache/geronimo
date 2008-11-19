/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class GShellCommandRegistration {
    
    private static final String INDENT = "    ";
           
    private static boolean updateClassworlds(File classworldsFile, String classworlsTest, String classworlds) throws Exception {
        boolean updated = checkClassworlds(classworldsFile, classworlsTest);
        if (updated) {
            return false;
        }
                             
        File tmpFile = new File(classworldsFile.getAbsolutePath() + ".tmp");
        PrintWriter writer = new PrintWriter(new FileWriter(tmpFile));
        BufferedReader reader = new BufferedReader(new FileReader(classworldsFile));
        boolean inGShellSection = false;
        String line = null;     
        while( (line = reader.readLine()) != null) {
            writer.println(line);
                
            if (line.startsWith("[gshell]")) {                    
                inGShellSection = true;     
            } else if (line.startsWith("[")) {
                inGShellSection = false;
            } else if (inGShellSection) {
                if (!line.startsWith(INDENT)) {
                    break;
                }
            }
        }
            
        if (inGShellSection) {
            writer.println(INDENT + classworlds);
        }
            
        while( (line = reader.readLine()) != null) {
            writer.println(line);
        }
            
        reader.close();
        writer.close();
            
        switchFile(tmpFile, classworldsFile);
        
        return true;
    }    
    
    private static boolean checkClassworlds(File classworldsFile, String classworlsTest) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(classworldsFile));
        String line = null;
        boolean matches = false;
        while( (line = reader.readLine()) != null) {
            if (line.matches(classworlsTest)) {
                matches = true;
                break;
            }            
        }
        reader.close();
        return matches;
    }
            
    private static void switchFile(File tmpFile, File realFile) {
        realFile.delete();
        if (!tmpFile.renameTo(realFile)) {
            throw new RuntimeException("Failed to rename " + tmpFile + " to " + realFile);
        }
    }
    
    public static void main(String [] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Syntax: GShellCommandRegistration <geronimo_home> <properties file>");
        }
        String baseDir = args[0];
        String propsFile = args[1];
        
        ClassLoader loader = GShellCommandRegistration.class.getClassLoader();
        InputStream in = loader.getResourceAsStream(propsFile);
        
        if (in == null) {
            in = new FileInputStream(propsFile);
           // throw new Exception("Failed to load properties file: " + propsFile);
        }
                
        Properties p = new Properties();
        p.load(in);
                       
        // update layout.xml
        String layoutCommands = p.getProperty("layout");
        
        if (layoutCommands != null) {
            File layoutFile = new File(baseDir, "etc/layout.xml");
            Layout layout = new Layout(layoutFile);
            boolean modified = false;
            String [] commands = layoutCommands.split(",");
            for (String command : commands) {
                command = command.trim();
                if (command.length() == 0) {
                    continue;
                }
                String [] cmd = command.split(" ");
                String operation = cmd[0];
                if ("addCommand".equals(operation)) {
                    String groupName = (cmd.length > 3) ? cmd[3] : null;
                    if (layout.addCommand(cmd[1], cmd[2], groupName)) {
                        modified = true;
                    }
                } else {
                    throw new Exception("Unsupported layout command: " + operation);
                }
            }
            if (modified) {
                System.out.println("Updated layout.xml");
                layout.save();
            }
        }
        
        // update gsh-classworlds.conf
        String classworldsRegExTest = p.getProperty("classworldsRegExTest");
        String classworldsEntry = p.getProperty("classworldsEntry");
        if (classworldsRegExTest != null && classworldsEntry != null) {
            File classworldsFile = new File(baseDir, "etc/gsh-classworlds.conf");            
            if (updateClassworlds(classworldsFile, classworldsRegExTest, classworldsEntry)) {
                System.out.println("Updated gsh-classworlds.conf");
            }
        }
    }
     
    private static class Layout {
        
        private File layoutFile;
        private Document layoutDocument;
        
        public Layout(File layoutFile) throws Exception {
            this.layoutFile = layoutFile;
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            this.layoutDocument = domFactory.newDocumentBuilder().parse(layoutFile);
        }
        
        public boolean addGroup(String groupName) throws Exception {
            Element group = getGroup(groupName);
            if (group == null) {
                Node nodes = getNodes(this.layoutDocument.getDocumentElement());
                nodes.appendChild(createGroup(groupName));
                return true;
            }
            return false;
        }
        
        public Element getGroup(String groupName) throws Exception {
            return getElement(this.layoutDocument, "//layout/nodes/group/name[contains(.,'" + groupName + "')]/..");
        }
        
        public boolean addCommand(String commandName, String commandId, String groupName) throws Exception {
            if (groupName == null) {
                Element nodes = getNodes(this.layoutDocument.getDocumentElement());
                return addCommand(nodes, commandName, commandId);
            } else {
                Element group = getGroup(groupName);
                if (group == null) {
                    Node nodes = getNodes(this.layoutDocument.getDocumentElement());
                    group = createGroup(groupName);
                    nodes.appendChild(group);
                }
                Element nodes = getNodes(group);
                if (nodes == null) {
                    nodes = this.layoutDocument.createElement("nodes");
                    group.appendChild(nodes);
                    nodes.appendChild(createCommand(commandName, commandId));
                    return true;
                } else {
                    return addCommand(nodes, commandName, commandId);
                }
            }
        }

        private boolean addCommand(Element nodes, String commandName, String commandId) throws Exception {
            Element command = getCommand(nodes, commandName);
            if (command == null) {
                nodes.appendChild(createCommand(commandName, commandId));
                return true;
            } else {
                return false;
            }
        }
        
        private Element createGroup(String groupName) {
            Element group = layoutDocument.createElement("group");
            group.appendChild(createElement("name", groupName));
            return group;
        }
        
        private Element createCommand(String commandName, String commandId) {
            Element command = layoutDocument.createElement("command");
            command.appendChild(createElement("name", commandName));
            command.appendChild(createElement("id", commandId));
            return command;
        }
        
        private Element createElement(String name, String value) {
            Element node = layoutDocument.createElement(name);
            Text text = layoutDocument.createTextNode(value);
            node.appendChild(text);
            return node;
        }

        private Element getNodes(Node root) throws Exception {
            return getElement(root, "./nodes");            
        }
        
        public Element getCommand(Node root, String name) throws Exception {
            return getElement(root, "./command/name[contains(.,'" + name + "')]/..");
        }
        
        private Element getElement(Node root, String query) throws XPathExpressionException {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xPath = xpathFactory.newXPath();            
            Node node = (Node)xPath.evaluate(query, root, XPathConstants.NODE);
            return (Element)node;  
        }
        
        public void save() throws Exception {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
     
            DOMSource source = new DOMSource(layoutDocument);
            File tmpFile = new File(this.layoutFile.getAbsolutePath() + ".tmp");
            FileOutputStream out = new FileOutputStream(tmpFile);
            StreamResult result = new StreamResult(out);
            try {
                transformer.transform(source, result);
            } finally {
                out.close();
            }
                
            switchFile(tmpFile, this.layoutFile);
        }
    }
}
