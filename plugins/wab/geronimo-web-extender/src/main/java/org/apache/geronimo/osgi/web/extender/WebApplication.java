/*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.BundleDeploymentContext;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of a WAB deployed to an available Web Container
 * instance.
 *
 * @version $Rev$, $Date$
 */
public class WebApplication implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

    private final WebContainerExtender extender;
    // the bundle where the web application resides
    private final Bundle bundle;
    // the deployed context path from the bundle headers
    private final String contextPath;

    private final AtomicBoolean scheduled = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();

    private boolean destroyed;

    private Artifact deployedArtifact;

    /**
     * Construct a WebApplicationImp object to represent a
     * WAB-resident application.
     *
     * @param bundle    The bundle containing the WAB.
     * @param contextPath
     *                  The context path from the WAB headers.
     */
    public WebApplication(WebContainerExtender extender, Bundle bundle, String contextPath) {
        this.extender = extender;
        this.bundle = bundle;
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    /**
     * Provide access to the bundle where the application resides.
     *
     * @return The Bundle instance for the WAB.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Schedule this handler for deployment processing.
     */
    public void schedule() {
        // only one scheduled startup at a time.
        if (scheduled.compareAndSet(false, true)) {
            extender.getExecutorService().submit(this);
        }
    }

    /**
     * Run the application deployment process in a separate thread.
     */
    public void run() {
        scheduled.set(false);
        synchronized (scheduled) {
            synchronized (extender.getConfigurationManager()) {
                synchronized (running) {
                    running.set(true);
                    try {
                        doRun();
                    } finally {
                        running.set(false);
                        running.notifyAll();
                    }
                }
            }
        }
    }

    private void deploying() {
        extender.getEventDispatcher().deploying(bundle, contextPath);
    }

    private void deployed() {
        extender.getEventDispatcher().deployed(bundle, contextPath);
    }

    private void undeploying() {
        extender.getEventDispatcher().undeploying(bundle, contextPath);
    }

    private void undeployed() {
        extender.getEventDispatcher().undeployed(bundle, contextPath);
    }

    private void failed(Throwable cause) {
        extender.getEventDispatcher().failed(bundle, contextPath, cause);
    }

    /**
     * This method must be called inside a synchronized block to ensure this method is not run concurrently
     */
    private void doRun() {
        if (destroyed) {
            return;
        }
        ConfigurationData configurationData = null;
        try {
            // send out a broadcast alert that we're going to do this
            deploying();

            ConfigurationManager configurationManager = extender.getConfigurationManager();

            File configSer = bundle.getBundleContext().getDataFile("config.ser");
            if (configSer.exists() && configSer.lastModified() == bundle.getLastModified()) {
                LOGGER.info("Redeploying WAB {} at {}", new Object[] {bundle, contextPath});

                FileInputStream in = new FileInputStream(configSer);
                try {
                    configurationData = ConfigurationUtil.readConfigurationData(in);
                } finally {
                    in.close();
                }
            } else {
                LOGGER.info("Deploying WAB {} at {}", new Object[] {bundle, contextPath});

                ModuleIDBuilder idBuilder = new ModuleIDBuilder();
                Kernel kernel = extender.getKernel();
                Naming naming = kernel.getNaming();
                ModuleBuilder webModuleBuilder = extender.getWebModuleBuilder();
                if (webModuleBuilder == null) {
                    throw new DeploymentException("Unable to deploy " + bundle + " WAB. No web module builders found.");
                }
                WebModule webModule = (WebModule) webModuleBuilder.createModule(bundle, naming, idBuilder);

                BundleDeploymentContext deploymentContext =
                    new BundleDeploymentContext(
                        webModule.getEnvironment(),
                        ConfigurationModuleType.WAB,
                        naming,
                        configurationManager,
                        bundle.getBundleContext(),
                        extender.getServerName(),
                        webModule.getModuleName(),
                        extender.getTransactionManagerObjectName(),
                        extender.getConnectionTrackerObjectName(),
                        extender.getCorbaGBeanObjectName(),
                        new HashMap(),
                        bundle);
                webModule.setEarContext(deploymentContext);
                webModule.setRootEarContext(deploymentContext);

                deploymentContext.flush();
                deploymentContext.initializeConfiguration();

                webModule.getJndiScope(JndiScope.app).put("app/AppName", webModule.getName());

                webModuleBuilder.initContext(deploymentContext, webModule, bundle);

                AbstractName appJndiName = naming.createChildName(deploymentContext.getModuleName(), "ApplicationJndi", "ApplicationJndi");
                deploymentContext.getGeneralData().put(EARContext.APPLICATION_JNDI_NAME_KEY, appJndiName);

                webModuleBuilder.addGBeans(deploymentContext, webModule, bundle, extender.getRepositories());

                GBeanData appContexts = new GBeanData(appJndiName, ApplicationJndi.class);
                appContexts.setAttribute("globalContextSegment", webModule.getJndiScope(JndiScope.global));
                appContexts.setAttribute("applicationContextMap", webModule.getJndiScope(JndiScope.app));
                appContexts.setReferencePattern("GlobalContext", extender.getGlobalContextAbstractName());
                deploymentContext.addGBean(appContexts);

                configurationData = deploymentContext.getConfigurationData();
                FileOutputStream configSerOut =  null;
                try {
                    configSerOut = new FileOutputStream(configSer);
                    ConfigurationUtil.writeConfigurationData(configurationData, configSerOut);
                } finally {
                    IOUtils.close(configSerOut);
                }

                // set config.ser last modified time to be of the bundle
                configSer.setLastModified(bundle.getLastModified());

                File geronimoPlugin = bundle.getBundleContext().getDataFile("geronimo-plugin.xml");
                FileOutputStream geronimoPluginOut = null;
                try {
                    geronimoPluginOut = new FileOutputStream(geronimoPlugin);
                    PluginType pluginMetadata = deploymentContext.getPluginMetadata();
                    PluginXmlUtil.writePluginMetadata(pluginMetadata, geronimoPluginOut);
                    DependencyManager.updatePluginMetadata(bundle.getBundleContext(), bundle);
                } finally {
                    IOUtils.close(geronimoPluginOut);
                }
                deploymentContext.close();
            }

            configurationData.setUseEnvironment(true);
            configurationData.setBundleContext(bundle.getBundleContext());

            configurationManager.loadConfiguration(configurationData);
            configurationManager.startConfiguration(configurationData.getId());

            deployedArtifact = configurationData.getId();

            LOGGER.info("Deployed WAB {} at {}", new Object[] {bundle, contextPath});

            // send out the deployed event
            deployed();
        } catch (Throwable exception) {
            LOGGER.error("Unable to start web application for bundle " + getBundle().getSymbolicName(), exception);
            // broadcast a failure event
            failed(exception);
            // undeploy the configuration (in case it was loaded but failed to start)
            if (configurationData != null) {
                undeploy(configurationData.getId());
            }
            // unregister the application and possibly let other WABs with the same ContextPath to deploy
            extender.unregisterWebApplication(this);
        }
    }

    private void undeploy(Artifact artifact) {
        ConfigurationManager configurationManager = extender.getConfigurationManager();
        try {
            configurationManager.uninstallConfiguration(artifact);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchConfigException e) {
            // ignore
        } catch (LifecycleException e) {
        }
    }

    /**
     * Undeploy a web application.
     */
    public void undeploy() {
        destroyed = true;

        synchronized (running) {
            while (running.get()) {
                try {
                    running.wait();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        // send the undeploying event
        undeploying();

        if (deployedArtifact != null) {
            LOGGER.info("Undeploying WAB {} at {}", new Object[] {bundle, contextPath});
            undeploy(deployedArtifact);
            LOGGER.info("Undeployed WAB {} at {}", new Object[] {bundle, contextPath});
        }

        // finished with the undeploy operation
        undeployed();

        // unregister the application and possibly let other WABs with the same ContextPath to deploy
        extender.unregisterWebApplication(this);
    }

}
