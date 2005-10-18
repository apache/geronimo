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
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.certmanager.CertManagerPortlet;
import org.apache.geronimo.kernel.KernelRegistry;

public class ViewKeyStore {
    public static void action(CertManagerPortlet portlet,
            ActionRequest request, ActionResponse response)
            throws PortletException, Exception {
        response.setRenderParameter("action", request.getParameter("action"));
    }

    public static void render(CertManagerPortlet portlet,
            RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        PortletRequestDispatcher rd = null;

        try {
            ObjectName objname = portlet.getKeyStoreObjectName();

            String keyStoreType = (String) KernelRegistry.getSingleKernel()
                    .getAttribute(objname, "keyStoreType");
            String keyStoreProvider = (String) KernelRegistry.getSingleKernel()
                    .getAttribute(objname, "keyStoreProvider");
            String keyStoreLocation = (String) KernelRegistry.getSingleKernel()
                    .getAttribute(objname, "keyStoreLocation");

            request.setAttribute("com.gluecode.se.keystore.type", keyStoreType);
            request.setAttribute("com.gluecode.se.keystore.provider",
                    keyStoreProvider);
            request.setAttribute("com.gluecode.se.keystore.location",
                    keyStoreLocation);

            List storelist = (List) KernelRegistry.getSingleKernel().invoke(
                    objname, "getKeyStoreEntries");

            Iterator iter = storelist.iterator();
            while (iter.hasNext()) {
                portlet.getPortletContext().log("store-item = " + iter.next());
            }

            request.setAttribute("com.gluecode.se.keystore.list", storelist);
            request.setAttribute("com.gluecode.se.keystore.size", String
                    .valueOf(storelist.size()));
        } catch (Exception e) {
            throw new PortletException(e);
        }

        rd = portlet.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/certmanager/viewKeyStoreNormal.jsp");
        rd.include(request, response);
    }
}
