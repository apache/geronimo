/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.web.extender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extender bundle to manage deployment of Web
 * Application Bundles (WABs) to the RFC 66 web container.
 * 
 * @version $Rev$, $Date$
 */
public class WebContainerExtender implements GBeanLifecycle {
    // the header that identifies a bundle as being a WAB
    public final static String WEB_CONTEXT_PATH_HEADER = "Web-ContextPath";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebContainerExtender.class);

    private final Kernel kernel;
    private final BundleContext bundleContext;
    private final Environment defaultEnvironment;
    private final AbstractNameQuery serverName;
    private final AbstractNameQuery transactionManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery corbaGBeanObjectName;
    private final AbstractNameQuery globalContextAbstractName;
    private final SingleElementCollection<ModuleBuilder> webConfigBuilder;
    private final Collection<? extends Repository> repositories;
    private final ConfigurationManager configurationManager;
    
    private boolean stopped;
    private Map<String, WebApplications> contextPathMap;
    private WebContainerEventDispatcher eventDispatcher;
    private BundleTracker bt;
    private ExecutorService executor;
    
    public WebContainerExtender(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                @ParamAttribute(name = "transactionManagerAbstractName") AbstractNameQuery transactionManagerAbstractName,
                                @ParamAttribute(name = "connectionTrackerAbstractName") AbstractNameQuery connectionTrackerAbstractName,
                                @ParamAttribute(name = "corbaGBeanAbstractName") AbstractNameQuery corbaGBeanAbstractName,
                                @ParamAttribute(name = "globalContextAbstractName") AbstractNameQuery globalContextAbstractName,
                                @ParamAttribute(name = "serverName") AbstractNameQuery serverName,
                                @ParamReference(name = "Repositories", namingType = "Repository") Collection<? extends Repository> repositories,
                                @ParamReference(name = "WebModuleBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> webModuleBuilders,
                                @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws Exception {
        this.defaultEnvironment = defaultEnvironment;
        this.transactionManagerObjectName = transactionManagerAbstractName;
        this.connectionTrackerObjectName = connectionTrackerAbstractName;
        this.corbaGBeanObjectName = corbaGBeanAbstractName;
        this.globalContextAbstractName = globalContextAbstractName;
        this.serverName = serverName;
        this.repositories = repositories;
        this.webConfigBuilder = new SingleElementCollection<ModuleBuilder>(webModuleBuilders);
        this.bundleContext = bundleContext;
        this.kernel = kernel;   
        this.configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
    }

    AbstractNameQuery getServerName() {
        return serverName;
    }

    AbstractNameQuery getTransactionManagerObjectName() {
        return transactionManagerObjectName;
    }

    AbstractNameQuery getConnectionTrackerObjectName() {
        return connectionTrackerObjectName;
    }

    AbstractNameQuery getCorbaGBeanObjectName() {
        return corbaGBeanObjectName;
    }

    AbstractNameQuery getGlobalContextAbstractName() {
        return globalContextAbstractName;
    }
    
    Collection<? extends Repository> getRepositories() {
        return repositories;
    }
    
    Kernel getKernel() {
        return kernel;
    }
    
    ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    ModuleBuilder getWebModuleBuilder() {
        return webConfigBuilder.getElement();
    }
    
    /**
     * Activate the bundle and initialize the extender instance.
     *
     * @param context The BundleContext for our bundle.
     */
    public void start(BundleContext context) {
        LOGGER.debug("Starting web container extender...");

        stopped = false;
        executor = Executors.newFixedThreadPool(3);
        contextPathMap = Collections.synchronizedMap(new HashMap<String, WebApplications>());  
        eventDispatcher = new WebContainerEventDispatcher(context);
        bt = new BundleTracker(context, Bundle.STARTING | Bundle.ACTIVE, new WebBundleTrackerCustomizer());
        bt.open();
        
        LOGGER.debug("Web container extender started");
    }

    public ExecutorService getExecutorService() {
        return executor;
    }
    
    /**
     * Get the event dispatcher instance associated with this
     * application.
     *
     * @return The configured event dispatcher information.
     */
    public WebContainerEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }   
    
    private class WebBundleTrackerCustomizer implements BundleTrackerCustomizer {

        public Object addingBundle(Bundle bundle, BundleEvent event) {
            if (bundle.getState() == Bundle.ACTIVE) {
                return deploy(bundle);
            } else if (bundle.getState() == Bundle.STARTING) {
                String activationPolicyHeader = (String) bundle.getHeaders().get(Constants.BUNDLE_ACTIVATIONPOLICY);
                if (activationPolicyHeader != null && activationPolicyHeader.startsWith(Constants.ACTIVATION_LAZY)) {
                    return deploy(bundle);
                }
            }
            return null;
        }

        public void modifiedBundle(Bundle bundle, BundleEvent event, Object arg) {
        }

        public void removedBundle(Bundle bundle, BundleEvent event, Object arg) {
            undeploy((WebApplication) arg);
        }
        
    }

    /**
     * Shutdown the extender bundle at termination time.
     *
     * @param context Our BundleContext.
     */
    public void stop(BundleContext context) {
        LOGGER.debug("Stopping web container extender...");
        
        stopped = true;
        if (bt != null) {
            bt.close();
        }
        eventDispatcher.destroy();
        executor.shutdown();
        
        LOGGER.debug("Web container extender stopped");
    }

    /**
     * Destroy a web application deployment, either as a result
     * of the host bundle getting started, the extended getting stopped,
     * or the hosting Web Container service going away.
     *
     * @param wab    The deployed application.
     */
    private void undeploy(WebApplication wab) {
        WebApplications webApplications = contextPathMap.get(wab.getContextPath());
        webApplications.remove(wab);
    }

    /**
     * Check a started bundle to detect if this bundle
     * should be handled by this extender instance.
     *
     * @param bundle The source bundle.
     */
    private WebApplication deploy(Bundle bundle) {
        LOGGER.debug("Scanning bundle {} for WAB application", bundle.getSymbolicName());
        String contextPath = (String) bundle.getHeaders().get(WEB_CONTEXT_PATH_HEADER);
        // a WAB MUST have the Web-ContextPath header or it must be ignored by the extender.
        if (contextPath == null) {
            LOGGER.debug("No web container application found in bundle {}", bundle.getSymbolicName());
            return null;
        }
        LOGGER.debug("Found web container application in bundle {} with context path: {}", bundle.getSymbolicName(), contextPath);
        WebApplications webApplications = getWebApplications(contextPath);
        WebApplication webApp = new WebApplication(this, bundle, contextPath);
        Collection<Long> collisions = webApplications.register(webApp);
        if (!collisions.isEmpty()) {
            eventDispatcher.deploying(bundle, contextPath);
            eventDispatcher.collision(bundle, contextPath, collisions);
            LOGGER.warn("WAB {} cannot be deployed. WAB {} is already deployed with {} Context-Path.", 
                        new Object[] {bundle, webApplications.getDeployed().getBundle(), contextPath});
        }
        return webApp;
    }
        
    private WebApplications getWebApplications(String contextPath) {
        synchronized (contextPathMap) {
            WebApplications webApplications = contextPathMap.get(contextPath);
            if (webApplications == null) {
                webApplications = new WebApplications(contextPath);
                contextPathMap.put(contextPath, webApplications);
            }
            return webApplications;            
        }
    }
 
    protected void unregisterWebApplication(WebApplication wab) {
        WebApplications webApplications = contextPathMap.get(wab.getContextPath());
        webApplications.unregister(wab, !stopped);
    }

    public void doFail() {
        stop(bundleContext);  
    }

    public void doStart() throws Exception {
        start(bundleContext);
    }

    public void doStop() throws Exception {
        stop(bundleContext);        
    }
    
    private static class WebApplications {
        
        private String contextPath;
        private WebApplication deployed;
        private List<WebApplication> waiting;
        
        public WebApplications(String contextPath) {
            this.contextPath = contextPath;
            this.waiting = new LinkedList<WebApplication>();
        }
        
        public synchronized Collection<Long> register(WebApplication webApp) {
            if (deployed == null) {
                deployed = webApp;
                deployed.schedule();
                return Collections.emptyList();
            } else {
                waiting.add(webApp);
                List<Long> bundleIds = new ArrayList<Long>();
                bundleIds.add(deployed.getBundle().getBundleId());
                for (WebApplication app : waiting) {
                    bundleIds.add(app.getBundle().getBundleId());
                }
                return bundleIds;
            }
        }
        
        public synchronized void unregister(WebApplication webApp, boolean redeploy) {
            if (deployed == webApp) {
                deployed = null;
                if (redeploy) {
                    WebApplication candidate = null;
                    for (WebApplication app : waiting) {
                        if (candidate == null || candidate.getBundle().getBundleId() > app.getBundle().getBundleId()) {
                            candidate = app;
                        }
                    }
                    if (candidate != null) {
                        waiting.remove(candidate);
                        deployed = candidate;
                        deployed.schedule();
                    }
                }
            } else {
                waiting.remove(webApp);
            }
        }
        
        public synchronized void remove(WebApplication webApp) {
            if (deployed == webApp) {
                // this will cause unregister() to be invoked
                deployed.undeploy();
            } else {
                waiting.remove(webApp);
            }
        }
        
        public synchronized WebApplication getDeployed() {
            return deployed;
        }
        
        public String toString() {
            return "ContextPath[" + contextPath + " " + deployed + " " + waiting + "]";
        }
    }
}
