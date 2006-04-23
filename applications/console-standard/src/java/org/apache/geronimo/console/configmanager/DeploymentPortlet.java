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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.ConfigIDExtractor;
import org.apache.geronimo.common.DeploymentException;

public class DeploymentPortlet extends BasePortlet {
    private PortletRequestDispatcher deployView;

    private PortletRequestDispatcher helpView;

    private boolean messageNotRendered = true;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        messageNotRendered = true;
        if (!PortletFileUpload.isMultipartContent(actionRequest)) {
            throw new PortletException("Expected file upload");
        }

        File rootDir = new File(System.getProperty("java.io.tmpdir"));
        PortletFileUpload uploader = new PortletFileUpload(new DiskFileItemFactory(10240, rootDir));
        File moduleFile = null;
        File planFile = null;
        String startApp = null;
        String redeploy = null;
        try {
            List items = uploader.parseRequest(actionRequest);
            for (Iterator i = items.iterator(); i.hasNext();) {
                FileItem item = (FileItem) i.next();
                if (!item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String name = item.getName().trim();
                    File file;
                    if (name.length() == 0) {
                        file = null;
                    } else {
                        // Firefox sends basename, IE sends full path
                        int index = name.lastIndexOf('\\');
                        if (index != -1) {
                            name = name.substring(index + 1);
                        }
                        file = new File(rootDir, name);
                    }
                    if ("module".equals(fieldName)) {
                        moduleFile = file;
                    } else if ("plan".equals(fieldName)) {
                        planFile = file;
                    }
                    if (file != null) {
                        try {
                            item.write(file);
                        } catch (Exception e) {
                            throw new PortletException(e);
                        }
                    }
                } else {
                    // retrieve 'startApp' form field value
                    if ("startApp".equalsIgnoreCase(item.getFieldName())) {
                        startApp = item.getString();
                    } else if ("redeploy".equalsIgnoreCase(item.getFieldName())) {
                        redeploy = item.getString();
                    }
                }
            }
        } catch (FileUploadException e) {
            throw new PortletException(e);
        }
        DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
        try {
            DeploymentManager mgr = dfm.getDeploymentManager("deployer:geronimo:inVM", null, null);
            try {
                boolean isRedeploy = redeploy != null && !redeploy.equals("");
                if(mgr instanceof JMXDeploymentManager) {
                    ((JMXDeploymentManager)mgr).setLogConfiguration(false, true);
                }
                Target[] all = mgr.getTargets();
                ProgressObject progress;
                if(isRedeploy) {
                    TargetModuleID[] targets = identifyTargets(moduleFile, planFile, mgr.getAvailableModules(null, all));
                    progress = mgr.redeploy(targets, moduleFile, planFile);
                } else {
                    progress = mgr.distribute(all, moduleFile, planFile);
                }
                while(progress.getDeploymentStatus().isRunning()) {
                    Thread.sleep(100);
                }

                if(progress.getDeploymentStatus().isCompleted()) {
                    String message = "The application was successfully "+(isRedeploy ? "re" : "")+"deployed.<br/>";
                    // start installed app/s
                    if (!isRedeploy && startApp != null && !startApp.equals("")) {
                        progress = mgr.start(progress.getResultTargetModuleIDs());
                        while(progress.getDeploymentStatus().isRunning()) {
                            Thread.sleep(100);
                        }
                        message+="The application was successfully started";
                    }
                    actionResponse.setRenderParameter("outcome",message);
                } else {
                    actionResponse.setRenderParameter("outcome", "Deployment failed: "+progress.getDeploymentStatus().getMessage());
                }
            } finally {
                mgr.release();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private TargetModuleID[] identifyTargets(File module, File plan, TargetModuleID[] allModules) throws PortletException {
        String moduleId = null;
        List modules = new ArrayList();
        try {
            if(plan != null) {
                moduleId = ConfigIDExtractor.extractModuleIdFromPlan(plan);
            } else if(module != null) {
                moduleId = ConfigIDExtractor.extractModuleIdFromArchive(module);
                if(moduleId == null) {
                    int pos = module.getName().lastIndexOf('.');
                    moduleId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                }
            }
            if(moduleId != null) {
                modules.addAll(ConfigIDExtractor.identifyTargetModuleIDs(allModules, moduleId));
            } else {
                throw new PortletException("Unable to calculate a ModuleID from supplied module and/or plan.");
            }
        } catch (IOException e) {
            throw new PortletException("Unable to read input files: "+e.getMessage());
        } catch (DeploymentException e) {
            throw new PortletException(e.getMessage(), e);
        }
        return (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        if (messageNotRendered) {
            renderRequest.setAttribute("outcome", renderRequest
                    .getParameter("outcome"));
            messageNotRendered = false;
        }
        deployView.include(renderRequest, renderResponse);
        // clear previous message for next rendering
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        deployView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/deploy.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/deployHelp.jsp");
    }

    public void destroy() {
        deployView = null;
        helpView = null;
        super.destroy();
    }
}
