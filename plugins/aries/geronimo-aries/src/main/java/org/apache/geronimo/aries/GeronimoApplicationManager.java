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
package org.apache.geronimo.aries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.DeploymentMetadataFactory;
import org.apache.aries.application.filesystem.IDirectory;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationManager;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ManagementException;
import org.apache.aries.application.management.ResolveConstraint;
import org.apache.aries.application.management.ResolverException;
import org.apache.aries.application.utils.AppConstants;
import org.apache.aries.application.utils.management.SimpleBundleInfo;
import org.apache.aries.application.utils.manifest.BundleManifest;
import org.apache.aries.application.utils.manifest.ManifestDefaultsInjector;
import org.apache.aries.application.utils.manifest.ManifestProcessor;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
public class GeronimoApplicationManager {

    private static final Attributes.Name APPLICATION_CONTENT_NAME = new Attributes.Name(AppConstants.APPLICATION_CONTENT);
    
    private static final Logger LOG = LoggerFactory.getLogger(GeronimoApplicationManager.class);

    private BundleContext bundleContext;
    
    private ServiceTracker deploymentFactoryTracker;
    private ServiceTracker applicationFactoryTracker;
    private ServiceTracker applicationManagerTracker;
    
    public GeronimoApplicationManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.deploymentFactoryTracker = new ServiceTracker(bundleContext, DeploymentMetadataFactory.class.getName(), null);
        this.applicationFactoryTracker = new ServiceTracker(bundleContext, ApplicationMetadataFactory.class.getName(), null);
        this.applicationManagerTracker = new ServiceTracker(bundleContext, AriesApplicationManager.class.getName(), null);
    }
    
    public void doStart() throws Exception {
        deploymentFactoryTracker.open();
        applicationFactoryTracker.open();
        applicationManagerTracker.open();
    }
    
    public void doStop() {
        deploymentFactoryTracker.close();
        applicationFactoryTracker.close();
        applicationManagerTracker.close();
    }
    
    public DeploymentMetadataFactory getDeploymentMetadataFactory() throws ManagementException {
        DeploymentMetadataFactory service = (DeploymentMetadataFactory) deploymentFactoryTracker.getService();
        if (service == null) {
            throw new ManagementException(new ServiceException(DeploymentMetadataFactory.class.getName(), ServiceException.UNREGISTERED));           
        }
        return service;
    }
    
    public ApplicationMetadataFactory getApplicationMetadataFactory() throws ManagementException {
        ApplicationMetadataFactory service = (ApplicationMetadataFactory) applicationFactoryTracker.getService();
        if (service == null) {
            throw new ManagementException(new ServiceException(ApplicationMetadataFactory.class.getName(), ServiceException.UNREGISTERED));           
        }
        return service; 
    }
    
    private AriesApplicationManager getAriesApplicationManager() throws ManagementException {
        AriesApplicationManager service = (AriesApplicationManager) applicationManagerTracker.getService();
        if (service == null) {
            throw new ManagementException(new ServiceException(AriesApplicationManager.class.getName(), ServiceException.UNREGISTERED));           
        }
        return service; 
    }
    
    public AriesApplication createApplication(JarFile ebaJarFile) throws IOException, ManagementException {
        File ebaFile = new File(ebaJarFile.getName());
        
        if (!ebaFile.isDirectory()) {
            throw new IOException("Must be expanded Aries Application");
        }
        
        Manifest applicationMF = readApplicationManifest(ebaJarFile);
        
        Set<BundleInfo> bundleInfos = getBundleInfos(ebaFile);
        
        if (applicationMF.getMainAttributes().get(APPLICATION_CONTENT_NAME) == null) {
            String appContent = buildAppContent(bundleInfos);
            applicationMF.getMainAttributes().put(APPLICATION_CONTENT_NAME, appContent);
        }
        
        ManifestDefaultsInjector.updateManifest(applicationMF, ebaFile.getName(), ebaFile); 
        ApplicationMetadata applicationMetadata = getApplicationMetadataFactory().createApplicationMetadata(applicationMF);
        
        DeploymentMetadata deploymentMetadata = readDeploymentManifest(ebaJarFile);
        
        return new GeronimoApplication(applicationMetadata,  deploymentMetadata, bundleInfos);
    }
    
    public GeronimoApplication loadApplication(JarFile ebaJarFile) throws IOException, ManagementException {
        return (GeronimoApplication) createApplication(ebaJarFile);
    }
    
    public GeronimoApplication loadApplication(Bundle bundle) throws IOException, ManagementException {
        ApplicationMetadata applicationMetadata = null;
        URL applicationMF = bundle.getEntry(AppConstants.APPLICATION_MF);
        InputStream applicationMFStream = null;
        try {
            applicationMFStream = applicationMF.openStream();
            applicationMetadata = getApplicationMetadataFactory().parseApplicationMetadata(applicationMFStream);
        } finally {
            IOUtils.close(applicationMFStream);
        }
        
        DeploymentMetadata deploymentMetadata = null;
        URL deploymentMF = bundle.getEntry(AppConstants.DEPLOYMENT_MF);
        if (deploymentMF != null) {
            InputStream deploymentMFStream = null;
            try {
                deploymentMFStream = deploymentMF.openStream();
                deploymentMetadata = getDeploymentMetadataFactory().createDeploymentMetadata(deploymentMFStream);
            } finally {
                IOUtils.close(deploymentMFStream);
            }
        }
        
        Set<BundleInfo> bundleInfos = null;
        File bundleFile = BundleUtils.toFile(bundle);
        if (bundleFile != null && bundleFile.isDirectory()) {
            bundleInfos = getBundleInfos(bundleFile);
        } else {
            bundleInfos = getBundleInfos(bundle);
        }
        
        return new GeronimoApplication(applicationMetadata,  deploymentMetadata, bundleInfos);
    }

    public GeronimoApplication loadApplication(File ebaFile) throws IOException, ManagementException {
        JarFile jarFile = null;
        try {
            jarFile = JarUtils.createJarFile(ebaFile);
            return loadApplication(jarFile);
        } finally {
            JarUtils.close(jarFile);
        }
    }
    
    private Set<BundleInfo> getBundleInfos(File baseDir) throws IOException, ManagementException {
        ApplicationMetadataFactory applicationMetadataFactory = getApplicationMetadataFactory();
        Set<BundleInfo> bundleInfos = new HashSet<BundleInfo>();
        collectBundleInfos(baseDir, applicationMetadataFactory, bundleInfos);
        return bundleInfos;
    }
    
    private void collectBundleInfos(File baseDir, ApplicationMetadataFactory applicationMetadataFactory, Set<BundleInfo> bundleInfos) throws IOException, ManagementException {
        for (File file : baseDir.listFiles()) {
            if (file.isDirectory()) {
                if (file.getName().endsWith(".jar")) {
                    BundleManifest bm = fromBundle(file);
                    if (bm != null && bm.isValid()) {
                        bundleInfos.add(new SimpleBundleInfo(applicationMetadataFactory, bm, "reference:" + file.toURI().toString()));                
                    }
                } else {
                    collectBundleInfos(file, applicationMetadataFactory, bundleInfos);
                    continue;
                }
            } else {
                BundleManifest bm = fromBundle(file);
                if (bm != null && bm.isValid()) {
                    /*
                     * Pass file:// url instead of reference:file:// as bundle location to make sure
                     * Equinox has its own copy of the jar. That is, to prevent strange ZipErrors when
                     * application bundles are updated at runtime, specifically, when 
                     * ApplicationGBean.hotSwapApplicationContent() is called.
                     */
                    bundleInfos.add(new SimpleBundleInfo(applicationMetadataFactory, bm, file.toURI().toString()));                
                }
            }
        }
    }
    
    private BundleManifest fromBundle(File file) {
        if (file.isFile()) {
            // it's a jar file
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                return BundleManifest.fromBundle(in);
            } catch (IOException e) {
                LOG.debug("Error reading file: " + file, e);
            } finally {
                IOUtils.close(in);
            }
            return null;
        } else if (file.isDirectory()) {
            // it's a jar directory
            File manifestFile = new File(file, JarFile.MANIFEST_NAME);
            if (manifestFile.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(manifestFile);
                    return new BundleManifest(in);
                } catch (IOException e) {
                    LOG.debug("Error reading manifest file: " + file, e);
                } finally {
                    IOUtils.close(in);
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + file);
        }
    }
    
    private BundleManifest fromBundle(InputStream jarInputStream) {
        try {
            return BundleManifest.fromBundle(jarInputStream);
        } finally {
            IOUtils.close(jarInputStream);
        }
    }
    
    private Set<BundleInfo> getBundleInfos(Bundle bundle) throws IOException, ManagementException {
        ApplicationMetadataFactory applicationMetadataFactory = getApplicationMetadataFactory();
        Set<BundleInfo> bundleInfos = new HashSet<BundleInfo>();
        Enumeration<URL> e = bundle.findEntries("/", "*", true);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            if (url.getPath().endsWith("/")) {
                continue;
            }
            BundleManifest bm = fromBundle(url.openStream());
            if (bm != null && bm.isValid()) {
                bundleInfos.add(new SimpleBundleInfo(applicationMetadataFactory, bm, url.toExternalForm()));
            }
        }
        return bundleInfos;
    }
    
    private Manifest readApplicationManifest(JarFile ebaJarFile) throws IOException {
        Manifest manifest;
        ZipEntry entry = ebaJarFile.getEntry(AppConstants.APPLICATION_MF);
        if (entry != null) {
            InputStream applicationMFStream = null;
            try {
                applicationMFStream = ebaJarFile.getInputStream(entry);
                manifest = ManifestProcessor.parseManifest(applicationMFStream);
            } finally {
                IOUtils.close(applicationMFStream);
            }
        } else {
            manifest = new Manifest();
        }
        return manifest;
    }
    
    private DeploymentMetadata readDeploymentManifest(JarFile ebaJarFile) throws IOException, ManagementException {
        DeploymentMetadata deploymentMetadata = null;
        ZipEntry entry = ebaJarFile.getEntry(AppConstants.DEPLOYMENT_MF);
        if (entry != null) {
            InputStream deploymentMFStream = null;
            try {
                deploymentMFStream = ebaJarFile.getInputStream(entry);
                deploymentMetadata = getDeploymentMetadataFactory().createDeploymentMetadata(deploymentMFStream);
            } finally {
                IOUtils.close(deploymentMFStream);
            }
        }
        return deploymentMetadata;
    }
    
    private String buildAppContent(Set<BundleInfo> bundleInfos) {
        StringBuilder builder = new StringBuilder();
        Iterator<BundleInfo> iterator = bundleInfos.iterator();
        while (iterator.hasNext()) {
            BundleInfo info = iterator.next();
            builder.append(info.getSymbolicName());

            // bundle version is not a required manifest header
            if (info.getVersion() != null) {
                String version = info.getVersion().toString();
                builder.append(";version=\"[");
                builder.append(version);
                builder.append(',');
                builder.append(version);
                builder.append("]\"");
            }

            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
    
    public ApplicationMetadata getApplicationMetadata(JarFile ebaJarFile) throws IOException, ManagementException {
        Manifest applicationMF = readApplicationManifest(ebaJarFile);
        
        // this is to prevent unnecessary jar scanning by ManifestDefaultsInjector
        boolean dummyAppContent = false;
        if (applicationMF.getMainAttributes().get(APPLICATION_CONTENT_NAME) == null) {
            applicationMF.getMainAttributes().put(APPLICATION_CONTENT_NAME, "none");
            dummyAppContent = true;
        }
        
        File ebaFile = new File(ebaJarFile.getName()); 
        
        ManifestDefaultsInjector.updateManifest(applicationMF, ebaFile.getName(), ebaFile); 
        
        if (dummyAppContent) {
            applicationMF.getMainAttributes().remove(APPLICATION_CONTENT_NAME);
        }
        
        ApplicationMetadata applicationMetadata = getApplicationMetadataFactory().createApplicationMetadata(applicationMF);
        return applicationMetadata;
    }
    
    public AriesApplication resolve(AriesApplication app, ResolveConstraint... constraints) throws ResolverException {
        try {
            return getAriesApplicationManager().resolve(app, constraints);
        } catch (ManagementException e) {
            throw new ResolverException(e);
        }
    }
    
    public AriesApplication createApplication(IDirectory ebaFile) throws ManagementException {
        return getAriesApplicationManager().createApplication(ebaFile);
    }
}
