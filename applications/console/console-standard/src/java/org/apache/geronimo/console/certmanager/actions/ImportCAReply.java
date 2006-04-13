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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.certmanager.CertManagerPortlet;
import org.apache.geronimo.kernel.KernelRegistry;

public class ImportCAReply {
    public static void action(CertManagerPortlet portlet,
            ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

        // pass 'alias' parameter along
        String alias = request.getParameter("alias");
        response.setRenderParameter("alias", alias);

        String action = request.getParameter("action");

        // this should never happen
        if (action == null) {
            return;
        }

        if (action.equals("import-ca-reply")) {
            return;
        } else if (action.equals("save-pkcs7-cert")) {
            String submit = request.getParameter("submit");

            if (submit.equalsIgnoreCase("cancel")) {
                return;
            }

            // save pkcs7-encoded certificate
            String pkcs7cert = request.getParameter("pkcs7cert");

            try {
                KernelRegistry.getSingleKernel()
                        .invoke(
                                portlet.getKeyStoreObjectName(),
                                "importPKCS7Certificate",
                                new Object[] { alias, pkcs7cert },
                                new String[] { "java.lang.String",
                                        "java.lang.String" });
            } catch (Exception e) {
                throw new PortletException(e);
            }
        }
    }

    public static void render(CertManagerPortlet portlet,
            RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        String action = request.getParameter("action");
        String alias = request.getParameter("alias");

        // set alias name
        request.setAttribute("alias", alias);

        // this should never happen
        if (action == null) {
            ViewKeyStoreEntryDetail.render(portlet, request, response);
            return;
        }

        if (action.equals("import-ca-reply")) {
            PortletRequestDispatcher rd = portlet
                    .getPortletContext()
                    .getRequestDispatcher(
                            "/WEB-INF/view/certmanager/importCAReplyNormal.jsp");

            rd.include(request, response);
        } else if (action.equals("save-pkcs7-cert")) {
            ViewKeyStoreEntryDetail.render(portlet, request, response);
        }
    }
}
