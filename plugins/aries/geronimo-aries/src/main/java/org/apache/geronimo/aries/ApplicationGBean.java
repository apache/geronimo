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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.DeploymentContent;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.DeploymentMetadataFactory;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContext.ApplicationState;
import org.apache.aries.application.management.AriesApplicationContextManager;
import org.apache.aries.application.management.AriesApplicationResolver;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ManagementException;
import org.apache.aries.application.management.ResolverException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean
public class ApplicationGBean implements GBeanLifecycle {
        
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGBean.class);
    
    private static final long applicationStartTimeout = getApplicationStartTimeout();
    
    private final Bundle bundle;
    private final ApplicationInstaller installer;
    private final Artifact configId;
    private final ApplicationUpdateHelper updateHelper;
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
        this.updateHelper = new ApplicationUpdateHelper(this);
                
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
    
    public long getApplicationContentBundleId(String symbolicName, String version) {
        Bundle bundle = findBundle(symbolicName, version);
        return (bundle == null) ? -1 : bundle.getBundleId();
    }
    
    public File getApplicationContentBundlePublishLocation(String symbolicName, String version) {
        Bundle bundle = findBundle(symbolicName, version);
        if (bundle == null) {
            return null;
        }
        File file = BundleUtils.toFile(bundle);
        return (file != null && file.isDirectory()) ? file : null;
    }

    private Bundle findBundle(String symbolicName, String version) {
        if (symbolicName != null) {
            for (Bundle content : applicationBundles) {
                if (symbolicName.equals(content.getSymbolicName())) {
                    if (version == null) {
                        // if no version specified, return first one that matches
                        return content;
                    } else if (version.equals(content.getVersion().toString())) {
                        // version specified - match exactly
                        return content;
                    }
                }
            }
        }
        return null;
    }
    
    private Bundle findBundle(long bundleId) {
        for (Bundle content : applicationBundles) {
            if (content.getBundleId() == bundleId) {
                return content;
            }
        }
        return null;
    }
    
    public String getApplicationContentBundleSymbolicName(long bundleId) {
        Bundle bundle = findBundle(bundleId);
        return (bundle != null) ? bundle.getSymbolicName() : null;
    }
    
    /*
     * Update contents of a single bundle within the OSGi application.
     * 
     * @param bundleId id of the bundle to update.
     * @param file new contents of the bundle.
     */
    public synchronized void updateApplicationContent(long bundleId, File file) throws Exception {
        Bundle targetBundle = findBundle(bundleId);
        if (targetBundle == null) {
            throw new IllegalArgumentException("Could not find bundle with id " + bundleId + " in the application");
        }
        waitForStart();
        updateHelper.updateBundle(targetBundle, file);
        waitForStart();
    }
    
    /*
     * Attempts to hot swap classes of a single bundle within the OSGi application.
     * 
     * @param bundleId id of the bundle to update.
     * @param changesFile file containing updated class files for the bundle.
     * @param updateArchive indicates if the application archive file should be updated with the changes. 
     */
    public synchronized boolean hotSwapApplicationContent(long bundleId, File changesFile, boolean updateArchive) throws Exception {
        Bundle targetBundle = findBundle(bundleId);
        if (targetBundle == null) {
            throw new IllegalArgumentException("Could not find bundle with id " + bundleId + " in the application");
        }
        waitForStart();
        return updateHelper.updateBundleClasses(targetBundle, changesFile, updateArchive);
    }
   
    /*
     * Update application archive with new bundle contents.
     * 
     * @param bundleId id of the bundle to update.
     * @param file file containing new contents of the bundle. The file can contain partial or full contents.
     * @param partial true if file contains partial contents. False, otherwise.
     */
    public synchronized boolean updateApplicationArchive(long bundleId, File file, boolean partial) throws Exception {
        Bundle targetBundle = findBundle(bundleId);
        if (targetBundle == null) {
            throw new IllegalArgumentException("Could not find bundle with id " + bundleId + " in the application");
        }
        try {
            if (partial) {
                updateHelper.updateEBAPartial(targetBundle, file);
            } else {
                updateHelper.updateEBA(targetBundle, file);
            }
            return true;
        } catch (Exception e) {
            LOG.warn("Error updating application archive with the new contents. " +
                     "Changes made might be gone next time the application or server is restarted.", e);
            return false;
        }
    }
        
    protected File getApplicationArchive() throws IOException {
        File ebaArchive = installer.getApplicationLocation(configId);
        if (ebaArchive == null || !ebaArchive.exists()) {
            throw new IOException("Cannot locate application archive for " + configId);
        }
        return ebaArchive;
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
    
    protected String getApplicationName() {
        return application.getApplicationMetadata().getApplicationScope();
    }
    
    private DeploymentMetadata getDeploymentMetadata(AriesApplicationResolver resolver, DeploymentMetadataFactory deploymentFactory) throws ResolverException {
        DeploymentMetadata meta = application.getDeploymentMetadata();
        if (meta == null) {
            // try to resolve the application
            LOG.debug("Resolving {} application.", getApplicationName());
            Set<BundleInfo> requiredBundles = resolver.resolve(application);
            meta = deploymentFactory.createDeploymentMetadata(application, requiredBundles);
            LOG.debug("Resolved application bundles: {} ", requiredBundles);
        } else {
            LOG.debug("Application {} is resolved.", getApplicationName());
        }
        return meta;
    }
    
    private void install(DeploymentMetadataFactory deploymentFactory) throws Exception {

        BundleContext bundleContext = bundle.getBundleContext();
        
        AriesApplicationResolver resolver = null;
        PackageAdmin packageAdmin = null;
        
        ServiceReference resolverRef = bundleContext.getServiceReference(AriesApplicationResolver.class.getName());
        ServiceReference packageAdminRef = bundleContext.getServiceReference(PackageAdmin.class.getName());
                              
        applicationBundles = new LinkedHashSet<Bundle>();
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
                        throw new ManagementException("Could not find bundle: " + bundleSymbolicName + "_" + bundleVersion);
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
    
    @SuppressWarnings("deprecation")
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
        LOG.debug("Starting {} application.", getApplicationName());
        
        applicationState = ApplicationState.STARTING;
        try {
            startApplicationBundles();
            applicationState = ApplicationState.ACTIVE;
            LOG.debug("Application {} started successfully.", getApplicationName());
        } catch (BundleException be) {
            applicationState = ApplicationState.INSTALLED;
            
            Exception rootException = be;
            String rootMessage = be.getMessage();
            
            // check for resolver errors
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                try {
                    ResolverErrorAnalyzer errorAnalyzer = new ResolverErrorAnalyzer(bundleContext);
                    String resolverErrors = errorAnalyzer.getErrorsAsString(applicationBundles);
                    if (resolverErrors != null) {
                        rootException = null;
                        rootMessage = resolverErrors;
                    }
                } catch (IllegalStateException e) {
                    // bundle is no loger valid, use the original exception
                }
            }

            String message = MessageFormat.format("Error starting {0} application. {1}", getApplicationName(), rootMessage);
            
            if (getFailOnStartError()) {
                throw new BundleException(message, rootException);
            } else {
                LOG.error(message, rootException);
            }
        }        
    }    
    
    private void startApplicationBundles() throws Exception {
        BundleContext context = bundle.getBundleContext();
        FrameworkWiring wiring = context.getBundle(0).adapt(FrameworkWiring.class);
        if (!wiring.resolveBundles(applicationBundles)) {
            throw new BundleException("One or more bundles in " + getApplicationName() + " application could not be resolved.");
        }
        
        LinkedList<Bundle> bundlesWeStarted = new LinkedList<Bundle>();
        try {
            applicationBundles = getSortedBundles();
            for (Bundle b : applicationBundles) {
                if (BundleUtils.canStart(b)) {
                    LOG.debug("Starting {} application bundle.", b);
                    b.start(Bundle.START_TRANSIENT);                 
                    bundlesWeStarted.addFirst(b); 
                }
            }
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
            throw be;
        }
    }

    /*
     * Sorts bundles in bundle dependency order (i.e. Import-Package, Require-Bundle order).
     */
    private LinkedHashSet<Bundle> getSortedBundles() {
        LinkedList<Bundle> stack = new LinkedList<Bundle>();
        LinkedHashSet<Bundle> orderedBundles = new LinkedHashSet<Bundle>(applicationBundles.size());
        for (Bundle bundle : applicationBundles) {
            sortDependentBundles(bundle, orderedBundles, stack);
        }
        return orderedBundles;
    }
    
    private void sortDependentBundles(Bundle bundle, LinkedHashSet<Bundle> sortedBundles, LinkedList<Bundle> stack) {
        if (sortedBundles.contains(bundle)) {
            return;
        }
        
        if (stack.contains(bundle)) {
            // dependency loop detected
            LOG.debug("Bundle dependency loop detected: {}", stack.subList(stack.indexOf(bundle), stack.size()));
            return;
        }
        stack.add(bundle);
        
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleWire> wires;
        wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleWire wire : wires) {
            Bundle wiredBundle = wire.getProviderWiring().getBundle();
            // examine wires to other application bundles only
            if (wiredBundle != bundle && applicationBundles.contains(wiredBundle)) {
                sortDependentBundles(wiredBundle, sortedBundles, stack);
            }
        }
        wires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        for (BundleWire wire : wires) {
            Bundle wiredBundle = wire.getProviderWiring().getBundle();
            // examine wires to other application bundles only
            if (wiredBundle != bundle && applicationBundles.contains(wiredBundle)) {
                sortDependentBundles(wiredBundle, sortedBundles, stack);
            }
        }
        
        stack.removeLast();
        
        sortedBundles.add(bundle);
    }
    
    private static long getApplicationStartTimeout() {
        String property = System.getProperty("org.apache.geronimo.aries.applicationStartTimeout", String.valueOf(5 * 60 * 1000));
        return Long.parseLong(property);
    }
    
    private void waitForStart() {
        waitForStart(applicationStartTimeout);
    }
    
    private void waitForStart(long timeout) {
        Set<Bundle> webBundles = new HashSet<Bundle>();
        for (Bundle b : applicationBundles) {
            if (b.getState() == Bundle.ACTIVE && WebApplicationUtils.isWebApplicationBundle(b)) {
                webBundles.add(b);               
            }
        }
        if (!webBundles.isEmpty()) {
            LOG.debug("Waiting {}ms for asynchronous processing for {} bundles to finish", timeout, webBundles);
            try {
                boolean completed = installer.getWebApplicationTracker().waitForBundles(webBundles, timeout);
                if (completed) {
                    LOG.debug("Asynchronous processing completed.");                    
                } else {
                    LOG.debug("Time out while waiting for asynchronous processing to finish.");
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }   
    }
    
    public void doStop() {
        LOG.debug("Stopping {} application.", getApplicationName());
        
        // stop bundles in reverse order
        LinkedList<Bundle> sortedList = new LinkedList<Bundle>();
        for (Bundle bundle : applicationBundles) {
            sortedList.addFirst(bundle);
        }
        
        for (Bundle bundle : sortedList) {
            try {
                LOG.debug("Stopping and uninstalling {} application bundle.", bundle);
                bundle.uninstall();
            } catch (Exception e) {
                LOG.error("Fail to uninstall", e);
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
        LOG.debug("Uninstalling {} application.", getApplicationName());

        try {
            installer.getConfigurationManager().unloadConfiguration(configId);
            installer.getConfigurationManager().uninstallConfiguration(configId);
        } catch (Exception e) {
            // ignore
        }
                     
        applicationState = ApplicationState.UNINSTALLED;
    }

}
