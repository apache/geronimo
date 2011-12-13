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

package org.apache.geronimo.console.configmanager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.aries.ApplicationGBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManagerPortlet.class);

    private static final String START_ACTION = "start";

    private static final String STOP_ACTION = "stop";

    private static final String RESTART_ACTION = "restart";

    private static final String UNINSTALL_ACTION = "uninstall";

    private static final String CONFIG_INIT_PARAM = "config-type";

    private static final String SHOW_DEPENDENCIES_COOKIE = "org.apache.geronimo.configmanager.showDependencies";

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private boolean showDisplayName;

    private String moduleType;

    private static List<String> loadChildren(Kernel kernel, String configName) {
        List<String> kids = new ArrayList<String>();

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("J2EEApplication", configName);
        filter.put("j2eeType", "WebModule");

        Set<AbstractName> test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (AbstractName child : test) {
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "EJBModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (AbstractName child : test) {
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "AppClientModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (AbstractName child : test) {
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }

        filter.put("j2eeType", "ResourceAdapterModule");
        test = kernel.listGBeans(new AbstractNameQuery(null, filter));
        for (AbstractName child : test) {
            String childName = child.getNameProperty("name");
            kids.add(childName);
        }
        return kids;
    }

    public String printResults(Set<Artifact> lcresult) {
        StringBuilder sb = new StringBuilder();
        for (Artifact config : lcresult) {

            // TODO might be a hack
            List<String> kidsChild = loadChildren(kernel, config.toString());

            // TODO figure out the web url and show it when appropriate.
            sb.append("<br />").append(config);
            for (String kid : kidsChild) {
                sb.append("<br />-> ").append(kid);
            }
        }
        return sb.toString();
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        actionResponse.setRenderParameter("message", ""); // set to blank first

        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            String config = getConfigID(actionRequest);
            Artifact configId = Artifact.create(config);

            if (START_ACTION.equals(action)) {

                if (!configurationManager.isLoaded(configId)) {
                    configurationManager.loadConfiguration(configId);
                }
                if (!configurationManager.isRunning(configId)) {
                    org.apache.geronimo.kernel.config.LifecycleResults lcresult = configurationManager
                    .startConfiguration(configId);
                    addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg01")
                            + printResults(lcresult.getStarted()));
                }

            } else if (STOP_ACTION.equals(action)) {

                if(configurationManager.isLoaded(configId)) {
                    LifecycleResults lcresult = configurationManager.unloadConfiguration(configId);
                    addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg02") + printResults(lcresult.getStopped()));
                }

            } else if (UNINSTALL_ACTION.equals(action)) {

                configurationManager.uninstallConfiguration(configId);

                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg04") + "<br />" + configId);

            } else if (RESTART_ACTION.equals(action)) {

                LifecycleResults lcresult = configurationManager.restartConfiguration(configId);
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg03") + printResults(lcresult.getStarted()));

            } else {
                addWarningMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.warnMsg01") + action + "<br />");
                throw new PortletException("Invalid value for changeState: " + action);
            }

        } catch (NoSuchConfigException e) {
            // ignore this for now
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg01"));
            logger.error("Configuration not found", e);
        } catch (LifecycleException e) {
            // todo we have a much more detailed report now
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg02"));
            logger.error("Lifecycle operation failed ", e);
        } catch (Throwable e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg03"));
            logger.error("Exception", e);
        }
    }

    /**
     * Check if a configuration should be listed here. This method depends on the "config-type" portlet parameter
     * which is set in portle.xml.
     */
    private boolean shouldListConfig(ConfigurationModuleType info) {
        String configType = getInitParameter(CONFIG_INIT_PARAM);
        return configType == null || info.getName().equalsIgnoreCase(configType);
    }

    private String getConfigID(ActionRequest actionRequest) {
        return actionRequest.getParameter("configId");
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        String cookies = renderRequest.getProperty("cookie");
        boolean showDependencies = (cookies != null && cookies.indexOf(SHOW_DEPENDENCIES_COOKIE + "=true") > 0);

        List<ModuleDetails> moduleDetails = new ArrayList<ModuleDetails>();
        ConfigurationManager configManager = PortletManager.getConfigurationManager();
        List<ConfigurationInfo> infos = configManager.listConfigurations();

        for (ConfigurationInfo info : infos) {
            if (ConfigurationModuleType.WAR.getName().equalsIgnoreCase(moduleType)) {

                if (info.getType() == ConfigurationModuleType.WAR) {
                    ModuleDetails details = new ModuleDetails(info.getConfigID(), info.getType(), info.getState());
                    try {
                        AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                        boolean loaded = loadModule(configManager, configObjName);
                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if (config != null) {
                            for(Map.Entry<AbstractName, GBeanData> entry : config.getGBeans().entrySet()) {
                                if(entry.getKey().getNameProperty(NameFactory.J2EE_TYPE).equals(NameFactory.WEB_MODULE)) {
                                    details.getContextPaths().add((String)entry.getValue().getAttribute("contextPath"));
                                    details.setDisplayName(((WebAppInfo)entry.getValue().getAttribute("webAppInfo")).displayName);
                                }
                            }
                        }

                        if (showDependencies) {
                            addDependencies(details, configObjName);
                        }

                        if (loaded) {
                            unloadModule(configManager, configObjName);
                        }
                    } catch (InvalidConfigException ice) {
                        logger.error("Fail to load configuration", ice);
                    }
                    moduleDetails.add(details);
                } else if (info.getType() == ConfigurationModuleType.EAR) {
                    try {
                        AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                        boolean loaded = loadModule(configManager, configObjName);

                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if (config != null) {
                            for (Map.Entry<AbstractName, GBeanData> entry : config.getGBeans().entrySet()) {
                                if (entry.getKey().getNameProperty(NameFactory.J2EE_TYPE).equals(NameFactory.WEB_MODULE)) {
                                    ModuleDetails childDetails = new ModuleDetails(info.getConfigID(), ConfigurationModuleType.WAR, info.getState());
                                    AbstractName webModuleAbName = entry.getKey();
                                    GBeanData webModuleGBeanData = entry.getValue();
                                    childDetails.setComponentName(webModuleAbName.getNameProperty("name"));
                                    childDetails.getContextPaths().add((String) webModuleGBeanData.getAttribute("contextPath"));
                                    childDetails.setDisplayName(((WebAppInfo)webModuleGBeanData.getAttribute("webAppInfo")).displayName);
                                    if (showDependencies) {
                                        addDependencies(childDetails, configObjName);
                                    }
                                    moduleDetails.add(childDetails);
                                }
                            }
                        }
                        if (loaded) {
                            unloadModule(configManager, configObjName);
                        }
                    } catch (InvalidConfigException ice) {
                        logger.error("Fail to load configuration", ice);
                    }
                }

            } else if (shouldListConfig(info.getType())) {
                ModuleDetails details = new ModuleDetails(info.getConfigID(), info.getType(), getConfigurationState(info));
                try {
                    AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                    boolean loaded = loadModule(configManager, configObjName);

                    if (info.getType() == ConfigurationModuleType.EAR) {
                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if(config != null){
                            for(Map.Entry<AbstractName, GBeanData> entry : config.getGBeans().entrySet()) {
                                if(entry.getKey().getNameProperty(NameFactory.J2EE_TYPE).equals(NameFactory.WEB_MODULE)) {
                                    details.getContextPaths().add((String)entry.getValue().getAttribute("contextPath"));
                                    details.setDisplayName(((WebAppInfo)entry.getValue().getAttribute("webAppInfo")).displayName);
                                }
                            }
                        }
                    } else if (info.getType() == ConfigurationModuleType.EBA) {
                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if (config != null && configManager.isRunning(info.getConfigID())) {
                            for(Map.Entry<AbstractName, GBeanData> entry : config.getGBeans().entrySet()) {
                                if(entry.getKey().getNameProperty("name").equals("AriesApplication")) {
                                    try {
                                        ApplicationGBean applicationGBean = (ApplicationGBean)PortletManager.getKernel().getGBean(entry.getKey());
                                        long[] bundleIds = applicationGBean.getApplicationContentBundleIds();
                                        BundleContext bundleContext = config.getBundleContext();
                                        for (long id : bundleIds){
                                            Bundle bundle = bundleContext.getBundle(id);
                                            if (bundle != null && bundle.getHeaders().get("Web-ContextPath") != null){
                                                details.getContextPaths().add((String)bundle.getHeaders().get("Web-ContextPath"));
                                            }
                                        }
                                    } catch (GBeanNotFoundException e) {
                                        logger.error("AriesApplication GBean is not found", e);
                                    } catch (InternalKernelException e) {
                                        logger.error("AriesApplication GBean is not found", e);
                                    } catch (IllegalStateException e) {
                                        logger.error("AriesApplication GBean is not found", e);
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (info.getType().equals(ConfigurationModuleType.CAR)) {
                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        details.setClientAppServerSide(config.getOwnedConfigurations().size() > 0);
                    }
                    if (showDependencies) {
                        addDependencies(details, configObjName);
                    }
                    if (loaded) {
                        unloadModule(configManager, configObjName);
                    }
                } catch (InvalidConfigException ice) {
                    logger.error("Fail to load configuration", ice);
                }
                moduleDetails.add(details);
            }

        }

        Collections.sort(moduleDetails);
        renderRequest.setAttribute("configurations", moduleDetails);
        renderRequest.setAttribute("showWebInfo", Boolean.valueOf(showWebInfo()));
        renderRequest.setAttribute("showDisplayName", Boolean.valueOf(showDisplayName));
        renderRequest.setAttribute("showDependencies", Boolean.valueOf(showDependencies));
        if (moduleDetails.size() == 0) {
            addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.warnMsg02"));
        }
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    private State getConfigurationState(ConfigurationInfo configurationInfo) {
        State configurationState = configurationInfo.getState();
        if (configurationState.isRunning()) {
            // Check whether the Configuration's sub-gbeans are running
            try {
                Configuration configuration = PortletManager.getConfigurationManager().getConfiguration(configurationInfo.getConfigID());
                Map<AbstractName, GBeanData> abstractNameGBeanDataMap = configuration.getGBeans();
                // Check one sub-GBean's state, if one gbean fails to start, all will be shutdown
                Iterator<AbstractName> it = abstractNameGBeanDataMap.keySet().iterator();
                if (it.hasNext()) {
                    AbstractName abstractName = it.next();
                    if (!PortletManager.getKernel().isRunning(abstractName)) {
                        return State.STOPPED;
                    }
                }
            } catch (InternalKernelException e) {
                return State.STOPPED;
            } catch (IllegalStateException e) {
                return State.STOPPED;
            }
        }
        return configurationState;
    }

    private WebModule getWebModule(Configuration config, Configuration child) {
        try {
            Map<String, String> query1 = new HashMap<String, String>();
            String name = config.getId().getArtifactId();
            query1.put("J2EEApplication", config.getId().toString());
            query1.put("j2eeType", "WebModule");
            query1.put("name", child.getId().getArtifactId().substring(name.length()+1));
            AbstractName childName = new AbstractName(config.getAbstractName().getArtifact(), query1);
            return (WebModule)kernel.getGBean(childName);
        } catch(Exception h){
            // No gbean found, will not happen
            // Except if module not started, ignored
        }
        return null;
    }

    private boolean loadModule(ConfigurationManager configManager, AbstractName configObjName) {
        if(!kernel.isLoaded(configObjName)) {
            try {
                configManager.loadConfiguration(configObjName.getArtifact());
                return true;
            } catch (NoSuchConfigException e) {
                // Should not occur
                e.printStackTrace();
            } catch (LifecycleException e) {
                // config could not load because one or more of its dependencies
                // has been removed. cannot load the configuration in this case,
                // so don't rely on that technique to discover its parents or children
                if (e.getCause() instanceof MissingDependencyException) {
                    // do nothing
                } else {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void addDependencies(ModuleDetails details, AbstractName configObjName) {
        DependencyManager depMgr = kernel.getDependencyManager();
        Set<AbstractName> parents = depMgr.getParents(configObjName);
        for (AbstractName parent : parents) {
            details.getParents().add(parent.getArtifact());
        }
        Set<AbstractName> children = depMgr.getChildren(configObjName);
        for (AbstractName child : children) {
            //if(configManager.isConfiguration(child.getArtifact()))
            if (child.getNameProperty("configurationName") != null) {
                details.getChildren().add(child.getArtifact());
            }
        }
        Collections.sort(details.getParents());
        Collections.sort(details.getChildren());
    }

    private void unloadModule(ConfigurationManager configManager, AbstractName configObjName) {
        try {
            configManager.unloadConfiguration(configObjName.getArtifact());
        } catch (NoSuchConfigException e) {
            logger.error("Fail to unload module " + configObjName, e);
        }  catch (LifecycleException e) {
            logger.error("Fail to unload module " + configObjName, e);
        }
    }

    private boolean showWebInfo() {
        return ConfigurationModuleType.WAR.getName().equalsIgnoreCase(moduleType) ||
               ConfigurationModuleType.EAR.getName().equalsIgnoreCase(moduleType) ||
               ConfigurationModuleType.EBA.getName().equalsIgnoreCase(moduleType);
    }

    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        normalView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/help.jsp");
        moduleType = getInitParameter(CONFIG_INIT_PARAM);
        //Only show the displayNames for the web applications
        showDisplayName = ConfigurationModuleType.WAR.getName().equalsIgnoreCase(moduleType);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        kernel = null;
        super.destroy();
    }

    /**
     * Convenience data holder for portlet that displays deployed modules.
     * Includes context path information for web modules.
     */
    public static class ModuleDetails implements Comparable<ModuleDetails>, Serializable {
        private static final long serialVersionUID = -7022687152297202079L;
        private final Artifact configId;
        private final ConfigurationModuleType type;
        private final State state;
        private List<Artifact> parents = new ArrayList<Artifact>();
        private List<Artifact> children = new ArrayList<Artifact>();
        private boolean expertConfig = false;   // used to mark this config as one that should only be managed (stop/uninstall) by expert users.
        private List<String> contextPaths = new ArrayList<String>();
        private String componentName;
        //This flag is used to indicate whether it is the client side if the module is a client application
        private boolean clientAppServerSide = false;
        private String displayName;

        public ModuleDetails(Artifact configId, ConfigurationModuleType type, State state) {
            this.configId = configId;
            this.type = type;
            this.state = state;
            if (configId.toString().indexOf("org.apache.geronimo.configs/") == 0 || configId.toString().indexOf("org.apache.geronimo.plugins/") == 0) {
                this.expertConfig = true;
            }
        }

        public int compareTo(ModuleDetails o) {
            if (o != null ){
                return configId.compareTo(o.configId);
            } else {
                return -1;
            }
        }

        public Artifact getConfigId() {
            return configId;
        }

        public State getState() {
            return state;
        }

        public ConfigurationModuleType getType() {
            return type;
        }

        public boolean getExpertConfig() {
            return expertConfig;
        }

        public List<Artifact> getParents() {
            return parents;
        }

        public List<Artifact> getChildren() {
            return children;
        }

        public List<String> getContextPaths() {
            return contextPaths;
        }

        public String getComponentName(){
            return componentName;
        }

        public void setComponentName(String name){
            componentName = name;
        }

        public String getDisplayName(){
            return displayName;
        }

        public void setDisplayName(String name){
            displayName = name;
        }

        public void setClientAppServerSide(boolean clientAppServerSide) {
            this.clientAppServerSide = clientAppServerSide;
        }

        public boolean isClientAppServerSide() {
            return this.clientAppServerSide;
        }
    }
}
