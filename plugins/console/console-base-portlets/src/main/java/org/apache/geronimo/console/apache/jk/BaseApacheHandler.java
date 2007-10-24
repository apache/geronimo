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
package org.apache.geronimo.console.apache.jk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * The base class for all handlers for this portlet
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseApacheHandler extends MultiPageAbstractHandler {
    protected static final String INDEX_MODE = "index";
    protected static final String BASIC_CONFIG_MODE = "basic";
    protected static final String AJP_MODE = "ajp";
    protected static final String WEB_APP_MODE = "webapp";
    protected static final String RESULTS_MODE = "results";

    protected BaseApacheHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    public final static class WebAppData implements Serializable {
        private String parentConfigId;
        private String childName;
        private String moduleBeanName;
        private boolean enabled;
        private String dynamicPattern;
        private boolean serveStaticContent;
        private String contextRoot;
        private String webAppDir;

        public WebAppData(Artifact parentConfigId, String childName, AbstractName moduleBeanName, boolean enabled, String dynamicPattern, boolean serveStaticContent) {
            this.parentConfigId = parentConfigId.toString();
            this.enabled = enabled;
            this.dynamicPattern = dynamicPattern;
            this.serveStaticContent = serveStaticContent;
            this.moduleBeanName = moduleBeanName == null ? null : moduleBeanName.toString();
            this.childName = childName;
        }

        public WebAppData(PortletRequest request, String prefix) {
            parentConfigId = request.getParameter(prefix+"configId");
            childName = request.getParameter(prefix+"childName");
            moduleBeanName = request.getParameter(prefix+"moduleBeanName");
            dynamicPattern = request.getParameter(prefix+"dynamicPattern");
            String test = request.getParameter(prefix+"enabled");
            enabled = test != null && !test.equals("") && !test.equals("false");
            test = request.getParameter(prefix+"serveStaticContent");
            serveStaticContent = test != null && !test.equals("") && !test.equals("false");
            contextRoot = request.getParameter(prefix+"contextRoot");
            webAppDir = request.getParameter(prefix+"webAppDir");
        }

        public void save(ActionResponse response, String prefix) {
            response.setRenderParameter(prefix+"configId", parentConfigId);
            response.setRenderParameter(prefix+"moduleBeanName", moduleBeanName);
            response.setRenderParameter(prefix+"dynamicPattern", dynamicPattern);
            response.setRenderParameter(prefix+"enabled", Boolean.toString(enabled));
            response.setRenderParameter(prefix+"serveStaticContent", Boolean.toString(serveStaticContent));
            if(!isEmpty(contextRoot)) response.setRenderParameter(prefix+"contextRoot", contextRoot);
            if(!isEmpty(webAppDir)) response.setRenderParameter(prefix+"webAppDir", webAppDir);
            if(!isEmpty(childName)) response.setRenderParameter(prefix+"childName", childName);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getParentConfigId() {
            return parentConfigId;
        }

        public void setParentConfigId(String parentConfigId) {
            this.parentConfigId = parentConfigId;
        }

        public String getDynamicPattern() {
            return dynamicPattern;
        }

        public void setDynamicPattern(String dynamicPattern) {
            this.dynamicPattern = dynamicPattern;
        }

        public boolean isServeStaticContent() {
            return serveStaticContent;
        }

        public void setServeStaticContent(boolean serveStaticContent) {
            this.serveStaticContent = serveStaticContent;
        }

        public String getContextRoot() {
            return contextRoot;
        }

        public void setContextRoot(String contextRoot) {
            this.contextRoot = contextRoot;
        }

        public String getWebAppDir() {
            return webAppDir;
        }

        public void setWebAppDir(String webAppDir) {
            this.webAppDir = webAppDir;
        }

        public String getChildName() {
            return childName;
        }

        public String getModuleBeanName() {
            return moduleBeanName;
        }

        public String getName() {
            return isEmpty(childName) ? parentConfigId : childName;
        }

        public boolean isRunning() {
            return webAppDir != null;
        }
    }

    public final static class ApacheModel implements MultiPageModel {
        public final static String WEB_APP_SESSION_KEY = "console.apache.jk.WebApps";
        private String os;
        private Integer addAjpPort;
        private String logFilePath;
        private String workersPath;
        private List webApps = new ArrayList();

        public ApacheModel(PortletRequest request) {
            Map map = request.getParameterMap();
            os = request.getParameter("os");
            // logFilePath and workersPath need to be encoded before saving
            // and decoded after fetching
            logFilePath = request.getParameter("logFilePath");
            if(logFilePath == null) {
                logFilePath = PortletManager.getCurrentServer(request).getServerInfo().resolve("var/log/apache_mod_jk.log").getPath();
            }
            workersPath = request.getParameter("workersPath");
            if(workersPath == null) {
                workersPath = PortletManager.getCurrentServer(request).getServerInfo().resolve("var/config/workers.properties").getPath();
            }
            String ajp = request.getParameter("addAjpPort");
            if(!isEmpty(ajp)) addAjpPort = new Integer(ajp);
            int index = 0;
            boolean found = false;
            while(true) {
                String key = "webapp."+(index++)+".";
                if(!map.containsKey(key+"configId")) {
                    break;
                }
                found = true;
                WebAppData data = new WebAppData(request, key);
                webApps.add(data);
            }
            if(!found) {
                List list = (List) request.getPortletSession(true).getAttribute(WEB_APP_SESSION_KEY);
                if(list != null) {
                    webApps = list;
                }
            }
        }

        public void save(ActionResponse response, PortletSession session) {
            if(!isEmpty(os)) response.setRenderParameter("os", os);
            if(!isEmpty(logFilePath)) response.setRenderParameter("logFilePath", logFilePath);
            if(!isEmpty(workersPath)) response.setRenderParameter("workersPath", workersPath);
            if(addAjpPort != null) response.setRenderParameter("addAjpPort", addAjpPort.toString());
            if(webApps.size() > 0) {
                session.setAttribute(WEB_APP_SESSION_KEY, webApps);
            }
        }
        
        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public Integer getAddAjpPort() {
            return addAjpPort;
        }

        public void setAddAjpPort(Integer addAjpPort) {
            this.addAjpPort = addAjpPort;
        }

        public String getLogFilePath() {
            return logFilePath;
        }

        public void setLogFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
        }

        public String getWorkersPath() {
            return workersPath;
        }

        public void setWorkersPath(String workersPath) {
            this.workersPath = workersPath;
        }

        public List getWebApps() {
            return webApps;
        }

        public void setWebApps(List webApps) {
            this.webApps = webApps;
        }
    }
}
