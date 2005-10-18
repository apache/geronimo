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

package org.apache.geronimo.console.certmanager;

import java.io.IOException;
import java.util.Enumeration;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.certmanager.actions.ChangeStorePassword;
import org.apache.geronimo.console.certmanager.actions.GenerateCSR;
import org.apache.geronimo.console.certmanager.actions.GenerateKeyPair;
import org.apache.geronimo.console.certmanager.actions.ImportCAReply;
import org.apache.geronimo.console.certmanager.actions.ImportTrustedCertificate;
import org.apache.geronimo.console.certmanager.actions.UploadCertificateFile;
import org.apache.geronimo.console.certmanager.actions.ViewKeyStore;
import org.apache.geronimo.console.certmanager.actions.ViewKeyStoreEntryDetail;
import org.apache.geronimo.console.util.ObjectNameConstants;

public class CertManagerPortlet extends GenericPortlet {

    private PortletContext ctx;

    private ObjectName ksobjname;

    public CertManagerPortlet() {
        this.ctx = null;
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        // iniitialize portlet environment
        this.ctx = portletConfig.getPortletContext();

        try {
            this.ksobjname = new ObjectName(
                    ObjectNameConstants.KEYSTORE_OBJ_NAME);
        } catch (Exception e) {
            throw new PortletException(e);
        }

        this.ctx.log("Certificate manager portlet initialized");
    }

    public ObjectName getKeyStoreObjectName() {
        return ksobjname;
    }

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

        String action = request.getParameter("action");
        ctx.log("process-action: action = " + action);

        if (action == null) {
            return;
        }

        // pass 'action' parameter value to render method
        response.setRenderParameter("action", action);

        if (action.equals("upload-certificate-file")) {
            UploadCertificateFile.action(this, request, response);
        } else if (action.equals("import-trusted-certificate")) {
            ImportTrustedCertificate.action(this, request, response);
        } else if (action.equals("tools-generate-key-pair")) {
            GenerateKeyPair.action(this, request, response);
        } else if (action.equals("tools-change-keystore-password")) {
            ChangeStorePassword.action(this, request, response);
        } else if (action.equals("generate-csr")) {
            GenerateCSR.action(this, request, response);
        } else if (action.equals("import-ca-reply")) {
            ImportCAReply.action(this, request, response);
        } else if (action.equals("save-pkcs7-cert")) {
            ImportCAReply.action(this, request, response);
        } else if (action.equals("generate-key-pair")) {
            GenerateKeyPair.action(this, request, response);
        }
    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        PortletRequestDispatcher prd = null;

        String action = request.getParameter("action");
        ctx.log("do-view: action = " + action);

        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String pname = (String) e.nextElement();
            String value = request.getParameter(pname);
            ctx.log("param-name = " + pname + ", param-value = " + value);
        }

        if (action == null) {
            ViewKeyStore.render(this, request, response);
        } else if (action.equals("tools-import-trusted-certificate")) {
            ImportTrustedCertificate.render(this, request, response);
        } else if (action.equals("tools-generate-key-pair")) {
            GenerateKeyPair.render(this, request, response);
        } else if (action.equals("tools-change-keystore-password")) {
            ChangeStorePassword.render(this, request, response);
        } else if (action.equals("upload-certificate-file")) {
            UploadCertificateFile.render(this, request, response);
        } else if (action.equals("import-trusted-certificate")) {
            ImportTrustedCertificate.render(this, request, response);
        } else if (action.equals("view-keystore-entry-details")) {
            ViewKeyStoreEntryDetail.render(this, request, response);
        } else if (action.equals("generate-csr")) {
            GenerateCSR.render(this, request, response);
        } else if (action.equals("import-ca-reply")) {
            ImportCAReply.render(this, request, response);
        } else if (action.equals("save-pkcs7-cert")) {
            ImportCAReply.render(this, request, response);
        } else if (action.equals("generate-key-pair")) {
            GenerateKeyPair.render(this, request, response);
        }
    }

    public void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        PortletRequestDispatcher prd = ctx
                .getRequestDispatcher("/WEB-INF/view/certmanager/viewKeyStoreHelp.jsp");
        prd.include(renderRequest, renderResponse);
    }
}
