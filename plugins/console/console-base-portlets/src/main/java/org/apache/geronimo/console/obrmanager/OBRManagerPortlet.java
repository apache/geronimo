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
import org.apache.felix.bundlerepository.Resource;
import org.apache.geronimo.console.BasePortlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBRManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(OBRManagerPortlet.class);

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher obrManagerView;

    private static final String SERACH_ACTION = "search";

    private static final String LIST_ACTION = "listAll";

    private static final String REMOVE_URL_ACTION = "removeurl";

    private static final String REFRESH_URL_ACTION = "refreshurl";

    private static final String ADD_URL_ACTION = "add_url";

    private static final String SEARCH_TYPE_ALL = "ALL";

    private static final String SEARCH_TYPE_SEARCH = "SEARCH";

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
        actionResponse.setRenderParameter("message", ""); // set to blank first
        
        BundleContext bundleContext = getBundleContext(actionRequest);
        ServiceReference reference = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(reference);
        
        String action = actionRequest.getParameter("action");


        if (ADD_URL_ACTION.equals(action)) {
            String obrUrl = actionRequest.getParameter("obrUrl");

            try {
                repositoryAdmin.addRepository(new URI(obrUrl).toURL());
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.obrmanager.info.add", obrUrl));
            } catch (Exception e) {
                addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.obrmanager.err.actionError") + action, e.getMessage());
                logger.error("Exception", e);
            }
        }
        else if (REFRESH_URL_ACTION.equals(action)) {
            String uri = actionRequest.getParameter("uri");

            try {
                repositoryAdmin.removeRepository(uri);
                repositoryAdmin.addRepository(uri);
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.obrmanager.info.refreshurl", uri));
            } catch (Exception e) {
                addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.obrmanager.err.actionError") + action, e.getMessage());
                logger.error("Exception", e);
            }
        }
        else if (REMOVE_URL_ACTION.equals(action)) {
            String uri = actionRequest.getParameter("uri");
            repositoryAdmin.removeRepository(uri);
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.obrmanager.info.removeurl", uri));
        }
        else if (LIST_ACTION.equals(action)) {
            String searchType = SEARCH_TYPE_ALL;
            actionResponse.setRenderParameter("searchType", searchType);
        }
        else if (SERACH_ACTION.equals(action)) {
            String searchType = SEARCH_TYPE_SEARCH;
            String searchString = actionRequest.getParameter("searchString");
            actionResponse.setRenderParameter("searchType", searchType);
            actionResponse.setRenderParameter("searchString", searchString);            
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

            //get All OBR
            Repository[] repos = repositoryAdmin.listRepositories();
            renderRequest.setAttribute("repos", repos);

            String searchType = renderRequest.getParameter("searchType");
            if (searchType != null && !"".equals(searchType)) {
                if (SEARCH_TYPE_ALL.equals(searchType)) {
                    Repository[] repoAll = repositoryAdmin.listRepositories();
                    List<Resource> resourcesResult = new ArrayList<Resource>();
                    
                    for (Repository repo : repoAll) {
                        Resource[] resources = repo.getResources();
                        for (Resource reso : resources) {
                            resourcesResult.add(reso);
                        }
                    }
                    renderRequest.setAttribute("resources", resourcesResult);
                    
                }
                else if (SEARCH_TYPE_SEARCH.equals(searchType)) {
                    try {
                        String searchString = renderRequest.getParameter("searchString");
                        StringBuffer sb = new StringBuffer();
                        
                        if (searchString==null||"".equals(searchString)) {
                            sb.append("(|(presentationname=*)(symbolicname=*))");
                        }
                        else {
                            sb.append("(|(presentationname=*");
                            sb.append(searchString);
                            sb.append("*)(symbolicname=*");
                            sb.append(searchString);
                            sb.append("*))");
                        }
                        
                        Resource[] resources = repositoryAdmin.discoverResources(sb.toString());
                        renderRequest.setAttribute("resources", resources);
                    } catch (Exception e) {
                        addErrorMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.obrmanager.err.actionError"), e.getMessage());
                        logger.error("Exception", e);
                     } finally {
                         bundleContext.ungetService(reference);
                     }
                }
            }
            obrManagerView.include(renderRequest, renderResponse);
        }

    }

    private BundleContext getBundleContext(PortletRequest request) {
        return (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
    }
}
