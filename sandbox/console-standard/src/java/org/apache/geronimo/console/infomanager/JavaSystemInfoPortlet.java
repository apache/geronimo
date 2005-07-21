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

package org.apache.geronimo.console.infomanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

public class JavaSystemInfoPortlet extends GenericPortlet {

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/infomanager/javaSysNormal.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/infomanager/javaSysMaximized.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/infomanager/javaSysHelp.jsp";

    private static Map javaSysProps;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        javaSysProps = System.getProperties();
        renderRequest.setAttribute("javaSysProps", javaSysProps);

        String sep = (String) javaSysProps.get("path.separator");

        String cp = (String) javaSysProps.get("sun.boot.class.path");
        if (cp != null) {
            renderRequest.setAttribute("bootClassPathList", split(cp, sep));
        }
        cp = (String) javaSysProps.get("java.library.path");
        if (cp != null) {
            renderRequest.setAttribute("javaLibraryPath", split(cp, sep));
        }
        cp = (String) javaSysProps.get("java.class.path");
        if (cp != null) {
            renderRequest.setAttribute("javaClassPath", split(cp, sep));
        }

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
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
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

}