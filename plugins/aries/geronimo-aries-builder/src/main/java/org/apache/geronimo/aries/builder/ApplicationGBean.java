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
package org.apache.geronimo.aries.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.DeploymentContent;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.DeploymentMetadataFactory;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContextManager;
import org.apache.aries.application.management.AriesApplicationResolver;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ManagementException;
import org.apache.aries.application.management.AriesApplicationContext.ApplicationState;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean
public class ApplicationGBean implements GBeanLifecycle {
        
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGBean.class);
    
    private BundleContext bundleContext;
    private ApplicationInstaller installer;
    private GeronimoApplication application;
    private ApplicationState applicationState;
    private Set<Bundle> applicationBundles;
    
    public ApplicationGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                            @ParamReference(name="Installer") ApplicationInstaller installer) 
        throws Exception {
        this.bundleContext = bundleContext;
        this.installer = installer;
                
        // XXX: fix me
        DeploymentMetadataFactory deploymentFactory = getDeploymentMetadataFactory();
        ApplicationMetadataFactory applicationFactory = getApplicationMetadataFactory();
        
        this.application = new GeronimoApplication(bundleContext.getBundle(), applicationFactory, deploymentFactory);
        
        install();
        
        GeronimoApplicationContextManager applicationManager = 
            (GeronimoApplicationContextManager) getApplicationContextManager();
        applicationManager.registerApplicationContext(new GeronimoApplicationContext(this));
    }
    
    protected Bundle getBundle() {
        return bundleContext.getBundle();
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
        
    private AriesApplicationContextManager getApplicationContextManager() {
        ServiceReference ref = 
            bundleContext.getServiceReference(AriesApplicationContextManager.class.getName());
        if (ref != null) {
            return (AriesApplicationContextManager) bundleContext.getService(ref);
        } else {
            return null;
        }
    }
    
    private DeploymentMetadataFactory getDeploymentMetadataFactory() {
        ServiceReference ref = 
            bundleContext.getServiceReference(DeploymentMetadataFactory.class.getName());
        if (ref != null) {
            return (DeploymentMetadataFactory) bundleContext.getService(ref);
        } else {
            return null;
        }
    }
    
    private ApplicationMetadataFactory getApplicationMetadataFactory() {
        ServiceReference ref = 
            bundleContext.getServiceReference(ApplicationMetadataFactory.class.getName());
        if (ref != null) {
            return (ApplicationMetadataFactory) bundleContext.getService(ref);
        } else {
            return null;
        }
    }
    
    private void install() throws Exception {

        AriesApplicationResolver resolver = null;

        ServiceReference ref = bundleContext.getServiceReference(AriesApplicationResolver.class.getName());

        if (ref != null) {
            resolver = (AriesApplicationResolver) bundleContext.getService(ref);
        }

        if (resolver == null) {
            throw new ManagementException("AriesApplicationResolver service not found");
        }

        DeploymentMetadata meta = application.getDeploymentMetadata();
        
        List<DeploymentContent> bundlesToInstall = new ArrayList<DeploymentContent>();
        bundlesToInstall.addAll(meta.getApplicationDeploymentContents());
        bundlesToInstall.addAll(meta.getApplicationProvisionBundles());
        
        applicationBundles = new HashSet<Bundle>();
        try {
            for (DeploymentContent content : bundlesToInstall) {
                String bundleSymbolicName = content.getContentName();
                Version bundleVersion = content.getExactVersion();

                BundleInfo bundleInfo = null;

                for (BundleInfo info : application.getBundleInfo()) {
                    if (info.getSymbolicName().equals(bundleSymbolicName)
                        && info.getVersion().equals(bundleVersion)) {
                        bundleInfo = info;
                        break;
                    }
                }

                if (bundleInfo == null) {
                    // call out to the bundle repository.
                    bundleInfo = resolver.getBundleInfo(bundleSymbolicName, bundleVersion);
                }

                if (bundleInfo == null) {
                    throw new ManagementException("Cound not find bundles: " + bundleSymbolicName + "_" + bundleVersion);
                }

                Bundle bundle = bundleContext.installBundle(bundleInfo.getLocation());

                applicationBundles.add(bundle);
            }
        } catch (BundleException be) {
            for (Bundle bundle : applicationBundles) {
                bundle.uninstall();
            }

            applicationBundles.clear();

            throw be;
        } finally {
            if (resolver != null) {
                bundleContext.ungetService(ref);
            }
        }

        applicationState = ApplicationState.INSTALLED;
    }
    
    public void doStart() throws Exception {
        LOG.debug("Starting {}", application.getApplicationMetadata().getApplicationScope());
        
        applicationState = ApplicationState.STARTING;

        List<Bundle> bundlesWeStarted = new ArrayList<Bundle>();
        try {
            for (Bundle b : applicationBundles) {
                if (b.getState() != Bundle.ACTIVE) {
                    b.start(Bundle.START_ACTIVATION_POLICY);
                    bundlesWeStarted.add(b);
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

            applicationState = ApplicationState.INSTALLED;
            throw be;
        }
        applicationState = ApplicationState.ACTIVE;
    }    

    public void doStop() {
        LOG.debug("Stopping {}", application.getApplicationMetadata().getApplicationScope());
        
        for (Bundle bundle : applicationBundles) {
            try {
                bundle.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        applicationState = ApplicationState.RESOLVED;
    }

    public void doFail() {
        doStop();
    }
   
    protected void uninstall() {
        LOG.debug("Uninstalling {}", application.getApplicationMetadata().getApplicationScope());
        
        // uninstall application bundles
        for (Bundle bundle : applicationBundles) {
            try {
                bundle.uninstall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        applicationBundles.clear();

        // uninstall configuration
        installer.uninstall(application);
        
        // uninstall application bundle         
        try {
            bundleContext.getBundle().uninstall();
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        applicationState = ApplicationState.UNINSTALLED;
    }
}
