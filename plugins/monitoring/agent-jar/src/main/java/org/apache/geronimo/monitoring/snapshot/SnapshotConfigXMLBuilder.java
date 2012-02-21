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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.monitoring.MonitorConstants;

/**
 * In charge of dealing with the XML processing of the snapshot's data.
 */
public class SnapshotConfigXMLBuilder {
    private static final Logger log = LoggerFactory.getLogger(SnapshotConfigXMLBuilder.class);
    
    private static final String pathToXML = 
        System.getProperty("org.apache.geronimo.server.dir") + "/var/monitoring/snapshot-config.xml";

    private static JAXBContext jc = null;

    static {
        try {
            ObjectFactory objFactory = new ObjectFactory();
            ClassLoader cl = objFactory.getClass().getClassLoader();
            jc = JAXBContext.newInstance("org.apache.geronimo.monitoring.snapshot", cl);
        } catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @return SnapshotConfig object which represents the XML document
     */
    private static SnapshotConfig getSnapshotConfig() {
        // ensure that there is a snapshot-config.xml at all times
        try {
            if(checkXMLExists()) {
                // unmarshall the xml document into SnapshotConfig object
                Unmarshaller m = jc.createUnmarshaller();
                return (SnapshotConfig)m.unmarshal( new File(pathToXML) );
            } else {
                SnapshotConfig sc = new SnapshotConfig();
                sc.setMbeans( new SnapshotConfig.Mbeans() );
                saveDocument(sc);
                return sc;
            }
        } catch(Exception e) {
            log.error(e.getMessage());
            return null;    // in the case of an error, return null
        }
    }
    
    /**
     * @return A list of all mbean names that have been previously saved.
     * These mbean names are those to keep track of for per snapshot.
     */
    public static ArrayList<String> getMBeanNames() {
        if(getSnapshotConfig().getMbeans() == null) {
            return new ArrayList<String>();
        } else {
            return (ArrayList<String>)getSnapshotConfig().getMbeans().getMbean();
        }
    }
    
    /**
     * Removes from the snapshot-config.xml a configuration element <mbean>
     * in order to persistently keep track of all user requested statistics.
     * If there does not exist an instance of the mbeanNAme, nothing will be done.
     */
    public static boolean removeMBeanName(String mbeanName) {
        ArrayList<String> mbeanNames = getMBeanNames();
        for(int i = 0 ; i < (int)mbeanNames.size(); i++) {
            if(mbeanNames.get(i).equals(mbeanName)) {
                // remove the mbean name by directly accessing it, because it is by reference
                SnapshotConfig sc = getSnapshotConfig();
                sc.getMbeans().getMbean().remove(i);
                // save the current state of the SnapshotConfig object
                saveDocument( sc );
                return true;
            }
        }
        return false;
    }

    /**
     * Adds to the snapshot-config.xml another configuration element <mbean>
     * in order to persistently keep track of all user requested statistics.
     * If there is a duplicate, nothing will be done.
     */
    public static boolean addMBeanName(String mbeanName) {
        // check to see if the mbean name already exists
        ArrayList<String> mbeanNames = getMBeanNames();
        for(int i = 0 ; i < (int)mbeanNames.size(); i++) {
            if(mbeanNames.get(i).equals(mbeanName)) {
                return false;   // nothing needs to be done if it is already there
            }
        }

        // insert the mbean name into the SnapshotConfig object
        SnapshotConfig sc = getSnapshotConfig();
        sc.getMbeans().getMbean().add(mbeanName);
        // write the object to XML
        saveDocument(sc);
        return true;
    }
    
    /**
     * Saves the duration of the snapshot as a configuration attribute
     * @param duration
     */
    public static void saveDuration(long duration) {
        SnapshotConfig sc = getSnapshotConfig();
        sc.setDuration(String.valueOf(duration));
        saveDocument(sc);
    }

    /**
     * Saves the retention of the snapshot as a configuration attribute
     * @param retention
     */
    public static void saveRetention(int retention) {
        SnapshotConfig sc = getSnapshotConfig();
        sc.setRetention(String.valueOf(retention));
        saveDocument(sc);
    }
    
    public static void saveStarted(boolean started) {
        SnapshotConfig sc = getSnapshotConfig();
        sc.setStarted(String.valueOf(started));
        saveDocument(sc);
    }
    
    /**
     * Returns the value of the configuration attribute, defined by the key
     * @param key
     * @return
     * @throws Exception
     */
    public static String getAttributeValue(String key) throws Exception {
        if(key.equals( MonitorConstants.DURATION )) {
            return getSnapshotConfig().getDuration();
        } else if(key.equals( MonitorConstants.RETENTION )) {
            return getSnapshotConfig().getRetention();
        } else if(key.equals( MonitorConstants.STARTED)) {
            return getSnapshotConfig().getStarted();
        } else {
            // Houston, we have a problem
            throw new Exception("[WARNING] Attribute: " + key + " is not valid.");
        }
    }
    
    /**
     * Ensures that there is an existing XML file. Creates one if there
     * does not exist one already. 
     */
    public static boolean checkXMLExists() {
        ensureMonitorDir();
        File docFile = new File(pathToXML);
        return docFile.exists();
    }
    
    
    /**
     * Write the XML document.
     */
    public static void saveDocument(SnapshotConfig sc) {
        try {
            Marshaller m = jc.createMarshaller();
            m.marshal(sc, new FileOutputStream( pathToXML ));
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    
    /**
     * Checks to see if the GERONIMO_SERVER/var/monitoring/ directory was made.
     * If not, the method creates it.
     */
    public static void ensureMonitorDir() {
        final String pathToDir = 
            System.getProperty("org.apache.geronimo.server.dir") + "/var/monitoring/";
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
