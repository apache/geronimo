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
package org.apache.geronimo.monitoring.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * In charge of dealing with the XML processing of the snapshot's data.
 */
public class SnapshotConfigXMLBuilder {
    private static Log log = LogFactory.getLog(SnapshotConfigXMLBuilder.class);
    
    private static final String pathToXML = 
        System.getProperty("org.apache.geronimo.home.dir") + "/var/monitoring/snapshot-config.xml";
    
    private static final String SNAPSHOT_CONFIG = "snapshot-config";
    private static final String DURATION = "duration";
    private static final String RETENTION = "retention";
    private static final String MBEAN = "mbean"; 
    
    /**
     * @return A list of all mbean names that have been previously saved.
     * These mbean names are those to keep track of for per snapshot.
     */
    public static ArrayList<String> getMBeanNames() {
        ArrayList<String> mbeanList = new ArrayList<String>();
        // get an instance of the document
        Document doc = openDocument();
        // get the root element node
        Element rootElement = doc.getDocumentElement();
        // get all children in the root node (i.e. all config properties)
        NodeList configNodes = rootElement.getChildNodes();
        // find the duration node and save it
        for(int i = 0; i < configNodes.getLength(); i++) {
            if(MBEAN.equals(configNodes.item(i).getNodeName())) {
                mbeanList.add( configNodes.item(i).getTextContent() );
            }
        }
        return mbeanList;
    }
    
