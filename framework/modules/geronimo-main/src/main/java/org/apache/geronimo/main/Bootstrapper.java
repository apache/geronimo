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
package org.apache.geronimo.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @version $Rev$ $Date$
 */
public class Bootstrapper {

    private org.apache.felix.karaf.main.Main main;
    private boolean waitForStop = true;    
    private List<String> bundles;
    private int defaultStartLevel = 100;
    private boolean uniqueStorage = false;

    public Bootstrapper() {
        main = new org.apache.felix.karaf.main.Main(null);
    }
    
    public void setWaitForStop(boolean waitForStop) {
        this.waitForStop = waitForStop;
    }
    
    public void setStartBundles(List<String> bundles) {
        this.bundles = bundles;
    }
    
    public void setUniqueStorage(boolean uniqueStorage) {
        this.uniqueStorage = uniqueStorage;
    }
    
    public int execute(Object opaque) {
        int exitCode;
        
        exitCode = launch();
        if (exitCode != 0) {
            return exitCode;
        }
        
        if (bundles != null) {
            StartLevelListener listener = new StartLevelListener(this);
            listener.start();            
        }
        
        Main main = getMain();
        if (main == null) {
            System.err.println("Main not found");
            stop(false);
            return -1;
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = main.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            exitCode = main.execute(opaque);
            stop(waitForStop);
        } catch (Throwable e) {
            e.printStackTrace();
            stop(false);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return exitCode;
    }

    public Main getMain() {
        ServiceTracker tracker = new ServiceTracker(main.getFramework().getBundleContext(), Main.class.getName(), null);
        tracker.open();
        
        Main main = null;
        try {
            main = (Main) tracker.waitForService(1000 * 60);
            tracker.close();
        } catch (Exception e) {            
            e.printStackTrace();            
        }
        return main;
    }
    
    public int launch() {        
        try {
            System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, String.valueOf(defaultStartLevel));
            System.setProperty(Constants.FRAMEWORK_STORAGE, getStorageDirectory());
            
            main.launch();
        } catch (Exception e) {
            e.printStackTrace();           
        }
        return main.getExitCode();
    }
    
    public void stop(boolean await) {
        try {
            main.destroy(await);
        } catch (Exception e) {
            e.printStackTrace();           
        } finally {
            if (uniqueStorage) {
                String dir = System.getProperty(Constants.FRAMEWORK_STORAGE);
                recursiveDelete(new File(dir));                
            }
        }
    }
            
    protected BundleContext getBundleContext() {
        return main.getFramework().getBundleContext();
    }
            
    public void startLevelChanged(int startLevel) {
        if (startLevel == defaultStartLevel) {
            startBundles();
        }        
    }
    
    public void startBundles() {
        BundleContext context = getBundleContext();
        for (String location : bundles) {
            String mvnLocation = "mvn:" + location;
            File fileLocation = getBundleLocation(location);
            try {
                Bundle b = context.installBundle(mvnLocation, fileLocation.toURL().openStream());
                if (b != null) {
                    b.start(Bundle.START_TRANSIENT);
                }
            } catch (Exception ex) {
                System.err.println("Error starting: " + location + " " + fileLocation + " " + ex);
            }            
        }
    }
    
    private File getBundleLocation(String name) {
        String[] parts = name.split("/");
        String group = parts[0].replace('.', '/');
        String artifactId = parts[1];
        String version = parts[2];
        String type = parts[3];
        
        String fileName = group + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + type;
                
        String defaultRepo = System.getProperty(org.apache.felix.karaf.main.Main.DEFAULT_REPO);
        
        File repo = new File(getHome(), defaultRepo);
        File bundleLocation = new File(repo, fileName);
        
        return bundleLocation;
    }
    
    private String getStorageDirectory() throws IOException {
        File storage;
        if (uniqueStorage) {
            File var = new File(getHome(), "var");
            File tmpFile = File.createTempFile("appclient-", "", var);
            storage = new File(var, tmpFile.getName() + "-cache");
            tmpFile.delete();
        } else {
            storage = new File(getHome(), "var/cache");
        }
                
        storage.mkdirs();
        return storage.getAbsolutePath();
    }
    
    private String getHome() {
        return System.getProperty(org.apache.felix.karaf.main.Main.PROP_KARAF_HOME);
    }
    
    private static boolean recursiveDelete(File root) {
        if (root == null) {
            return true;
        }

        boolean ok = true;
        
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        ok = ok && recursiveDelete(file);
                    } else {
                        ok = ok && file.delete();
                    }
                }
            }
        }
        
        ok = ok && root.delete();
        
        return ok;
    }

}
