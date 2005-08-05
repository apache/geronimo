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

package org.apache.geronimo.console.securitymanager;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

public abstract class AbstractSecurityManagerPortlet extends GenericPortlet {

    protected PortletRequestDispatcher normalView;

    protected PortletRequestDispatcher addNormalView;

    protected PortletRequestDispatcher maximizedView;

    protected PortletRequestDispatcher addMaximizedView;

    protected PortletRequestDispatcher helpView;

    protected PortletRequestDispatcher errorView;

    protected void doEdit(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            addNormalView.include(renderRequest, renderResponse);
        } else {
            addMaximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

}
