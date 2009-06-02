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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class GetArchiveHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(GetArchiveHandler.class);

    public GetArchiveHandler(BasePortlet portlet) {
        super(GET_ARCHIVE_MODE, "/WEB-INF/view/configcreator/getArchive.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        setNewSessionData(request);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getSessionData(request);
        FileItem fileItem = (FileItem) getUploadFiles().get(MODULE_URI_PARAMETER);

        String fileName = fileItem.getName();
        if (fileName == null || fileName.length() <= 0) {
            return getMode();
        }

        // TODO Is there a better way of checking whether the archive is a WAR or not?
        int i = fileName.length() - 4;
        if (!fileName.substring(i).equalsIgnoreCase(".war")) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "errorMsg01"));
            return getMode();
        }

        File uploadedFile = uploadFile(fileItem);
        data.setUploadedWarUri(uploadedFile.toURL().toString());

        String str = getBasename(fileItem.getName().trim());
        String warName = str.substring(0, str.length() - 4);
        data.setContextRoot(warName);
        data.setGroupId("default");
        data.setArtifactId(warName);
        data.setVersion("1.0");
        data.setType("war");
        data.setHiddenClasses("");
        data.setNonOverridableClasses("");
        data.setInverseClassLoading(false);

        try {
            JSR88_Util.parseWarReferences(request, data, uploadedFile.toURL());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return getMode();
        }

        return ENVIRONMENT_MODE + "-before";
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
