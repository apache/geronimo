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
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.KeystoreException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for entering a password to unlock a key
 *
 * @version $Rev$ $Date$
 */
public class UnlockKeyHandler extends BaseKeystoreHandler {
    private static final Logger log = LoggerFactory.getLogger(UnlockKeyHandler.class);
    
    public UnlockKeyHandler() {
        super(UNLOCK_KEY, "/WEB-INF/view/keystore/unlockKey.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
        String keystore = request.getParameter("keystore");
        String password = request.getParameter("password");
        request.setAttribute("keystore", keystore);
        request.setAttribute("password", password);
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        try {
            request.setAttribute("keys", data.getInstance().listPrivateKeys(password.toCharArray()));
        } catch (KeystoreException e) {
            throw new PortletException(e);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        String password = request.getParameter("password");
        String alias = request.getParameter("keyAlias");
        String keyPassword = request.getParameter("keyPassword");
        if(keystore == null || keystore.equals("")) {
            return getMode(); // todo: this is bad; if there's no ID, then the form on the page is just not valid!
        }
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        try {
            data.unlockPrivateKey(alias, keyPassword.toCharArray());
        } catch (KeystoreException e) {
            response.setRenderParameter("keystore", keystore);
            response.setRenderParameter("password", password);
            response.setRenderParameter(ERROR_MSG, "Unable to unlock key '"+alias+"'." + e);
            log.error("Unable to unlock key '"+alias+"'.", e);
            return getMode()+BEFORE_ACTION;
        }
        response.setRenderParameter(INFO_MSG, "Successfully unlocked key '"+alias+"' in keystore '"+keystore+"'.");
        return LIST_MODE+BEFORE_ACTION;
    }
}
