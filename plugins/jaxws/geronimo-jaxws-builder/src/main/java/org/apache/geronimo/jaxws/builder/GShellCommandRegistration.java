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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GShellCommandRegistration {
    
    private static String INDENT = "    ";
           
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
    
    private static boolean updateLayout(File layoutFile, String layoutTest, String layout) throws Exception {  
        
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        Document layoutDocument = domFactory.newDocumentBuilder().parse(layoutFile);
        
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xPath = xpathFactory.newXPath();
        
        Boolean updated = (Boolean)xPath.evaluate(layoutTest, layoutDocument, XPathConstants.BOOLEAN);
        
        if (updated.booleanValue()) {
            return false;
        }
                            
        Document doc = domFactory.newDocumentBuilder().parse(new InputSource(new StringReader(layout)));
            
        Element layoutElement = layoutDocument.getDocumentElement();
        NodeList nodes = layoutElement.getElementsByTagName("nodes");
        Node n = layoutDocument.importNode(doc.getDocumentElement(), true);
        nodes.item(0).appendChild(n);
            
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
 
        DOMSource source = new DOMSource(layoutDocument);
        File tmpFile = new File(layoutFile.getAbsolutePath() + ".tmp");
        FileOutputStream out = new FileOutputStream(tmpFile);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);                    
        out.close();
            
        switchFile(tmpFile, layoutFile);
            
        return true;        
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
            throw new Exception("Failed to load properties file: " + propsFile);
        }
                
        Properties p = new Properties();
        p.load(in);
                       
        // update layout.xml
        String layoutXPathTest = p.getProperty("layoutXPathTest");
        String layoutEntry = p.getProperty("layoutEntry");
        
        if (layoutXPathTest != null && layoutEntry != null) {
            File layoutFile = new File(baseDir, "etc/layout.xml");
            if (updateLayout(layoutFile, layoutXPathTest, layoutEntry)) {
                System.out.println("Registered commands in layout.xml");
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
     
}
