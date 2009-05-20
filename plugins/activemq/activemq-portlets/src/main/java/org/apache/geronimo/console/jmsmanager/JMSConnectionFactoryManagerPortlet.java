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

package org.apache.geronimo.console.jmsmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.jmsmanager.DataSourceInfo;
import org.apache.geronimo.console.jmsmanager.activemqCF.ActiveMQConnectorHelper;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.ObjectNameUtil;

public class JMSConnectionFactoryManagerPortlet extends BasePortlet {

    private final static ActiveMQConnectorHelper helper = new ActiveMQConnectorHelper();

    private final static String PARENT_ID = "geronimo/activemq-broker/" + org.apache.geronimo.system.serverinfo.ServerConstants.getVersion() + "/car";

    private final static String ADD_MODE = "addACF";

    private final static String SUBMIT_CREATE = "Create";

    private final ObjectName DATABASE_QUERY = ObjectNameUtil
            .getObjectName("*:j2eeType=JCAManagedConnectionFactory,*");

    protected final String NORMAL_VIEW = "/WEB-INF/view/jmsmanager/activemq/normal.jsp";

    protected final String DETAIL_VIEW = "/WEB-INF/view/jmsmanager/activemq/detail.jsp";

    protected final String HELP_VIEW = "/WEB-INF/view/jmsmanager/activemq/help.jsp";

    protected final String ADD_VIEW = "/WEB-INF/view/jmsmanager/activemq/addACF.jsp";

    protected Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher detailView;

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher addView;

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

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        PortletContext context = portletConfig.getPortletContext();
        normalView = context.getRequestDispatcher(NORMAL_VIEW);
        detailView = context.getRequestDispatcher(DETAIL_VIEW);
        helpView = context.getRequestDispatcher(HELP_VIEW);
        addView = context.getRequestDispatcher(ADD_VIEW);
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter("mode");
        String submit = actionRequest.getParameter("submit");

        if (mode == null) {
            return;
        } else if (ADD_MODE.equals(mode)) {
            actionResponse.setRenderParameter("mode", mode);
            if (SUBMIT_CREATE.equals(submit)) {
                String acfName = actionRequest.getParameter("acfName");
                String serverURL = actionRequest.getParameter("serverURL");
                String userName = actionRequest.getParameter("userName");
                String pword = actionRequest.getParameter("pword");
                String poolMaxSize = actionRequest.getParameter("poolMaxSize");
                String blocking = actionRequest.getParameter("blocking");

                String[] args = { trimStr(acfName), trimStr(PARENT_ID),
                        trimStr(acfName), trimStr(serverURL),
                        trimStr(userName), pword, trimStr(acfName),
                        trimStr(poolMaxSize), trimStr(blocking) };
                helper.deployPlan(actionRequest, args);
                // Set mode to list after creating the new ConnectionFactories
                actionResponse.setRenderParameter("mode", "list");
            }
            return;
        }

        String name = actionRequest.getParameter("name");
        if (name != null) {
            actionResponse.setRenderParameter("mode", "list");
            return;
        }
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        List dependencies = helper.getDependencies(renderRequest);
        // pass them to the render request
        renderRequest.setAttribute("dependencies", (String[]) dependencies
                .toArray(new String[dependencies.size()]));

        String name = renderRequest.getParameter("name");
        String mode = renderRequest.getParameter("mode");
        String check = renderRequest.getParameter("check");

        if (ADD_MODE.equals(mode)) {
            addView.include(renderRequest, renderResponse);
            return;
        }

        boolean test = false;
        if (name == null || mode == null) {
            mode = "list";
        }
        if ("true".equals(check)) {
            test = true;
        }
        if ("detail".equals(mode) || "config".equals(mode)) {
            renderDetail(renderRequest, renderResponse, name);
        } else {
            renderList(renderRequest, renderResponse, name, test);
        }
    }

    private void renderList(RenderRequest renderRequest,
            RenderResponse renderResponse, String name, boolean check)
            throws PortletException, IOException {

        Set gbeanNames = kernel.listGBeans(DATABASE_QUERY);
        List connectionFactories = new ArrayList(gbeanNames.size());
        for (Iterator i = gbeanNames.iterator(); i.hasNext();) {
            ObjectName gbeanName = (ObjectName) i.next();

            // check that this connector is a DataSource
            try {
                Class cfInterface = Class.forName((String) kernel.getAttribute(
                        gbeanName, "connectionFactoryInterface"));
                if (!(ConnectionFactory.class).isAssignableFrom(cfInterface)) {
                    continue;
                }
            } catch (Exception e) {
                throw new PortletException(e);
            }

            DataSourceInfo info = new DataSourceInfo();
            info.setObjectName(gbeanName);
            info.setName(gbeanName.getKeyProperty("name"));
            try {
                info.setState(new Integer(kernel.getGBeanState(gbeanName)));
                //check if user asked this connection to be tested
                if ((gbeanName.toString().equals(name)) && (check)) {
                    info.setWorking(true);
                    try {
                        Object cf = kernel.invoke(gbeanName, "$getResource");
                        testConnection(cf);
                        info.setMessage("Connected");
                    } catch (Exception e) {
                        Throwable t = e;
                        String message = "Failed: ";
                        if (t.getMessage() != null) {
                            message = message + t.getMessage();
                        } else {
                            while (t.getMessage() == null) {
                                t = t.getCause();
                                if (t != null) {
                                    message = message + t.getMessage();
                                } else {
                                    message = message + "Unknown reason";
                                }
                            }
                        }
                        info.setMessage(message);
                    }
                } else {
                    info.setWorking(false);
                }

            } catch (Exception e) {
                throw new PortletException(e);
            }
            connectionFactories.add(info);
        }
        Collections.sort(connectionFactories);
        renderRequest.setAttribute("cFactories", connectionFactories);

        normalView.include(renderRequest, renderResponse);
    }

    private void renderDetail(RenderRequest renderRequest,
            RenderResponse renderResponse, String name)
            throws PortletException, IOException {
        ObjectName gbeanName;

        try {
            gbeanName = new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new PortletException("Malformed parameter name: "
                    + renderRequest.getParameter("name"));
        }

        try {
            GBeanInfo gbeanInfo = kernel.getGBeanInfo(gbeanName);
            Set attributes = gbeanInfo.getAttributes();
            Map values = new HashMap(attributes.size());
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                GAttributeInfo attribute = (GAttributeInfo) i.next();
                String gname = attribute.getName();
                if (HIDDEN_ATTRIBUTES.contains(gname)) {
                    continue;
                }
                Object value = kernel.getAttribute(gbeanName, gname);
                values.put(gname, value);
            }
            renderRequest.setAttribute("attributeMap", values);
        } catch (Exception e) {
            throw new PortletException(e);
        }

        detailView.include(renderRequest, renderResponse);
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void destroy() {
        normalView = null;
        helpView = null;
        addView = null;
        kernel = null;
        super.destroy();
    }

    private String trimStr(String str) {
        if (str != null) {
            return str.trim();
        }
        return "";
    }

    protected void testConnection(Object cf) throws Exception {
        ConnectionFactory jmscf = (ConnectionFactory) cf;
        Connection c = jmscf.createConnection();
        c.close();
    }

}
