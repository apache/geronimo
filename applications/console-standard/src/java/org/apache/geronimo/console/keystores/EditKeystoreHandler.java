/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.console.keystores;

import org.apache.geronimo.console.MultiPageModel;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for entering a password to allow editing of a keystore
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class EditKeystoreHandler extends BaseKeystoreHandler {
    public EditKeystoreHandler() {
        super(UNLOCK_KEYSTORE_FOR_EDITING, "/WEB-INF/view/keystore/unlockKeystore.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        if(keystore != null) {
            response.setRenderParameter("keystore", keystore);
        } // else we hope this is after a failure and the actionAfterView took care of it below!
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        request.setAttribute("keystore", request.getParameter("keystore"));
        request.setAttribute("mode", "unlockEdit");
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        String password = request.getParameter("password");
        if(keystore == null || keystore.equals("")) {
            return getMode(); // todo: this is bad; if there's no ID, then the form on the page is just not valid!
        } else if(password == null) {
            response.setRenderParameter("keystore", keystore);
            return getMode();
        }
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        char[] storePass = password.toCharArray();
        data.setPassword(storePass);
        data.setCertificates(data.getInstance().listTrustCertificates(storePass));
        data.setKeys(data.getInstance().listPrivateKeys(storePass));
        return LIST_MODE+BEFORE_ACTION;
    }
}
