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
import java.util.List;
import java.util.Properties;

import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @version $Rev$ $Date$
 */
public class Bootstrapper extends FrameworkLauncher {
    
    private boolean waitForStop = true;    
    private List<String> bundles;

    public Bootstrapper() {
    }
    
    public void setWaitForStop(boolean waitForStop) {
        this.waitForStop = waitForStop;
    }
    
    public void setStartBundles(List<String> bundles) {
        this.bundles = bundles;
    }
        
    public int execute(Object opaque) {
        try {
            launch();
        } catch (Throwable e) {
            System.err.println("Error launching framework: " + e);
            destroy(false);
            return -1;
        }
                              
        Main geronimoMain = getMain();        
        if (geronimoMain == null) {
            System.err.println("Main not found");
            destroy(false);
            return -1;
        }
        
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = geronimoMain.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            int exitCode = geronimoMain.execute(opaque);
            if (exitCode == 0) {
                destroy(waitForStop);
            } else {
                destroy(false);
            }
            return exitCode;
        } catch (Throwable e) {
            System.err.println("Error in Main: " + e);
            destroy(false);
            return -1;
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

    public Main getMain() {
        ServiceTracker tracker = new ServiceTracker(getFramework().getBundleContext(), Main.class.getName(), null);
        tracker.open();        
        Main geronimoMain = null;
        try {
            return (Main) tracker.waitForService(1000 * 60);
        } catch (InterruptedException e) {            
            // ignore
        } finally {
            tracker.close();
        }
        return geronimoMain;
    }
                        
    @Override
    protected List<BundleInfo> loadStartupProperties(Properties startupProps, List<File> bundleDirs) {
        List<BundleInfo> startList = super.loadStartupProperties(startupProps, bundleDirs);
        
        for (String location : bundles) {
            String[] parts = location.split("/");
            
            File file = getBundleLocation(bundleDirs, parts);
            if (file == null) {
                System.err.println("Artifact " + location + " not found");
                continue;
            }
            
            parts[2] = file.getParentFile().getName();
            parts[3] = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            
            String mvnLocation = getMvnLocation(parts);
            
            BundleInfo info = new BundleInfo();
            info.location = file;
            info.mvnLocation = mvnLocation;
            info.startLevel = 60;
            
            startList.add(info);            
        }
        
        return startList;
    }
    
    private String getMvnLocation(String[] parts) {
        return "mvn:" + parts[0] + "/" + parts[1] + "/" + parts[2] + "/" + parts[3]; 
    }
    
    private static File getBundleLocation(List<File> bundleDirs, String[] parts) {
        for (File bundleDir : bundleDirs) {
            File file = getBundleLocation(bundleDir, parts);
            if (file != null) {
                return file;
            }
        }
        return null;
    }
    
    private static File getBundleLocation(File bundleDir, String[] parts) {
        String group = parts[0].replace('.', '/').trim();
        String artifactId = parts[1].trim();
        String version = parts[2].trim();
        String type = parts[3].trim();
                                        
        File base = new File(bundleDir, group + "/" + artifactId);
        if (base.exists()) {
            File versionFile = findFile(base, version);
            if (versionFile != null) {
                String artifactName = "";
                if (type.length() != 0) {                        
                    artifactName = artifactId + "-" + versionFile.getName() + "." + type;
                }
                return findFile(versionFile, artifactName);               
            }
        }
        return null;
    }
    
    private static File findFile(File base, String name) {
        File[] files = base.listFiles();
        if (name.length() == 0) {
            return (files.length > 0) ? files[0] : null;
        }
        for (File file : files) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
       
}