    /**
     * Adds to the snapshot-config.xml another configuration element <mbean>
     * in order to persistently keep track of all user requested statistics.
     * If there is a duplicate, nothing will be done.
     */
    public static boolean removeMBeanName(String mbeanName) {
        ArrayList<String> mbeanList = getMBeanNames();
        // operate on the snapshot-config.xml if there exists the mbean name
        if(mbeanList.contains(mbeanName)) {
            // get an instance of the document
            Document doc = openDocument();
            // get the root element node
            Element rootElement = doc.getDocumentElement();
            // find the Node that represents the mbeanName
            NodeList list = rootElement.getChildNodes();
            for(int i = 0; i < list.getLength(); i++) {
                // check the Node's text context for a match with mbeanName
                if(list.item(i).getTextContent().equals(mbeanName)) {
                    // remove the node from rootElement
                    Node toRemoveNode = list.item(i);
                    rootElement.removeChild(toRemoveNode);
                    break;
                }
            }
            // save the document
            saveDocument(doc, pathToXML);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes from the snapshot-config.xml a configuration element <mbean>
     * in order to persistently keep track of all user requested statistics.
     * If there does not exist an instance of the mbeanNAme, nothing will be done.
     */
    public static boolean addMBeanName(String mbeanName) {
        ArrayList<String> mbeanList = getMBeanNames();
        if(mbeanList.contains(mbeanName)) {
            return false;
        } else {
            // get an instance of the document
            Document doc = openDocument();
            // get the root element node
            Element rootElement = doc.getDocumentElement();
            // create <mbean> element
            Element mbeanElement = doc.createElement(MBEAN);
            mbeanElement.setTextContent(mbeanName);
            // add <mbean> element to the rootElement
            rootElement.appendChild(mbeanElement);
            try {
                Thread.sleep(1000);
            } catch(Exception e) {
                
            }
            // save the document
            saveDocument(doc, pathToXML);
            return true;
        }
    }
    
    /**
     * Saves the duration of the snapshot as a configuration attribute
     * @param duration
     */
    public static void saveDuration(long duration) {
        saveAttribute(DURATION, duration);
    }

    /**
     * Saves the retention of the snapshot as a configuration attribute
     * @param retention
     */
    public static void saveRetention(int retention) {
        saveAttribute(RETENTION, retention);
    }
    
    /**
     * Saves a generic attribute value into the node with text = attribute name.
     * Creates one if there is not an instance of one.
     * @param attrName
     * @param attributeValue
     */
    private static void saveAttribute(String attrName, long attributeValue) {
        Document doc = openDocument();
        // get the root node        
        Element rootElement = doc.getDocumentElement();
        // get all children in the root node (i.e. all config properties)
        NodeList configNodes = rootElement.getChildNodes();
        // find the duration node and save it
        boolean foundNode = false;
        for(int i = 0; i < configNodes.getLength() && !foundNode; i++) {
            Node configNode = configNodes.item(i);
            if(attrName.equals(configNode.getNodeName())) {
                // found a match
                configNode.setTextContent(attributeValue + "");
                foundNode = true;
            }
        }
        // if there was not a duration node, make one
        if(!foundNode) {
            Element element = doc.createElement(attrName);
            element.setTextContent(attributeValue + "");
            rootElement.appendChild(element);
        }
        try {
            Thread.sleep(1000);
        } catch(Exception e) {
            
        }
        log.info("***saving:  " + attrName + " = " + attributeValue);
        // save the document to file
        saveDocument(doc, pathToXML);
    }
    
    /**
     * Returns the value of the configuration attribute, defined by the key
     * @param key
     * @return
     * @throws Exception
     */
    public static String getAttributeValue(String key) throws Exception {
        // ensure that there exists the 'monitor' directory
        ensureMonitorDir();
        // get an instance of the document
        Document doc = openDocument();
        // get the root element node
        Element rootElement = doc.getDocumentElement();
        // get all children in the root node (i.e. all config properties)
        NodeList configNodes = rootElement.getChildNodes();
        // find the duration node and save it
        for(int i = 0; i < configNodes.getLength(); i++) {
            if(key.equals(configNodes.item(i).getNodeName())) {
                return configNodes.item(i).getTextContent();
            }
        }
        throw new Exception("[WARNING] " + key + " is not found in " + SNAPSHOT_CONFIG);
    }
    
    /**
     * Ensures that there is an existing XML file. Creates one if there
     * does not exist one already. 
     */
    public static void checkXMLExists() {
        File docFile = new File(pathToXML);
        // create an XML document if it does not exist
        if(!docFile.exists()) {
            Document doc = setUpDocument( createDocument() );
            saveDocument(doc, pathToXML);
        }
    }
    
    /**
     * Prepares the root element for a document.
     */
    public static Document setUpDocument(Document document) {
        // add <snapshot-config> tag as the root
        Element rootElement = document.createElement("snapshot-config");
        document.appendChild(rootElement);
        return document;
    }
    
    /**
     * Creates an instance of a Document and returns it
     */
    public static Document createDocument() {
        // get an instance of factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // get an instance of builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create an instance of DOM
            return db.newDocument();
        } catch(ParserConfigurationException pce) {
            log.error("Error while trying to instantiate DocumentBuilder", pce);
        }
        return null;
    }
    
    /**
     * Write the document object to the file location specified by
     * the path.
     */
    public static void saveDocument(Document document, String path) {
        try {
            //TODO GERONIMO-3719.  Hack to use xmlbeans to write out xml instead of sun specific classes.
            XmlObject xmlObject = XmlObject.Factory.parse(document.getDocumentElement());
            xmlObject.save(new File(path));

            // formatting the doc
//            OutputFormat format = new OutputFormat(document);
//            format.setIndenting(true);
            // generate a file output
//            XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(path)), format);
//            serializer.serialize(document);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Parses the XML document specified by the private member 'pathToXML'
     * and stores the information in the a Document object
     */
    public static Document openDocument() {
        // ensure that the XML file is there
        checkXMLExists();
        // get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // continue to attempt to parse
        while(true) {
            try {
                // Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();
                // parse using builder to get DOM representation of the XML file
                Document doc = db.parse(pathToXML);
                return doc;
            } catch(Exception e) {
                // Either this file is being read/written to by snapshot thread
                // or there is an UNKNOWN error
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Checks to see if the GERONIMO_HOME/var/monitoring/ directory was made.
     * If not, the method creates it.
     */
    public static void ensureMonitorDir() {
        final String pathToDir = 
            System.getProperty("org.apache.geronimo.home.dir") + "/var/monitoring/";
        File dir = new File(pathToDir);
        if(dir.exists() && dir.isDirectory()) {
            // all good
            return;
        } else {
            // make a directory
            if(dir.mkdir()) {
                // directory was successfully created
                log.info("/var/monitoring directory created.");
                return;
            } else {
                log.error("Could not make the directory " + pathToDir);
            }
        }
    }
}
