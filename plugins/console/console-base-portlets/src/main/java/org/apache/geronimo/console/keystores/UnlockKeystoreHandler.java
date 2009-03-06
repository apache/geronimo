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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for entering a password to unlock a keystore
 *
 * @version $Rev$ $Date$
 */
public class UnlockKeystoreHandler extends BaseKeystoreHandler {
    private static final Logger log = LoggerFactory.getLogger(UnlockKeystoreHandler.class);
    
    public UnlockKeystoreHandler(BasePortlet portlet) {
        super(UNLOCK_KEYSTORE_FOR_USAGE, "/WEB-INF/view/keystore/unlockKeystore.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        if(keystore != null) {
            response.setRenderParameter("keystore", keystore);
        } // else we hope this is after a failure and the actionAfterView took care of it below!
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        request.setAttribute("keystore", keystore);
        request.setAttribute("mode", "unlockKeystore");
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        request.setAttribute("keys", data.getKeys());
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        String password = request.getParameter("password");
        String alias = request.getParameter("keyAlias");
        String keyPassword = request.getParameter("keyPassword");
        if(keystore == null || keystore.equals("")) {
            return getMode(); // todo: this is bad; if there's no ID, then the form on the page is just not valid!
        } else if(password == null) {
            response.setRenderParameter("keystore", keystore);
            return getMode();
        }
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        char[] storePass = password.toCharArray();
        try {
            data.unlockUse(storePass);
            if(data.getKeys() != null && data.getKeys().length > 0) {
                // if it's unlocked for editing and has keys
                data.unlockPrivateKey(alias, keyPassword.toCharArray());
            } else if (data.getInstance().listPrivateKeys(storePass) != null && data.getInstance().listPrivateKeys(storePass).length > 0) {
                // if it's locked for editing but has keys
                response.setRenderParameter("keystore", keystore);
                response.setRenderParameter("password", password);
                return UNLOCK_KEY+BEFORE_ACTION;
            } // otherwise it has no keys
        } catch (Exception e) {
        	portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg09", keystore), e.getMessage());
            log.error("Unable to unlock keystore '"+keystore+"' for availability.", e);
            return getMode()+BEFORE_ACTION;
        }
        portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg07", keystore));
        return LIST_MODE+BEFORE_ACTION;
    }
}
