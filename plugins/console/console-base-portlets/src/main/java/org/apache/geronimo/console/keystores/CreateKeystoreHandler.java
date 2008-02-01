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
package org.apache.geronimo.console.keystores;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletSession;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.crypto.KeystoreUtil;

/**
 * Handler for creating a keystore
 *
 * @version $Rev$ $Date$
 */
public class CreateKeystoreHandler extends BaseKeystoreHandler {
    public CreateKeystoreHandler() {
        super(CREATE_KEYSTORE, "/WEB-INF/view/keystore/createKeystore.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        if(request.getParameter("filename") != null) {
            request.setAttribute("filename", request.getParameter("filename"));
        }
        request.setAttribute("keystoreTypes", KeystoreUtil.emptyKeystoreTypes);
        request.setAttribute("defaultType", KeystoreUtil.defaultType);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String filename = request.getParameter("filename");
        String password = request.getParameter("password");
        String type = request.getParameter("type");
        if(filename == null || filename.equals("")) {
            return getMode();
        } else if(password == null) {
            response.setRenderParameter("filename", filename);
            return getMode();
        }
        try {
            KeystoreInstance instance = PortletManager.getCurrentServer(request).getKeystoreManager().createKeystore(filename, password.toCharArray(), type);
            PortletSession session = request.getPortletSession(true);
            KeystoreData data = new KeystoreData();
            data.setInstance(instance);
            session.setAttribute(KEYSTORE_DATA_PREFIX+filename, data);
            char[] cpw = password.toCharArray();
            data.unlockEdit(cpw);
        } catch (KeystoreException e) {
            throw new PortletException(e);
        }
        return LIST_MODE+BEFORE_ACTION;
    }
}
