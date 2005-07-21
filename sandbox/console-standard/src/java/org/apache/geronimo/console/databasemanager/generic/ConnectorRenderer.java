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

package org.apache.geronimo.console.databasemanager.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.databasemanager.DetailViewRenderer;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;

public class ConnectorRenderer implements DetailViewRenderer {
    private static final Set HIDDEN_ATTRIBUTES;

    static {
        HIDDEN_ATTRIBUTES = new HashSet();
        HIDDEN_ATTRIBUTES.add("kernel");
        HIDDEN_ATTRIBUTES.add("connectionImplClass");
        HIDDEN_ATTRIBUTES.add("connectionInterface");
        HIDDEN_ATTRIBUTES.add("connectionFactoryInterface");
        HIDDEN_ATTRIBUTES.add("connectionFactoryImplClass");
        HIDDEN_ATTRIBUTES.add("implementedInterfaces");
        HIDDEN_ATTRIBUTES.add("managedConnectionFactoryClass");
        HIDDEN_ATTRIBUTES.add("recoveryXAResources");
    }

    private final Kernel kernel;

    private final PortletRequestDispatcher normalView;

    private final PortletRequestDispatcher configView;

    public ConnectorRenderer(Kernel kernel, PortletContext context) {
        this.kernel = kernel;
        normalView = context
                .getRequestDispatcher("/WEB-INF/view/databasemanager/generic/normal.jsp");
        configView = context
                .getRequestDispatcher("/WEB-INF/view/databasemanager/generic/config.jsp");
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse, ObjectName gbeanName)
            throws PortletException, IOException {
        throw new UnsupportedOperationException();
    }

    public void render(RenderRequest request, RenderResponse response,
            ObjectName gbeanName) throws PortletException, IOException {
        try {
            GBeanInfo gbeanInfo = kernel.getGBeanInfo(gbeanName);
            Set attributes = gbeanInfo.getAttributes();
            Map values = new HashMap(attributes.size());
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                GAttributeInfo attribute = (GAttributeInfo) i.next();
                String name = attribute.getName();
                if (HIDDEN_ATTRIBUTES.contains(name)) {
                    continue;
                }
                Object value = kernel.getAttribute(gbeanName, name);
                values.put(name, value);
            }
            request.setAttribute("attributeMap", values);
        } catch (Exception e) {
            throw new PortletException(e);
        }
        if ("config".equals(request.getParameter("mode"))) {
            configView.include(request, response);
        } else {
            normalView.include(request, response);
        }
    }
}
