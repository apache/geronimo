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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
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
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.ConfigIDExtractor;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;

/**
 * $Rev$ $Date$
 */
public class DeploymentPortlet extends BasePortlet {
    private static final Logger log = LoggerFactory.getLogger(DeploymentPortlet.class);
    
    private static final String DEPLOY_VIEW          = "/WEB-INF/view/configmanager/deploy.jsp";
    private static final String HELP_VIEW            = "/WEB-INF/view/configmanager/deployHelp.jsp";
    private static final String MIGRATED_PLAN_PARM   = "migratedPlan";
    private static final String ORIGINAL_PLAN_PARM   = "originalPlan";

    private PortletRequestDispatcher deployView;
    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
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
        FileInputStream fis = null;
        try {
            DeploymentManager mgr = dfm.getDeploymentManager("deployer:geronimo:inVM", null, null);
            try {
                boolean isRedeploy = redeploy != null && !redeploy.equals("");
                if(mgr instanceof JMXDeploymentManager) {
                    ((JMXDeploymentManager)mgr).setLogConfiguration(false, true);
                }
                Target[] all = mgr.getTargets();
                if (null == all) {
                    throw new IllegalStateException("No target to distribute to");
                }

                ProgressObject progress;
                if(isRedeploy) {
                    TargetModuleID[] targets = identifyTargets(moduleFile, planFile, mgr.getAvailableModules(null, all));
                    if(targets.length == 0) {
                        addErrorMessage(actionRequest, getLocalizedString(actionRequest, "plugin.errorMsg04"), null);
                        log.error(getLocalizedString(actionRequest, "plugin.errorMsg04"));
                        return;
                    }
                    progress = mgr.redeploy(targets, moduleFile, planFile);
                } else {
                    progress = mgr.distribute(new Target[] {all[0]}, moduleFile, planFile);
                }
                while(progress.getDeploymentStatus().isRunning()) {
                    Thread.sleep(100);
                }
                
                String abbrStatusMessage;
                String fullStatusMessage = null;
                if(progress.getDeploymentStatus().isCompleted()) {
                    abbrStatusMessage = getLocalizedString(actionRequest, !isRedeploy ? "plugin.infoMsg01" : "plugin.infoMsg02");
                    addInfoMessage(actionRequest, abbrStatusMessage);
                    // start installed app/s
                    if (!isRedeploy && startApp != null && !startApp.equals("")) {
                        progress = mgr.start(progress.getResultTargetModuleIDs());
                        while(progress.getDeploymentStatus().isRunning()) {
                            Thread.sleep(100);
                        }
                        if (progress.getDeploymentStatus().isCompleted()) {
                            abbrStatusMessage = getLocalizedString(actionRequest, "plugin.infoMsg03");
                            addInfoMessage(actionRequest, abbrStatusMessage);
                        } else {
                            abbrStatusMessage = getLocalizedString(actionRequest, "plugin.errorMsg02");
                            fullStatusMessage = progress.getDeploymentStatus().getMessage();
                            addErrorMessage(actionRequest, abbrStatusMessage, fullStatusMessage);
                            log.error(abbrStatusMessage + "\n" + fullStatusMessage);
                        }
                    }
                } else {
                    fullStatusMessage = progress.getDeploymentStatus().getMessage();
                    // for the abbreviated status message clip off everything
                    // after the first line, which in most cases means the gnarly stacktrace
                    abbrStatusMessage = getLocalizedString(actionRequest, "plugin.errorMsg01");
                    addErrorMessage(actionRequest, abbrStatusMessage, fullStatusMessage);
                    log.error(abbrStatusMessage + "\n" + fullStatusMessage);
                }
            } finally {
                mgr.release();
                if (fis!=null) fis.close();
                if(moduleFile != null && moduleFile.exists()) {
                    if(!moduleFile.delete()) {
                        log.debug("Unable to delete temporary file "+moduleFile);
                        moduleFile.deleteOnExit();
                    }
                }
                if(planFile != null && planFile.exists()) {
                    if(!planFile.delete()) {
                        log.debug("Unable to delete temporary file "+planFile);
                        planFile.deleteOnExit();
                    }
                }
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
                modules.addAll(ConfigIDExtractor.identifyTargetModuleIDs(allModules, moduleId, true));
            } else {
                String name = module != null ? module.getName() : plan.getName();
                int pos = name.lastIndexOf('.');
                if(pos > -1) {
                    name = name.substring(0, pos);
                }
                modules.addAll(ConfigIDExtractor.identifyTargetModuleIDs(allModules, Artifact.DEFAULT_GROUP_ID+"/"+name+"//", true));
            }
        } catch (IOException e) {
            throw new PortletException("Unable to read input files: "+e.getMessage());
        } catch (DeploymentException e) {
            return new TargetModuleID[0];
        }
        return (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        // The deployment plans and messages from the deployers sometime exceeds
        // the buffer size for render attributes. To avoid the buffer
        // overrun the render attributes are temporarily stored in the portlet
        // session during the processAction phase and then copied into render
        // attributes here so the JSP has easier access to them. This seems
        // to only be an issue on tomcat.
        copyRenderAttribute(renderRequest, MIGRATED_PLAN_PARM);
        copyRenderAttribute(renderRequest, ORIGINAL_PLAN_PARM);
        deployView.include(renderRequest, renderResponse);
    }
    
    private void copyRenderAttribute(RenderRequest renderRequest, String attr) {
        Object value = renderRequest.getPortletSession().getAttribute(attr);
        renderRequest.getPortletSession().removeAttribute(attr);
        renderRequest.setAttribute(attr, value);
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        deployView = portletConfig.getPortletContext().getRequestDispatcher(DEPLOY_VIEW);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(HELP_VIEW);
    }

    public void destroy() {
        deployView = null;
        helpView = null;
        super.destroy();
    }
}
