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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.DeploymentContent;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.DeploymentMetadataFactory;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContextManager;
import org.apache.aries.application.management.AriesApplicationResolver;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ManagementException;
import org.apache.aries.application.management.ResolverException;
import org.apache.aries.application.management.AriesApplicationContext.ApplicationState;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean
public class ApplicationGBean implements GBeanLifecycle {
        
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGBean.class);
    
    private final Bundle bundle;
    private final ApplicationInstaller installer;
    private final Artifact configId;
    private GeronimoApplication application;
    private ApplicationState applicationState;
    private Set<Bundle> applicationBundles;
    
    public ApplicationGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                            @ParamAttribute(name="configId") Artifact configId, 
                            @ParamReference(name="Installer") ApplicationInstaller installer) 
        throws Exception {
        this.bundle = bundle;
        this.installer = installer;
        this.configId = configId;
                
        BundleContext bundleContext = bundle.getBundleContext();

        DeploymentMetadataFactory deploymentFactory = null;
        ApplicationMetadataFactory applicationFactory  = null;
        
        ServiceReference deploymentFactoryReference = 
            bundleContext.getServiceReference(DeploymentMetadataFactory.class.getName());
        ServiceReference applicationFactoryReference =
            bundleContext.getServiceReference(ApplicationMetadataFactory.class.getName());
        
        try {
            deploymentFactory = getService(deploymentFactoryReference, DeploymentMetadataFactory.class);
            applicationFactory = getService(applicationFactoryReference, ApplicationMetadataFactory.class);
        
            this.application = new GeronimoApplication(bundle, applicationFactory, deploymentFactory);
            
            install(deploymentFactory);
        } finally {
            if (deploymentFactory != null) {
                bundleContext.ungetService(deploymentFactoryReference);
            }
            if (applicationFactory != null) {
                bundleContext.ungetService(applicationFactoryReference);
            }
        }
                        
        ServiceReference applicationManagerReference = 
            bundleContext.getServiceReference(AriesApplicationContextManager.class.getName());
        
        GeronimoApplicationContextManager applicationManager = 
            getService(applicationManagerReference, GeronimoApplicationContextManager.class);
        try {
            applicationManager.registerApplicationContext(new GeronimoApplicationContext(this));
        } finally {
            bundleContext.ungetService(applicationManagerReference);
        }
    }

    public long[] getApplicationContentBundleIds() {
        long[] ids = new long[applicationBundles.size()];
        int i = 0;
        for (Bundle content : applicationBundles) {
            ids[i++] = content.getBundleId();
        }
        return ids;
    }

    private Bundle getBundle(long bundleId) {
        for (Bundle content : applicationBundles) {
            if (content.getBundleId() == bundleId) {
                return content;
            }
        }
        return null;
    }
    
    public String getApplicationContentBundleSymbolicName(long bundleId) {
        Bundle bundle = getBundle(bundleId);
        return (bundle != null) ? bundle.getSymbolicName() : null;
    }

    public synchronized void updateApplicationContent(long bundleId, File bundleFile) throws Exception {
        Bundle targetBundle = getBundle(bundleId);

        if (targetBundle == null) {
            throw new IllegalArgumentException("Could not find bundle with id " + bundleId + " in the application");
        }

        String applicationName = application.getApplicationMetadata().getApplicationScope();
        String bundleName = targetBundle.getSymbolicName();
        
        LOG.info("Updating {} bundle in {} application", bundleName, applicationName);
                
        BundleContext context = bundle.getBundleContext();

        ServiceReference reference = null;
        RefreshListener refreshListener = null;
        try {
            // stop the bundle
            targetBundle.stop();

            // update the bundle
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(bundleFile);
                targetBundle.update(fi);
            } finally {
                IOUtils.close(fi);
            }

            reference = context.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) context.getService(reference);
            
            Bundle[] bundles = new Bundle [] { targetBundle };
            // resolve the bundle
            if (!packageAdmin.resolveBundles(bundles)) {
                throw new BundleException("Updated " + bundleName  + " bundle cannot be resolved");
            }
            
            Set<Bundle> dependents = new HashSet<Bundle>();
            collectDependentBundles(packageAdmin, dependents, targetBundle);
            if (!dependents.isEmpty()) {
                String bundleListString = bundleCollectionToString(dependents);
                LOG.info("Update of {} bundle will cause the following bundles to be refreshed: {}", bundleName, bundleListString);
            }
            
            // install listener for package refresh
            refreshListener = new RefreshListener();
            context.addFrameworkListener(refreshListener);

            // refresh the bundle - this happens asynchronously
            packageAdmin.refreshPackages(bundles);

            // update application archive
            try {
                updateArchive(targetBundle, bundleFile);
            } catch (Exception e) {
                LOG.warn("Error updating application archive with the new contents. " +
                         "Changes made might be gone next time the application or server is restarted.", e.getMessage());
            }

            // wait for package refresh to finish
            refreshListener.waitForRefresh(10 * 1000);

            // start the bundle
            if (BundleUtils.canStart(targetBundle)) {
                targetBundle.start(Bundle.START_TRANSIENT);
            }
            
            
            LOG.info("Bundle {} was successfully updated in {} application", bundleName, applicationName);
            
        } catch (Exception e) {
            LOG.error("Error updating " + bundleName + " bundle in " + applicationName + " application", e);
            throw new Exception("Error updating application: " + e.getMessage());
        } finally {
            if (refreshListener != null) {
                context.removeFrameworkListener(refreshListener);
            }
            if (reference != null) {
                context.ungetService(reference);
            }
        }
    }
    
    private class RefreshListener implements FrameworkListener {

        public CountDownLatch latch = new CountDownLatch(1);

        public void frameworkEvent(FrameworkEvent event) {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                latch.countDown();
            }
        }

        public void waitForRefresh(int timeout) {
            try {
                latch.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void collectDependentBundles(PackageAdmin packageAdmin, Set<Bundle> dependents, Bundle bundle) {
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
        if (exportedPackages != null) {
            for (ExportedPackage exportedPackage : exportedPackages) {
                Bundle[] importingBundles = exportedPackage.getImportingBundles();
                if (importingBundles != null) {
                    for (Bundle importingBundle : importingBundles) {
                        if (!dependents.contains(importingBundle)) {
                            dependents.add(importingBundle);
                            collectDependentBundles(packageAdmin, dependents, importingBundle);
                        }
                    }
                }
            }
        }
    }
    
    private static String bundleCollectionToString(Collection<Bundle> bundles) {
        StringBuilder builder = new StringBuilder();
        Iterator<Bundle> iterator = bundles.iterator();
        while(iterator.hasNext()) {
            Bundle bundle = iterator.next();
            builder.append(bundle.getSymbolicName());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
       return builder.toString();
    }
    
    private void updateArchive(Bundle bundle, File bundleFile) throws IOException {
        File ebaArchive = installer.getApplicationLocation(configId);
        if (ebaArchive == null || !ebaArchive.exists()) {
            throw new IOException("Cannot locate application archive for " + configId);
        }

        File newEbaArchive = new File(ebaArchive.getAbsoluteFile() + ".new");

        URI bundleLocation = URI.create(bundle.getLocation());
        String bundleNameInApp = bundleLocation.getPath();
        if (bundleNameInApp.startsWith("/")) {
            bundleNameInApp = bundleNameInApp.substring(1);
        }

        LOG.debug("Updating {} application archive with new contents for {}", ebaArchive, bundleNameInApp);

        ZipFile oldZipFile = null;
        ZipOutputStream newZipFile = null;
        try {
            newZipFile = new ZipOutputStream(new FileOutputStream(newEbaArchive));
            oldZipFile = new ZipFile(ebaArchive);
            Enumeration<? extends ZipEntry> entries = oldZipFile.entries();
            byte[] buffer = new byte[4096];
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                InputStream in = null;
                if (entry.getName().equals(bundleNameInApp)) {
                    in = new FileInputStream(bundleFile);
                    LOG.debug("Updating contents of {} with {}", bundleNameInApp, bundleFile.getAbsolutePath());
                } else {
                    in = oldZipFile.getInputStream(entry);
                }
                try {
                    newZipFile.putNextEntry(new ZipEntry(entry.getName()));
                    try {
                        int count;
                        while ((count = in.read(buffer)) > 0) {
                            newZipFile.write(buffer, 0, count);
                        }
                    } finally {
                        newZipFile.closeEntry();
                    }
                } finally {
                    IOUtils.close(in);
                }
            }
        } catch (IOException e) {
            LOG.debug("Error updating application archive", e);
        } finally {
            if (oldZipFile != null) {
                try {
                    oldZipFile.close();
                } catch (IOException ignore) {
                }
            }
            IOUtils.close(newZipFile);
        }

        if (ebaArchive.delete()) {
            if (!newEbaArchive.renameTo(ebaArchive)) {
                throw new IOException("Error renaming application archive");
            } else {
                LOG.debug("Application archive was successfully updated.");
            }
        } else {
            throw new IOException("Error deleting existing application archive");
        }
    }

    protected Bundle getBundle() {
        return bundle;
    }
    
    protected AriesApplication getAriesApplication() {
        return application;
    }
    
    protected Set<Bundle> getApplicationContent() {
        return new HashSet<Bundle>(applicationBundles);
    }

    protected ApplicationState getApplicationState() {
        return applicationState;
    }
    
    private DeploymentMetadata getDeploymentMetadata(AriesApplicationResolver resolver, DeploymentMetadataFactory deploymentFactory) throws ResolverException {
        DeploymentMetadata meta = application.getDeploymentMetadata();
        if (meta == null) {
            // try to resolve the application
            LOG.debug("Resolving {} application.", application.getApplicationMetadata().getApplicationScope());
            Set<BundleInfo> requiredBundles = resolver.resolve(application);
            meta = deploymentFactory.createDeploymentMetadata(application, requiredBundles);
            LOG.debug("Resolved application bundles: {} ", requiredBundles);
        } else {
            LOG.debug("Application {} is resolved.", application.getApplicationMetadata().getApplicationScope());
        }
        return meta;
    }
    
    private void install(DeploymentMetadataFactory deploymentFactory) throws Exception {

        BundleContext bundleContext = bundle.getBundleContext();
        
        AriesApplicationResolver resolver = null;
        PackageAdmin packageAdmin = null;
        
        ServiceReference resolverRef = bundleContext.getServiceReference(AriesApplicationResolver.class.getName());
        ServiceReference packageAdminRef = bundleContext.getServiceReference(PackageAdmin.class.getName());
                              
        applicationBundles = new HashSet<Bundle>();
        try {
            resolver = getService(resolverRef, AriesApplicationResolver.class);
            
            DeploymentMetadata meta = getDeploymentMetadata(resolver, deploymentFactory);
            
            List<DeploymentContent> deploymentContentsBundles = meta.getApplicationDeploymentContents();
            List<DeploymentContent> provisionBundles = meta.getApplicationProvisionBundles();
            
            List<DeploymentContent> bundlesToInstall = new ArrayList<DeploymentContent>();
            bundlesToInstall.addAll(deploymentContentsBundles);
            bundlesToInstall.addAll(provisionBundles);
            
            packageAdmin = getService(packageAdminRef, PackageAdmin.class);
            
            for (DeploymentContent content : bundlesToInstall) {
                String bundleSymbolicName = content.getContentName();
                Version bundleVersion = content.getExactVersion();

                // Step 1: See if bundle is already installed in the framework
                Bundle contentBundle = findBundleInFramework(packageAdmin, bundleSymbolicName, bundleVersion);
                if (contentBundle != null) {
                    // If the contentBundle has been already installed, and the contentBundle is deployment content bundle, it will be added to our application Bundles.
                    // That is, an installed provision bundle is not considered as an application bundle.
                    for (DeploymentContent deploymentContent : deploymentContentsBundles) {
                        if (deploymentContent.getContentName().equals(bundleSymbolicName) && deploymentContent.getExactVersion().equals(bundleVersion)){
                            applicationBundles.add(contentBundle);
                        }
                    }
                    
                } else {
                    // Step 2: See if the bundle is included in the application
                    BundleInfo bundleInfo = findBundleInfoInApplication(bundleSymbolicName, bundleVersion);
                    if (bundleInfo == null) {
                        // Step 3: Lookup bundle location using the resolver
                        bundleInfo = findBundleInfoUsingResolver(resolver, bundleSymbolicName, bundleVersion);
                    }
                    
                    if (bundleInfo == null) {
                        throw new ManagementException("Cound not find bundles: " + bundleSymbolicName + "_" + bundleVersion);
                    }
                        
                    contentBundle = bundleContext.installBundle(bundleInfo.getLocation());
                    applicationBundles.add(contentBundle);

                }
            }
        } catch (BundleException be) {
            for (Bundle bundle : applicationBundles) {
                bundle.uninstall();
            }

            applicationBundles.clear();

            throw be;
        } finally {
            if (resolver != null) {
                bundleContext.ungetService(resolverRef);
            }
            if (packageAdmin != null) {
                bundleContext.ungetService(packageAdminRef);
            }
        }

        applicationState = ApplicationState.INSTALLED;
    }
    
    private Bundle findBundleInFramework(PackageAdmin admin, String symbolicName, Version version) {
        String exactVersion = "[" + version + "," + version + "]";
        Bundle[] bundles = admin.getBundles(symbolicName, exactVersion);
        if (bundles != null && bundles.length == 1) {
            return bundles[0];
        } else {
            return null;
        }
    }
    
    private BundleInfo findBundleInfoInApplication(String symbolicName, Version version) {
        for (BundleInfo info : application.getBundleInfo()) {
            if (info.getSymbolicName().equals(symbolicName)
                && info.getVersion().equals(version)) {
                return info;
            }
        }
        return null;
    }
    
    private BundleInfo findBundleInfoUsingResolver(AriesApplicationResolver resolver, String symbolicName, Version version) {
        return resolver.getBundleInfo(symbolicName, version);
    }
    
    private <T> T getService(ServiceReference ref, Class<T> type) throws ManagementException {
        Object service = null;
        if (ref != null) {
            service = bundle.getBundleContext().getService(ref);
        }
        
        if (service == null) {
            throw new ManagementException(new ServiceException(type.getName(), ServiceException.UNREGISTERED));
        }
        
        return type.cast(service);
    }
    
    private static boolean getFailOnStartError() {
        String property = System.getProperty("org.apache.geronimo.aries.failApplicationOnStartError", "false");
        return Boolean.parseBoolean(property);
    }
    
    public void doStart() throws Exception {
        LOG.debug("Starting {} application.", application.getApplicationMetadata().getApplicationScope());
        
        applicationState = ApplicationState.STARTING;

        List<Bundle> bundlesWeStarted = new ArrayList<Bundle>();
        Bundle currentBundle = null;
        try {
            for (Bundle b : applicationBundles) {
                currentBundle = b;
                if (BundleUtils.canStart(b)) {
                    LOG.debug("Starting {} application bundle.", b);
                    b.start(Bundle.START_TRANSIENT);
                    bundlesWeStarted.add(b);
                }
            }
            applicationState = ApplicationState.ACTIVE;
            LOG.debug("Application {} started successfully.", application.getApplicationMetadata().getApplicationScope());
        } catch (BundleException be) {
            for (Bundle b : bundlesWeStarted) {
                try {
                    b.stop();
                } catch (BundleException be2) {
                    // we are doing tidyup here, so we don't want to replace the
                    // bundle exception
                    // that occurred during start with one from stop. We also
                    // want to try to stop
                    // all the bundles we started even if some bundles wouldn't
                    // stop.
                }
            }

            applicationState = ApplicationState.INSTALLED;
            if (getFailOnStartError()) {
                throw be;
            } else {
                String message = MessageFormat.format("Error starting {0} application. Bundle {1} failed to start.", 
                                                      application.getApplicationMetadata().getApplicationScope(), 
                                                      currentBundle);
                LOG.error(message, be);
            }
        }        
    }    

    public void doStop() {
        LOG.debug("Stopping {} application.", application.getApplicationMetadata().getApplicationScope());
        
        for (Bundle bundle : applicationBundles) {
            try {
                bundle.uninstall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        applicationBundles.clear();

        applicationState = ApplicationState.RESOLVED;
    }

    public void doFail() {
        doStop();
    }
   
    protected void applicationStart() throws BundleException {
        try {
            installer.getConfigurationManager().loadConfiguration(configId);
            installer.getConfigurationManager().startConfiguration(configId);
        } catch (Exception e) {
            throw new BundleException("Failed to start application", e);            
        }
    }
    
    protected void applicationStop() throws BundleException {
        try {
            installer.getConfigurationManager().unloadConfiguration(configId);
        } catch (Exception e) {
            throw new BundleException("Failed to start application", e);            
        }
    }
    
    protected void applicationUninstall() {
        LOG.debug("Uninstalling {} application.", application.getApplicationMetadata().getApplicationScope());

        try {
            installer.getConfigurationManager().unloadConfiguration(configId);
            installer.getConfigurationManager().uninstallConfiguration(configId);
        } catch (Exception e) {
            // ignore
        }
                     
        applicationState = ApplicationState.UNINSTALLED;
    }

}
