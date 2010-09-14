/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.ejbserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;

public class EJBServerPortlet extends BasePortlet {
	

	    private static final String VIEW_JSP = "/WEB-INF/view/ejbserver/view.jsp";

	    private static final String HELPVIEW_JSP = "/WEB-INF/view/ejbserver/help.jsp";
	    
	    private PortletRequestDispatcher normalView;

	    private PortletRequestDispatcher helpView;
	    
	    private List treeList;

	    public void processAction(ActionRequest actionRequest,
	            ActionResponse actionResponse) throws PortletException, IOException {
	    }

	    protected void doView(RenderRequest renderRequest,
	            RenderResponse renderResponse) throws IOException, PortletException {
	    	
	    	if (WindowState.MAXIMIZED.equals(renderRequest.getWindowState())) {
	    		normalView.include(renderRequest, renderResponse);
	        } else if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
	            return;
	        } else if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
	            normalView.include(renderRequest, renderResponse);
	        } else {
	            return;
	        }
	    }

	    private List split(String path, String sep) {
	        StringTokenizer st = new StringTokenizer(path, sep);

	        List l = new ArrayList();

	        while (st.hasMoreTokens()) {
	            l.add(st.nextToken());
	        }
	        return l;
	    }

	    protected void doHelp(RenderRequest renderRequest,
	            RenderResponse renderResponse) throws PortletException, IOException {
	        helpView.include(renderRequest, renderResponse);
	    }

	    public void init(PortletConfig portletConfig) throws PortletException {
	        super.init(portletConfig);
	        normalView = portletConfig.getPortletContext().getRequestDispatcher(
	                VIEW_JSP);
	        helpView = portletConfig.getPortletContext().getRequestDispatcher(
	                HELPVIEW_JSP);
	    }

	    public void destroy() {
	        normalView = null;	        
	        helpView = null;
	        super.destroy();
	    }

}
