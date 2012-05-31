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

package org.apache.geronimo.console.obrmanager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.geronimo.console.BasePortlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBRManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(OBRManagerPortlet.class);

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher obrManagerView;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/obrmanager/OBRManager.jsp");
        obrManagerView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/obrmanager/OBRManager.jsp");

    }

    public void destroy() {
        obrManagerView = null;
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
            IOException {

        String action = actionRequest.getParameter("action");

        if ("add_url".equals(action)) {
            String obrUrl = actionRequest.getParameter("obrUrl");

            BundleContext bundleContext = getBundleContext(actionRequest);
            ServiceReference reference = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
            RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(reference);

            try {
                repositoryAdmin.addRepository(new URI(obrUrl).toURL());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bundleContext.ungetService(reference);
            }

        }

    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {

        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) { // minimal view
            return;
        } else { // normal and maximal view

            BundleContext bundleContext = getBundleContext(renderRequest);
            ServiceReference reference = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
            RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(reference);

            Repository[] repos = repositoryAdmin.listRepositories();
            List<String> repoURIs = new ArrayList<String>();
            for (Repository repo : repos) {
                repoURIs.add(repo.getURI());
            }

            renderRequest.setAttribute("repoURIs", repoURIs);

            obrManagerView.include(renderRequest, renderResponse);
        }

    }

    private BundleContext getBundleContext(PortletRequest request) {
        return (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
    }
}
