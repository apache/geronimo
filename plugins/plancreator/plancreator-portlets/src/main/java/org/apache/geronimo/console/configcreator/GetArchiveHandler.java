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
package org.apache.geronimo.console.configcreator;

import java.io.File;
import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.configcreator.configData.EARConfigData;
import org.apache.geronimo.console.configcreator.configData.EjbConfigData;
import org.apache.geronimo.console.configcreator.configData.WARConfigData;
import org.apache.geronimo.j2ee.deployment.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.openejb.deployment.EjbModule;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class GetArchiveHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(GetArchiveHandler.class);

    public GetArchiveHandler(BasePortlet portlet) {
        super(GET_ARCHIVE_MODE, "/WEB-INF/view/configcreator/getArchive.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        String errorMsg = portlet.getLocalizedString(request, "errorMsg01");
        try {
            FileItem fileItem = (FileItem) getUploadFiles().get(MODULE_URI_PARAMETER);
            String fileName = fileItem.getName();
            if (fileName != null && fileName.length() > 0) {
                File uploadedFile = uploadFile(fileItem);
                ApplicationInfo applicationInfo = JSR88_Util.createApplicationInfo(request, uploadedFile);
                ConfigurationModuleType applicationType = applicationInfo.getType();
                if (ConfigurationModuleType.WAR == applicationType) {
                    WARConfigData data = setNewWARSessionData(request);
                    data.setUploadedWarUri(uploadedFile.toURI().toString());
                    data.parseWeb((WebModule) (applicationInfo.getModules().toArray()[0]));
                    return ENVIRONMENT_MODE + "-before";
                }
                if (ConfigurationModuleType.EAR == applicationType) {
                    EARConfigData earConfigData = setNewEARSessionData(request);
                    earConfigData.parseEAR(applicationInfo);
                    return EAR_MODE + "-before";
                }
                if (ConfigurationModuleType.EJB == applicationType) {
                    EjbConfigData ejbJarConfigData = setNewEjbJarSessionData(request);
                    ejbJarConfigData.parseEjbJar((EjbModule) (applicationInfo.getModules().toArray()[0]));
                    return EJB_MODE + "-before";
                }
            }
        } catch(Throwable e) {
            errorMsg = getRootCause(e).getMessage();
        }
        portlet.addErrorMessage(request, errorMsg);
        return getMode();
    }
    
    private Throwable getRootCause(Throwable e) {
        while(e.getCause() != null && e.getCause() != e) {
            e = e.getCause();
        }
        return e;
    }
    
    private File uploadFile(FileItem fileItem) throws PortletException, IOException {
        File tempDir = File.createTempFile("geronimo-planCreator", ".tmpdir");
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        String fileName = getBasename(fileItem.getName().trim());
        File file = new File(tempDir, fileName);
        file.deleteOnExit();
        try {
            fileItem.write(file);
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return file;
    }

    private String getBasename(String fileName) {
        // Firefox sends basename, IE sends full path
        int index = fileName.lastIndexOf('\\');
        if (index != -1) {
            return fileName.substring(index + 1);
        }
        return fileName;
    }
}
