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
import org.apache.geronimo.kernel.DependencyManager;
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

    private static List loadChildren(Kernel kernel, String configName) {
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

    public void printResults(Set<Artifact> lcresult, StringBuffer buf) {
        for (Artifact config : lcresult) {

            //TODO might be a hack
            List<String> kidsChild = loadChildren(kernel, config.toString());

            //TODO figure out the web url and show it when appropriate.
            buf.append("    ").append(config).append("<br />");
            for (String kid: kidsChild) {
                buf.append("      `-> ").append(kid).append("<br />");
            }
            buf.append("<br />");
        }
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        actionResponse.setRenderParameter("message", ""); // set to blank first
        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            String config = getConfigID(actionRequest);
            Artifact configId = Artifact.create(config);

            if (START_ACTION.equals(action)) {
                if(!configurationManager.isLoaded(configId)) {
                    configurationManager.loadConfiguration(configId);
                }
                if(!configurationManager.isRunning(configId)) {
                    org.apache.geronimo.kernel.config.LifecycleResults lcresult = configurationManager.startConfiguration(configId);
                    message(actionResponse, lcresult, "Started application<br /><br />");
                }
            } else if (STOP_ACTION.equals(action)) {
                if(configurationManager.isRunning(configId)) {
                    configurationManager.stopConfiguration(configId);
                }
                if(configurationManager.isLoaded(configId)) {
                    LifecycleResults lcresult = configurationManager.unloadConfiguration(configId);
                    message(actionResponse, lcresult, "Stopped application<br /><br />");
                }
            } else if (UNINSTALL_ACTION.equals(action)) {
                configurationManager.uninstallConfiguration(configId);
                message(actionResponse, null, "Uninstalled application<br /><br />"+configId+"<br /><br />");
            } else if (RESTART_ACTION.equals(action)) {
                LifecycleResults lcresult = configurationManager.reloadConfiguration(configId);
                message(actionResponse, lcresult, "Restarted application<br /><br />");
            } else {
                message(actionResponse, null, "Invalid value for changeState: " + action + "<br /><br />");
                throw new PortletException("Invalid value for changeState: " + action);
            }
        } catch (NoSuchConfigException e) {
            // ignore this for now
            message(actionResponse, null, "Configuration not found<br /><br />");
            logger.error("Configuration not found", e);
        } catch (LifecycleException e) {
            // todo we have a much more detailed report now
            message(actionResponse, null, "Lifecycle operation failed<br /><br />");
            logger.error("Lifecycle operation failed ", e);
        } catch (Throwable e) {
            message(actionResponse, null, "Encountered an unhandled exception<br /><br />");
            logger.error("Exception", e);
        }
    }

    private void message(ActionResponse actionResponse, LifecycleResults lcresult, String str) {
        StringBuffer buf = new StringBuffer(str);
        if (lcresult != null) {
            this.printResults(lcresult.getStarted(), buf);
        }
        actionResponse.setRenderParameter("messageStatus", buf.toString());
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
        ConfigurationManager configManager = ConfigurationUtil.getConfigurationManager(kernel);
        List<ConfigurationInfo> infos = configManager.listConfigurations();
        for (ConfigurationInfo info : infos) {

            String moduleType = getInitParameter(CONFIG_INIT_PARAM);
            if (ConfigurationModuleType.WAR.getName().equalsIgnoreCase(moduleType)) {

                if (info.getType().getValue() == ConfigurationModuleType.WAR.getValue()) {
                    ModuleDetails details = new ModuleDetails(info.getConfigID(), info.getType(), info.getState());
                    try {
                        AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                        boolean loaded = loadModule(configManager, configObjName);

                        WebModule webModule = (WebModule) PortletManager.getModule(renderRequest, info.getConfigID());
                        if (webModule != null) {
                            details.getContextPaths().add(webModule.getContextPath());
                        }

                        if (showDependencies) {
                            addDependencies(details, configObjName);
                        }
                        if (loaded) {
                            unloadModule(configManager, configObjName);
                        }
                    } catch (InvalidConfigException ice) {
                        // Should not occur
                        ice.printStackTrace();
                    }
                    moduleDetails.add(details);
                } else if (info.getType().getValue() == ConfigurationModuleType.EAR.getValue()) {
                    try {
                        AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                        boolean loaded = loadModule(configManager, configObjName);

                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if(config != null){
                            for (Configuration child : config.getChildren()) {
                                if (child.getModuleType().getValue() == ConfigurationModuleType.WAR.getValue()) {
                                    ModuleDetails childDetails = new ModuleDetails(info.getConfigID(), child.getModuleType(), info.getState());
                                    childDetails.setComponentName(child.getId().toString());
                                    WebModule webModule = getWebModule(config, child);
                                    if (webModule != null) {
                                        childDetails.getContextPaths().add(webModule.getContextPath());
                                    }
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
                        // Should not occur
                        ice.printStackTrace();
                    }
                }

            } else if (shouldListConfig(info.getType())) {
                ModuleDetails details = new ModuleDetails(info.getConfigID(), info.getType(), getConfigurationState(info));
                try {
                    AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                    boolean loaded = loadModule(configManager, configObjName);

                    if (info.getType().getValue() == ConfigurationModuleType.EAR.getValue()) {
                        Configuration config = configManager.getConfiguration(info.getConfigID());
                        if(config != null){
                            Iterator childs = config.getChildren().iterator();
                            while (childs.hasNext()) {
                                Configuration child = (Configuration) childs.next();
                                if (child.getModuleType().getValue() == ConfigurationModuleType.WAR.getValue()) {
                                    WebModule webModule = getWebModule(config, child);
                                    if (webModule != null) {
                                        details.getContextPaths().add(webModule.getContextPath());
                                    }
                                }
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
                    // Should not occur
                    ice.printStackTrace();
                }
                moduleDetails.add(details);
            }
        }
        Collections.sort(moduleDetails);
        renderRequest.setAttribute("configurations", moduleDetails);
        renderRequest.setAttribute("showWebInfo", Boolean.valueOf(showWebInfo()));
        renderRequest.setAttribute("showDependencies", Boolean.valueOf(showDependencies));
        if (moduleDetails.size() == 0) {
            renderRequest.setAttribute("messageInstalled", "No modules found of this type<br /><br />");
        } else {
            renderRequest.setAttribute("messageInstalled", "");
        }
        renderRequest.setAttribute("messageStatus", renderRequest.getParameter("messageStatus"));
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
            // Should not occur
            e.printStackTrace();
        }        
    }
    
    private boolean showWebInfo() {
        String moduleType = getInitParameter(CONFIG_INIT_PARAM);
        return ConfigurationModuleType.WAR.getName().equalsIgnoreCase(moduleType) ||
               ConfigurationModuleType.EAR.getName().equalsIgnoreCase(moduleType);
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
    public static class ModuleDetails implements Comparable, Serializable {
        private static final long serialVersionUID = -7022687152297202079L;
        private final Artifact configId;
        private final ConfigurationModuleType type;
        private final State state;
        private List<Artifact> parents = new ArrayList<Artifact>();
        private List<Artifact> children = new ArrayList<Artifact>();
        private boolean expertConfig = false;   // used to mark this config as one that should only be managed (stop/uninstall) by expert users.
        private List<String> contextPaths = new ArrayList<String>();
        private String componentName;

        public ModuleDetails(Artifact configId, ConfigurationModuleType type, State state) {
            this.configId = configId;
            this.type = type;
            this.state = state;
            if (configId.toString().indexOf("org.apache.geronimo.configs/") == 0) {
                this.expertConfig = true;
            }
        }

        public int compareTo(Object o) {
            if (o != null && o instanceof ModuleDetails){
                return configId.compareTo(((ModuleDetails)o).configId);
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
    }
}
