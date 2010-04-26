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

package org.apache.geronimo.shell.geronimo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.geronimo.main.Bootstrapper;
import org.apache.geronimo.main.Main;
import org.apache.geronimo.main.ServerInfo;
import org.apache.geronimo.main.Utils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
/**
 * @version $Rev$ $Date$
 */
public class StartServerstrapper extends Bootstrapper{
    
    private BundleContext bundleContext;
    //private org.apache.felix.karaf.main.Main karafMain;
    private boolean waitForStop = true;    
    private List<String> bundles;
    private int defaultStartLevel = 100;
    private boolean uniqueStorage = false;   
    private ServerInfo serverInfo;
    private String log4jFile;
    
    private Semaphore startSemaphore;
    private Throwable startException;
    private PrintStream out;

    public StartServerstrapper(BundleContext bundleContext,PrintStream out) {
        this.bundleContext=bundleContext;
        this.out=out;
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
    
    public void setLog4jConfigFile(String log4jFile) {
        this.log4jFile = log4jFile;
    }
    
    public int execute(Object opaque) {
        int exitCode;
        
        exitCode = launch();
        if (exitCode != 0) {
            return exitCode;
        }
        
        try {
            startBundles() ;
        } catch (BundleException e1) {
            out.println("Can't Start Bundle by BundleException"+e1.getMessage());
        } catch (IOException e1) {
            out.println("Can't Start Bundle by IOException"+e1.getMessage());
        }
        
        
        bundleContext.registerService(ServerInfo.class.getName(), serverInfo, null);
        /*
        if (this.bundles != null) {
            startSemaphore = new Semaphore(0);
            try {
                if (!startSemaphore.tryAcquire(60, TimeUnit.SECONDS)) {
                    return -1;
                }
            } catch (InterruptedException e) {
                return -1;
            }

            if (startException != null) {
                System.err.println("Error starting bundles: " + startException.getMessage());
                startException.printStackTrace();
                return -1;
            }
        }
        */
                
        Main geronimo_main = getMain();
        
        if (geronimo_main == null) {
            out.println("Main not found");
            stop(false);
            return -1;
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = geronimo_main.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            exitCode = geronimo_main.execute(opaque);
            stop(waitForStop);
        } catch (Throwable e) {
            out.println(e.getMessage());
            stop(false);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return exitCode;
    }

    public Main getMain() {
        ServiceTracker tracker = new ServiceTracker(bundleContext, Main.class.getName(), null);
        tracker.open();
        
        Main geronimoMain = null;
        try {
            geronimoMain = (Main) tracker.waitForService(1000 * 60);
            tracker.close();
        } catch (Exception e) {            
            out.println(e.getMessage());          
        }
        return geronimoMain;
    }
    

   
    public void stop(boolean await) {
        try {
            //TODO uninstall all the bundles
        } catch (Exception e) {
            out.println(e.getMessage());           
        } finally {
            if (uniqueStorage) {
                String dir = System.getProperty(Constants.FRAMEWORK_STORAGE);
                recursiveDelete(new File(dir));                
            }
        }
    }
                
    public void startLevelChanged(int startLevel) {
        if (startLevel == defaultStartLevel) {
            try {
                startBundles();
                startException = null;
            } catch (Throwable e) {
                startException = e;
            } finally {
                startSemaphore.release();
            }
        }        
    }
    protected BundleContext getBundleContext() {
        return bundleContext;
    }
    
    private String getMvnLocation(String[] parts) {
        return "mvn:" + parts[0] + "/" + parts[1] + "/" + parts[2] + "/" + parts[3]; 
    }
    
    private File getBundleLocation(String[] parts) {
        String group = parts[0].replace('.', '/').trim();
        String artifactId = parts[1].trim();
        String version = parts[2].trim();
        String type = parts[3].trim();
                        
        String defaultRepo = System.getProperty("karaf.default.repository");
        
        File repo = new File(getHome(), defaultRepo);
        
        File base = new File(repo, group + "/" + artifactId);
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
    
    private File findFile(File base, String name) {
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
    
    private String getStorageDirectory() throws IOException {
        File storage;
        if (uniqueStorage) {
            File var = new File(getServer(), "var");
            File tmpFile = File.createTempFile("appclient-", "", var);
            storage = new File(var, tmpFile.getName() + "-cache");
            tmpFile.delete();
        } else {
            storage = new File(getServer(), "var/cache");
        }
                
        storage.mkdirs();
        return storage.getAbsolutePath();
    }
    public void startBundles() throws BundleException, IOException {
        BundleContext context = getBundleContext();
        for (String location : this.bundles) {
            String[] parts = location.split("/");
            
            File fileLocation = getBundleLocation(parts);
            if (location == null) {
                out.println("Artifact " + location + " not found");
                continue;
            }
            parts[2] = fileLocation.getParentFile().getName();
            parts[3] = fileLocation.getName().substring(fileLocation.getName().lastIndexOf('.') + 1);
            
            String mvnLocation = getMvnLocation(parts);
            Bundle b = context.installBundle(mvnLocation, fileLocation.toURI().toURL().openStream());
            if (b != null) {
                b.start(Bundle.START_TRANSIENT);
            }
        }
    }
    
    private String getHome() {
        return this.serverInfo.getBase().getAbsolutePath();
    }
    
    private String getServer() {
        return this.serverInfo.getBaseServer().getAbsolutePath();
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
    public int launch() {      
        try {
            File geronimoHome = Utils.getGeronimoHome();
            File geronimoBase = Utils.getGeronimoBase(geronimoHome);
            File temporaryDir = Utils.getTempDirectory(geronimoBase);
            File log4jConfigFile = Utils.getLog4jConfigurationFile(geronimoBase, log4jFile);
            
            System.setProperty(Utils.HOME_DIR_SYS_PROP, 
                               geronimoHome.getAbsolutePath());
            
            System.setProperty(Utils.SERVER_DIR_SYS_PROP,
                                geronimoBase.getAbsolutePath());
            
            System.setProperty("java.io.tmpdir", 
                               temporaryDir.getAbsolutePath());
            
            if (log4jConfigFile != null) {
                System.setProperty("org.apache.geronimo.log4jservice.configuration",
                                   log4jConfigFile.getAbsolutePath());
            }
            
            this.serverInfo = new ServerInfo(geronimoHome, geronimoBase);
                        
            System.setProperty(Constants.FRAMEWORK_STORAGE, 
                               getStorageDirectory());
            
            System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, 
                               String.valueOf(defaultStartLevel));       
                      
            //System.setProperty(org.apache.felix.karaf.main.Main.PROPERTY_USE_LOCK, 
            //                   (uniqueStorage) ? "false" : "true");
           
            /*
            karafMain = new org.apache.felix.karaf.main.Main(null);
            
            try {           
                karafMain.launch();
            } catch (Exception e) {
                e.printStackTrace();           
            }
            
            return karafMain.getExitCode();*/
            
        } catch (IOException e) {
            out.println(e.getMessage());
            return -1;
        }
        
        
        return 0;
    }
}
