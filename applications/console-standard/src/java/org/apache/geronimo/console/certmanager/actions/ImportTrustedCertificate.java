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

package org.apache.geronimo.console.certmanager.actions;

import java.io.IOException;
import java.net.URLDecoder;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.certmanager.CertManagerPortlet;
import org.apache.geronimo.kernel.KernelRegistry;

public class ImportTrustedCertificate {
    public static void action(CertManagerPortlet portlet,
            ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            return;
        }

        // nothing to do
        if (action.equals("tools-generate-trusted-certificate")) {
            return;
        }

        String submit = request.getParameter("submit");

        if (submit.equalsIgnoreCase("cancel")) {
            return;
        }

        String certfileEnc = request
                .getParameter("com.gluecode.se.cert.file.enc");
        String alias = request.getParameter("alias");

        // decode certificate file name
        String certfile = URLDecoder.decode(certfileEnc, "UTF-8");

        // import certificate into the key store
        try {
            KernelRegistry.getSingleKernel().invoke(
                    portlet.getKeyStoreObjectName(),
                    "importTrustedX509Certificate",
                    new Object[] { alias, certfile },
                    new String[] { "java.lang.String", "java.lang.String" });
        } catch (Exception ex) {
            throw new PortletException(ex);
        }
    }

    public static void render(CertManagerPortlet portlet,
            RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        PortletRequestDispatcher rd = null;

        String action = request.getParameter("action");

        if (action.equals("tools-import-trusted-certificate")) {
            rd = portlet.getPortletContext().getRequestDispatcher(
                    "/WEB-INF/view/certmanager/importTrustedCertNormal.jsp");

            rd.include(request, response);
        } else {
            ViewKeyStore.render(portlet, request, response);
        }
    }
}
