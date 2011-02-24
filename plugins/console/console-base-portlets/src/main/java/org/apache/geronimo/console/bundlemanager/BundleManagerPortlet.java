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

package org.apache.geronimo.console.bundlemanager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.util.BundleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(BundleManagerPortlet.class);

    private static final String START_ACTION = "start";

    private static final String STOP_ACTION = "stop";

    private static final String RESTART_ACTION = "restart";

    private static final String UPDATE_ACTION = "update";
    
    private static final String UNINSTALL_ACTION = "uninstall";

    private static final String CONFIG_INIT_PARAM = "config-type";

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private String moduleType;

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        actionResponse.setRenderParameter("message", ""); // set to blank first
        
        String id = getConfigID(actionRequest);
        BundleContext bundleContext = getBundleContext(actionRequest);

        try {
            Bundle bundle = bundleContext.getBundle(Long.parseLong(id));

            if (START_ACTION.equals(action)) {
                bundle.start();
            } else if (STOP_ACTION.equals(action)) {
                bundle.stop();
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg02"));
            } else if (UNINSTALL_ACTION.equals(action)) {
                bundle.uninstall();
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg04") + "<br />" + getSymbolicName(bundle));
            } else if (RESTART_ACTION.equals(action)) {
                bundle.stop();
                bundle.start();
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg03"));
            } else if (UPDATE_ACTION.equals(action)) {
                bundle.update();
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg19"));
            } else {
                addWarningMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.warnMsg01") + action + "<br />");
                throw new PortletException("Invalid value for changeState: " + action);
            }            
        } catch (Throwable e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg03"));
            logger.error("Exception", e);
        }
    }
    
    
    private BundleContext getBundleContext(PortletRequest request) {
        return (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
    }
    
    private String getConfigID(ActionRequest actionRequest) {
        return actionRequest.getParameter("bundleId");
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        List<ModuleDetails> moduleDetails = new ArrayList<ModuleDetails>();
            
        Bundle[] bundles = getBundleContext(renderRequest).getBundles();
        for (Bundle bundle : bundles) {
            String contextPath = getContextPath(bundle);
            if (ConfigurationModuleType.WAB.getName().equalsIgnoreCase(moduleType)) {
                ModuleDetails details = new ModuleDetails(bundle, BundleType.WAB);
                if (contextPath != null) {
                    details.getContextPaths().add(contextPath);
                    moduleDetails.add(details);
                }
            } else {
                ModuleDetails details = new ModuleDetails(bundle, BundleType.REGULAR);
                if (contextPath != null) {
                    details.getContextPaths().add(contextPath);
                }
                moduleDetails.add(details);
            }
        }
        
        Collections.sort(moduleDetails);
        renderRequest.setAttribute("configurations", moduleDetails);
        renderRequest.setAttribute("showWebInfo", Boolean.valueOf(showWebInfo()));
        if (moduleDetails.size() == 0) {
            addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.warnMsg02"));
        }
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }
   
    private static String getContextPath(Bundle bundle) {
        return (String) bundle.getHeaders().get(BundleUtil.WEB_CONTEXT_PATH_HEADER);
    }
    
    private static String getBundleName(Bundle bundle) {
        String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
        name = (name == null) ? bundle.getSymbolicName() : name;
        name = (name == null) ? bundle.getLocation() : name;
        return name;
    }
    
    private static String getSymbolicName(Bundle bundle) {
        String name = bundle.getSymbolicName();
        if (name == null) {
            name = bundle.getLocation();
        }
        return name;
    }
    
    private boolean showWebInfo() {
        return true;
    }

    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/help.jsp");
        moduleType = getInitParameter(CONFIG_INIT_PARAM);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

    public static enum BundleType { 
        
        REGULAR("REGULAR"), WAB("WAB");
        
        private final String name;

        BundleType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }      
    }
    
    public static enum BundleState {
        
        UNINSTALLED(Bundle.UNINSTALLED, "Uninstalled"), 
        INSTALLED(Bundle.INSTALLED, "Installed"), 
        RESOLVED(Bundle.RESOLVED, "Resolved"),
        STARTING(Bundle.STARTING, "Starting"), 
        STOPPING(Bundle.STOPPING, "Stopping"), 
        ACTIVE(Bundle.ACTIVE, "Active");
        
        private final int state;
        private final String name;

        BundleState(int state, String name) {
            this.state = state;
            this.name = name;
        }

        public int getState() {
            return state;
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return name;
        }
        
        public boolean isRunning() {
            return state == Bundle.ACTIVE || state == Bundle.STARTING;
        }
        
        public boolean isStopped() {
            return state == Bundle.INSTALLED || state == Bundle.RESOLVED || state == Bundle.STOPPING;
        }
        
        public static BundleState getState(Bundle bundle) {
            int state = bundle.getState();
            switch (state) {
            case Bundle.UNINSTALLED:
                return BundleState.UNINSTALLED;
            case Bundle.INSTALLED:
                return BundleState.INSTALLED;
            case Bundle.RESOLVED:
                return BundleState.RESOLVED;
            case Bundle.STOPPING:
                return BundleState.STOPPING;
            case Bundle.STARTING:
                return BundleState.STARTING;            
            case Bundle.ACTIVE:
                return BundleState.ACTIVE;
            }
            throw new IllegalStateException("Unknown state: " + state);
        }
    }
    
    /**
     * Convenience data holder for portlet that displays deployed modules.
     * Includes context path information for web modules.
     */
    public static class ModuleDetails implements Comparable<ModuleDetails>, Serializable {
        private static final long serialVersionUID = -7022687152297202079L;
        private final long bundleId;
        private final String symbolicName;
        private final String bundleName;
        private final BundleType type;
        private final BundleState state;
        private List<String> contextPaths = new ArrayList<String>();

        public ModuleDetails(Bundle bundle, BundleType type) {
            this.bundleId = bundle.getBundleId();
            this.symbolicName = BundleManagerPortlet.getSymbolicName(bundle);
            this.bundleName = BundleManagerPortlet.getBundleName(bundle);
            this.state = BundleState.getState(bundle);
            this.type = type;
        }
        
        public int compareTo(ModuleDetails o) {
            if (o != null) {
                return (int) (bundleId - o.bundleId);
            } else {
                return -1;
            }
        }

        public long getBundleId() {
            return bundleId;
        }

        public BundleState getState() {
            return state;
        }

        public BundleType getType() {
            return type;
        }

        public List<String> getContextPaths() {
            return contextPaths;
        }

        public String getSymbolicName() {
            return symbolicName;
        }
        
        public String getBundleName() {
            return bundleName;
        }
                
    }
}
