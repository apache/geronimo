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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.geronimo.WebModule;

public class ConfigManagerPortlet extends BasePortlet {

    private static final String START_ACTION = "start";

    private static final String STOP_ACTION = "stop";

    private static final String RESTART_ACTION = "restart";

    private static final String UNINSTALL_ACTION = "uninstall";

    private static final String CONFIG_INIT_PARAM = "config-type";

    private String messageStatus = "";

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

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
                    configurationManager.startConfiguration(configId);
                    messageStatus = "Started application<br /><br />";
                }
            } else if (STOP_ACTION.equals(action)) {
                if(configurationManager.isRunning(configId)) {
                    configurationManager.stopConfiguration(configId);
                }
                if(configurationManager.isLoaded(configId)) {
                    configurationManager.unloadConfiguration(configId);
                    messageStatus = "Stopped application<br /><br />";
                }
            } else if (UNINSTALL_ACTION.equals(action)) {
                configurationManager.uninstallConfiguration(configId);
                messageStatus = "Uninstalled application<br /><br />";
            } else if (RESTART_ACTION.equals(action)) {
                configurationManager.restartConfiguration(configId);
                messageStatus = "Restarted application<br /><br />";
            } else {
                messageStatus = "Invalid value for changeState: " + action + "<br /><br />";
                throw new PortletException("Invalid value for changeState: " + action);
            }
        } catch (NoSuchConfigException e) {
            // ignore this for now
            messageStatus = "Configuration not found<br /><br />";
            throw new PortletException("Configuration not found", e);
        } catch (LifecycleException e) {
            // todo we have a much more detailed report now
            messageStatus = "Lifecycle operation failed<br /><br />";
            throw new PortletException("Exception", e);
        } catch (Exception e) {
            messageStatus = "Encountered an unhandled exception<br /><br />";
            throw new PortletException("Exception", e);
        }
    }

    /**
     * Check if a configuration should be listed here. This method depends on the "config-type" portlet parameter
     * which is set in portle.xml.
     */
    private boolean shouldListConfig(ConfigurationInfo info) {
        String configType = getInitParameter(CONFIG_INIT_PARAM);
        return configType == null || info.getType().getName().equalsIgnoreCase(configType);
    }

    /*
     * private URI getConfigID(ActionRequest actionRequest) throws
     * PortletException { URI configID; try { configID = new
     * URI(actionRequest.getParameter("configId")); } catch (URISyntaxException
     * e) { throw new PortletException("Invalid configId parameter: " +
     * actionRequest.getParameter("configId")); } return configID; }
     */

    private String getConfigID(ActionRequest actionRequest) {
        return actionRequest.getParameter("configId");
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        List moduleDetails = new ArrayList();
        ConfigurationManager configManager = ConfigurationUtil.getConfigurationManager(kernel);
        DependencyManager depMgr = kernel.getDependencyManager();
        List infos = configManager.listConfigurations();
        for (Iterator j = infos.iterator(); j.hasNext();) {
            ConfigurationInfo info = (ConfigurationInfo) j.next();
            if (shouldListConfig(info)) {
                ModuleDetails details = new ModuleDetails(info.getConfigID(), info.getType(), info.getState());

                if (info.getType().getValue()== ConfigurationModuleType.WAR.getValue()){
                    WebModule webModule = (WebModule) PortletManager.getModule(renderRequest, info.getConfigID());
                    if (webModule != null) {
                        details.setContextPath(webModule.getContextPath());
                        details.setUrlFor(webModule.getURLFor());
                    }
                }
                try {
                    AbstractName configObjName = Configuration.getConfigurationAbstractName(info.getConfigID());
                    boolean flag = false;
                    // Check if the configuration is loaded.  If not, load it to get information.
                    if(!kernel.isLoaded(configObjName)) {
                        flag = true;
                        try {
                            configManager.loadConfiguration(configObjName.getArtifact());
                        } catch (NoSuchConfigException e) {
                            // Should not occur
                            e.printStackTrace();
                        } catch (LifecycleException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    java.util.Set parents = depMgr.getParents(configObjName);
                    for(Iterator itr = parents.iterator(); itr.hasNext(); ) {
                        AbstractName parent = (AbstractName)itr.next();
                        details.getParents().add(parent.getArtifact());
                    }
                    java.util.Set children = depMgr.getChildren(configObjName);
                    for(Iterator itr = children.iterator(); itr.hasNext(); ) {
                        AbstractName child = (AbstractName)itr.next();
                        //if(configManager.isConfiguration(child.getArtifact()))
                        if(child.getNameProperty("configurationName") != null) {
                            details.getChildren().add(child.getArtifact());
                        }
                    }
                    Collections.sort(details.getParents());
                    Collections.sort(details.getChildren());

                    // Unload the configuration if it has been loaded earlier for the sake of getting information
                    if(flag) {
                        try {
                            configManager.unloadConfiguration(configObjName.getArtifact());
                        } catch (NoSuchConfigException e) {
                            // Should not occur
                            e.printStackTrace();
                        }
                    }
                } catch(InvalidConfigException ice) {
                    // Should not occur
                    ice.printStackTrace();
                }
                moduleDetails.add(details);
            }
        }
        Collections.sort(moduleDetails);
        renderRequest.setAttribute("configurations", moduleDetails);
        renderRequest.setAttribute("showWebInfo", Boolean.valueOf(getInitParameter(CONFIG_INIT_PARAM).equalsIgnoreCase(ConfigurationModuleType.WAR.getName())));
        if (moduleDetails.size() == 0) {
            renderRequest.setAttribute("messageInstalled", "No modules found of this type<br /><br />");
        } else {
            renderRequest.setAttribute("messageInstalled", "");
        }
        renderRequest.setAttribute("messageStatus", messageStatus);
        messageStatus = "";
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
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
        private final Artifact configId;
        private final ConfigurationModuleType type;
        private final State state;
        private URL urlFor;             // only relevant for webapps
        private String contextPath;     // only relevant for webapps
        private List parents = new ArrayList();
        private List children = new ArrayList();

        public ModuleDetails(Artifact configId, ConfigurationModuleType type, State state) {
            this.configId = configId;
            this.type = type;
            this.state = state;
        }

        public ModuleDetails(Artifact configId, ConfigurationModuleType type, State state, List parents, List children) {
            this.configId = configId;
            this.type = type;
            this.state = state;
            this.parents = parents;
            this.children = children;
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

        public URL getUrlFor() {
            return urlFor;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setUrlFor(URL urlFor) {
            this.urlFor = urlFor;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public ConfigurationModuleType getType() {
            return type;
        }

        public List getParents() {
            return parents;
        }

        public List getChildren() {
            return children;
        }
    }
}
