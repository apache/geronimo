/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.SecurityConstants;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.management.State;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public class ConfigManagerPortlet extends BasePortlet {

    private static final String START_ACTION = "start";

    private static final String STOP_ACTION = "stop";

    private static final String UNINSTALL_ACTION = "uninstall";

    private static final String CONTAINSCONFIG_METHOD = "containsConfiguration";

    private static final String UNINSTALL_METHOD = "uninstall";

    private static final String[] CONTAINSCONFIG_SIG = {URI.class.getName()};

    private static final String[] UNINSTALL_SIG = {URI.class.getName()};

    private static final String QUEUETOPIC_URI = "runtimedestination/";

    private static final String CONFIG_INIT_PARAM = "config-type";

    private String messageInstalled = "";

    private String messageStatus = "";

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private static final Collection EXCLUDED;

    static {
        // Add list of the configurationIDs that you do not want to list to this
        // List.
        EXCLUDED = new ArrayList();
    }

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        actionResponse.setRenderParameter("message", ""); // set to blank first
        try {
            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            String config = getConfigID(actionRequest);
            Artifact configID = Artifact.create(config);

            if (START_ACTION.equals(action)) {
                List ancestors = configurationManager.loadRecursive(configID);
                for (Iterator it = ancestors.iterator(); it.hasNext();) {
                    Artifact ancestor = (Artifact) it.next();
                    configurationManager.loadGBeans(ancestor);
                    configurationManager.start(ancestor);
                }
                messageStatus = "Started application<br /><br />";
            } else if (STOP_ACTION.equals(action)) {
                configurationManager.stop(configID);
                configurationManager.unload(configID);
                messageStatus = "Stopped application<br /><br />";
            } else if (UNINSTALL_ACTION.equals(action)) {
                uninstallConfig(actionRequest);
                messageStatus = "Uninstalled application<br /><br />";
            } else {
                messageStatus = "Invalid value for changeState: " + action
                        + "<br /><br />";
                throw new PortletException("Invalid value for changeState: "
                        + action);
            }
        } catch (NoSuchConfigException e) {
            // ignore this for now
            messageStatus = "Configuration not found<br /><br />";
            throw new PortletException("Configuration not found", e);
        } catch (InvalidConfigException e) {
            messageStatus = "Configuration not found<br /><br />";
            throw new PortletException("Configuration not found", e);
        } catch (Exception e) {
            messageStatus = "Encountered an unhandled exception<br /><br />";
            throw new PortletException("Exception", e);
        }
    }

    /**
     * Uninstall an application configuration
     *
     * @param actionRequest
     * @throws PortletException
     * @throws Exception
     */
    private void uninstallConfig(ActionRequest actionRequest)
            throws PortletException, Exception {
        ConfigurationManager configManager = ConfigurationUtil
                .getConfigurationManager(kernel);
        List configStores = configManager.listStores();
        int size = configStores.size();
        String configID = getConfigID(actionRequest);
        Artifact configURI = Artifact.create(configID);
        for (int i = 0; i < size; i++) {
            ObjectName configStore = (ObjectName) configStores.get(i);
            Boolean result = (Boolean) kernel.invoke(configStore,
                    CONTAINSCONFIG_METHOD,
                    new Object[]{configURI}, CONTAINSCONFIG_SIG);
            if (result.booleanValue()) {
                // stop config if running
                if (configManager.isLoaded(configURI)) {
                    int state = kernel.getGBeanState(Configuration.getConfigurationObjectName(configURI));
                    if (state == State.RUNNING.toInt()) {

                        configManager.stop(configURI);
                        configManager.unload(configURI);
                    }
                }
                kernel.invoke(configStore, UNINSTALL_METHOD, new Object[]{configURI}, UNINSTALL_SIG);
            }
        }
    }

    /**
     * Check if a configuration should be listed here. This method depends on the "config-type" portlet parameter
     * which is set in portle.xml.
     */
    private boolean shouldListConfig(ConfigurationInfo info) {
        String configType = getInitParameter(CONFIG_INIT_PARAM);
        if (configType != null && !info.getType().getName().equalsIgnoreCase(configType))
            return false;
        else
            return true;
    }

    /*
     * private URI getConfigID(ActionRequest actionRequest) throws
     * PortletException { URI configID; try { configID = new
     * URI(actionRequest.getParameter("configId")); } catch (URISyntaxException
     * e) { throw new PortletException("Invalid configId parameter: " +
     * actionRequest.getParameter("configId")); } return configID; }
     */

    private String getConfigID(ActionRequest actionRequest)
            throws PortletException {
        return actionRequest.getParameter("configId");
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        List configInfo = new ArrayList();
        ConfigurationManager configManager = ConfigurationUtil
                .getConfigurationManager(kernel);
        List stores = configManager.listStores();
        for (Iterator i = stores.iterator(); i.hasNext();) {
            ObjectName storeName = (ObjectName) i.next();
            try {
                List infos = configManager.listConfigurations(storeName);
                for (Iterator j = infos.iterator(); j.hasNext();) {
                    ConfigurationInfo info = (ConfigurationInfo) j.next();
                    if (shouldListConfig(info)) {
                        // TODO: Check if this is the right solution
                        // Disregard JMS Queues and Topics &&
                        if (!info.getConfigID().toURI().getPath().startsWith(QUEUETOPIC_URI)
                                && !info
                                .getConfigID().toURI()
                                .getPath()
                                .startsWith(SecurityConstants.SECURITY_CONFIG_PREFIX)) {
                            configInfo.add(info);
                        }
                    }
                }
            } catch (NoSuchStoreException e) {
                // we just got this list so this should not happen
                // in the unlikely event it does, just continue
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
        }
        Collections.sort(configInfo, new Comparator() {
            public int compare(Object o1, Object o2) {
                ConfigurationInfo ci1 = (ConfigurationInfo) o1;
                ConfigurationInfo ci2 = (ConfigurationInfo) o2;
                return ci1.getConfigID().toString().compareTo(ci2.getConfigID().toString());
            }
        });
        renderRequest.setAttribute("configurations", configInfo);
        messageInstalled = configInfo.size() == 0 ? "No modules found of this type<br /><br />"
                : "";
        renderRequest.setAttribute("messageInstalled", messageInstalled);
        renderRequest.setAttribute("messageStatus", messageStatus);
        messageStatus = "";
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
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
}
