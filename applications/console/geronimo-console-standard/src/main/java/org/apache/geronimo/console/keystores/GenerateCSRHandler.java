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

/**
 * Handler for generating a Certificate Signing Request (CSR)
 *
 * @version $Rev$ $Date$
 */
public class GenerateCSRHandler extends BaseKeystoreHandler {
    public GenerateCSRHandler() {
        super(GENERATE_CSR, "/WEB-INF/view/keystore/generateCSR.jsp");
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
        request.setAttribute("id", id);
        request.setAttribute("alias", alias);
        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + id));
        try {
            String csr = data.generateCSR(alias);
            request.setAttribute("csr", csr);
        } catch (KeystoreException e) {
            throw new PortletException(e);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
    	String id = request.getParameter("id");
    	String alias = request.getParameter("alias");
        response.setRenderParameter("id", id);
        response.setRenderParameter("alias", alias);
        return CERTIFICATE_DETAILS+BEFORE_ACTION;
    }
}
