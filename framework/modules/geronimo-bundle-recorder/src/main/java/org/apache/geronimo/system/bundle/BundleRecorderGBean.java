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
package org.apache.geronimo.system.bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.main.ServerStatus;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean
public class BundleRecorderGBean implements BundleRecorder, ServerStatus {
    
    private static final Logger log = LoggerFactory.getLogger(BundleRecorderGBean.class);

    private BundleContext bundleContext;
    StartLevel startLevelService;
    int defaultBundleStartLevel;
        
    private final PluginInstallerGBean pluginInstaller;
    private final WritableListableRepository writeableRepo;
    
    private File startupFile;

    public BundleRecorderGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                               @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                               @ParamReference(name = "ServerInfo") final ServerInfo serverInfo,
                               @ParamReference(name = "PluginInstallerGBean")PluginInstallerGBean installer,
                               @ParamReference(name = "Repository", namingType = "Repository") WritableListableRepository repository) 
        throws DeploymentException, IOException {
        
        bundleContext = bundle.getBundleContext();
        startLevelService = getStartLevelService(bundleContext);
        defaultBundleStartLevel = startLevelService.getInitialBundleStartLevel();
        
        pluginInstaller = installer;
        writeableRepo = repository;
                
        startupFile = serverInfo.resolveServer("etc/installed-bundles.properties");
    }
    
    private StartLevel getStartLevelService(BundleContext bundleContext){
        ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
        return (StartLevel) bundleContext.getService(startLevelRef);
    }
               
    /**
     * install the bundle to framework
     * @param location
     * @param startLevel
     * @return the bundle object of the installed bundle. null if install failed.
     */
    private Bundle installBundle(String location, int startLevel) {
        try {
            // install
            Bundle installedBundle = bundleContext.installBundle(location);
            // set start level
            startLevelService.setBundleStartLevel(installedBundle, startLevel);

            return installedBundle;
        } catch (BundleException e) {
            log.error("Bundle installation failed: " + location);
        }
        
        return null;
    }
    
    private void startBundle(Bundle bundle) {
        if (BundleUtils.canStart(bundle)) {
            try {
                bundle.start(Bundle.START_TRANSIENT);
            } catch (BundleException e) {
                log.error("Bundle " + bundle.getBundleId() + " failed to start: " + e.getMessage());
            }          
        }
    }
    
    private void uninstallBundle(Bundle bundle) {
        try {
            bundle.uninstall();
        } catch (BundleException e) {
            log.error("Bundle " + bundle.getBundleId() + " failed to uninstall: " + e.getMessage());
        }  
    }
    
    @Override
    public long recordInstall(File bundleFile, String groupId, int startLevel) throws IOException{
        if (bundleFile == null || bundleFile.isDirectory()) {
            throw new IllegalArgumentException("The bundle File is not exist "+ bundleFile.getPath());
        }
        
        // 1. copy to repo. If the file is not an osgi bundle, pluginInstaller can convert it automatically.
        if (groupId == null || groupId.isEmpty()) groupId = Artifact.DEFAULT_GROUP_ID;
        Artifact artifact = pluginInstaller.installLibrary(bundleFile, groupId);
                
        // 2. install the bundle
        String bundleLocation = getMvnLocationFromArtifact(artifact);
        
        if (startLevel <= 0) {
            log.info("Invalid start level or no start level specified, use defalut bundle start level");
            startLevel = defaultBundleStartLevel;
        }
        
        Bundle bundle = installBundle(bundleLocation, startLevel);
        if (bundle == null) return -1;
        
        // 3. record in startup.properties
        String recordKey = artifact.toString();
        
        if (!startupFile.exists() || Utils.findLineByKeyword(startupFile, recordKey) == null) {
            Utils.appendLine(startupFile, recordKey+"="+String.valueOf(startLevel));
        } else {
            log.warn("The bundle has been recorded: " + recordKey);
        }
                    
        return bundle.getBundleId();
    }
    
    @Override
    public void eraseUninstall(long bundleId) throws IOException{
        
        // uninstall bundle
        Bundle bundle = bundleContext.getBundle(bundleId);
        if (bundle == null) {
            return;
        }
        
        uninstallBundle(bundle);
        
        String bundleLocation = bundle.getLocation();        
        Artifact artifact = getArtifactFromMvnLocation(bundleLocation);
        if (artifact == null) {
            return;
        }
        
        String recordKey = artifact.toString();
        if (Utils.findLineByKeyword(startupFile, recordKey) != null) {
            // erase from startup.properties
            Utils.deleteLineByKeyword(startupFile, recordKey);
            
            // del the bundle file in repo
            File target = writeableRepo.getLocation(artifact);
            File versionFolder = target.getParentFile();
            File artifactFolder = target.getParentFile().getParentFile();
            
            if (target!=null && target.exists())
                target.delete(); // guarantee the target must be deleted
            
            FileUtils.recursiveDelete(versionFolder); // try delete the version folder recursively
            
            Utils.regressiveDelete(artifactFolder); // try delete the parent folder if it is empty
        }                
    }
    
    private String getMvnLocationFromArtifact(Artifact artifact){
        if (artifact == null) return null;
        
        StringBuilder bundleLocation = new StringBuilder();
        bundleLocation.append("mvn:");
        bundleLocation.append(artifact.getGroupId()).append('/').append(artifact.getArtifactId()).append('/').append(artifact.getVersion());
        
        return bundleLocation.toString();
    }

    private Artifact getArtifactFromMvnLocation(String mvnLocation) {
        if (!mvnLocation.startsWith("mvn:")) return null;
        
        String artifactString = mvnLocation.substring(4);
        String[] parts = artifactString.split("/");
        
        if (parts==null || parts.length < 3) return null;
        
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        String type = "jar";
        
        return new Artifact(groupId, artifactId, version, type);        
    }
        
    @Override
    public long getBundleId(String symbolicName, String version) {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion().toString())){
                return bundle.getBundleId();
            }
        }
        return -1;
    }

    @Override
    public boolean isServerStarted() {
        return false;
    }

    @Override
    public void setServerStarted(boolean started) {
        if (!startupFile.exists()) {
            return;
        }
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(startupFile));
            String line = null;
            while (( line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                
                int pos = line.indexOf("=");
                if (pos != -1) {
                    String name = line.substring(0, pos).trim();
                    String startLevel = line.substring(pos + 1).trim();
                    
                    String mvnLocation = getMvnLocationFromArtifact(Artifact.create(name));
                    Bundle bundle = findBundle(mvnLocation);
                    if (bundle == null) {
                        bundle = installBundle(mvnLocation, Integer.parseInt(startLevel));
                        if (bundle == null) {
                            continue;
                        }
                    }
                    startBundle(bundle);                    
                }
            }
        } catch (Exception e) {
            log.error("Error reading file", e);
        } finally {
            IOUtils.close(reader);
        }
    }
    
    private Bundle findBundle(String location) {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (location.equals(bundle.getLocation())) {
                return bundle;
            }
        }
        return null;
    }
}
