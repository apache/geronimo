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

public class GenerateCSR {
    public static void action(CertManagerPortlet portlet,
            ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        response.setRenderParameter("action", request.getParameter("action"));
    }

    public static void render(CertManagerPortlet portlet,
            RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        String alias = request.getParameter("alias");

        try {
            String csr = (String) KernelRegistry.getSingleKernel()
                    .invoke(portlet.getKeyStoreObjectName(), "generateCSR",
                            new Object[] { alias },
                            new String[] { "java.lang.String" });

            request.setAttribute("com.gluecode.se.cert.csr", csr);
            request.setAttribute("alias", alias);
        } catch (Exception e) {
            throw new PortletException(e);
        }

        PortletRequestDispatcher rd = portlet.getPortletContext()
                .getRequestDispatcher(
                        "/WEB-INF/view/certmanager/generateCSRNormal.jsp");

        rd.include(request, response);
    }
}
