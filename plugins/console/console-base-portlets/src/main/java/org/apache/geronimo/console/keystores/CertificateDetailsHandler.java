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

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.KeystoreException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.security.cert.Certificate;

/**
 * Handler for displaying  Trusted Certificate or Private Key Certificate details
 *
 * @version $Rev$ $Date$
 */
public class CertificateDetailsHandler extends BaseKeystoreHandler {
    public CertificateDetailsHandler() {
        super(CERTIFICATE_DETAILS, "/WEB-INF/view/keystore/certificateDetails.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("id");
        String alias = request.getParameter("alias");
        response.setRenderParameter("id", id);
        response.setRenderParameter("alias", alias);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("id");
        String alias = request.getParameter("alias");
        if(alias == null && request.getParameterMap().containsKey("alias")) {
            // Happens with an alias ""
            alias = "";
        }
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + id));
        Certificate cert;
        try {
            cert = data.getCertificate(alias);
        } catch (KeystoreException e) {
            throw new PortletException(e);
        }
        String type = "Trusted Certificate";
        boolean keyLocked = true;
        String[] keys = data.getKeys();
        for(int i = 0; i < keys.length; ++i) {
            if(keys[i].equals(alias)) {
                type = "Private Key";
                keyLocked = data.getInstance().isKeyLocked(alias);
            }
        }
        request.setAttribute("id", id);
        request.setAttribute("alias", alias);
        request.setAttribute("type", type);
        request.setAttribute("keyLocked", Boolean.valueOf(keyLocked));
        // TODO: Handle certificate chain
        request.setAttribute("certs", new Certificate[] {cert});
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("id");
        response.setRenderParameter("id", id);
        return VIEW_KEYSTORE+BEFORE_ACTION;
    }
}
