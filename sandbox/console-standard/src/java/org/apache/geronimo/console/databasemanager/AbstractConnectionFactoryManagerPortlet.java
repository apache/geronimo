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

package org.apache.geronimo.console.databasemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.databasemanager.connectionmanager.ConnectionManagerRenderer;
import org.apache.geronimo.console.databasemanager.generic.ConnectorRenderer;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public abstract class AbstractConnectionFactoryManagerPortlet extends
        GenericPortlet {

    private final static DatabaseManagerHelper helper = new DatabaseManagerHelper();

    private final static String PARENT_ID = "org/apache/geronimo/SystemDatabase";

    private final static String ADDDS_MODE = "addDS";

    private final static String SUBMIT_CREATE = "Create";

    private final static String DATASOURCE_MSSQL = "MSSQLDataSource2";

    protected static final Map RENDERERS = new HashMap();

    private final ObjectName DATABASE_QUERY;

    protected final String NORMAL_VIEW;

    protected final String HELP_VIEW;

    protected final String ADDDS_VIEW = "/WEB-INF/view/databasemanager/addDS.jsp";

    protected final Class connectionFactoryClass;

    protected Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher addDSView;

    private ConnectorRenderer connectorRenderer;

    private ConnectionManagerRenderer connectionManagerRenderer;

    public AbstractConnectionFactoryManagerPortlet(
            ObjectName ConnectionFactoryQuery, String NORMAL_VIEW,
            String HELP_VIEW, Class connectionFactoryClass) {
        this.DATABASE_QUERY = ConnectionFactoryQuery;
        this.NORMAL_VIEW = NORMAL_VIEW;
        this.HELP_VIEW = HELP_VIEW;
        this.connectionFactoryClass = connectionFactoryClass;
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter("mode");
        String submit = actionRequest.getParameter("submit");

        if (mode == null) {
            return;
        } else if (ADDDS_MODE.equals(mode)) {
            actionResponse.setRenderParameter("mode", mode);
            if (SUBMIT_CREATE.equals(submit)) {
                String dsName = actionRequest.getParameter("dsName");
                String jndiName = actionRequest.getParameter("jndiName");
                String dependency = actionRequest.getParameter("dependency");
                String driverClass = actionRequest.getParameter("driverClass");
                String jdbcUrl = actionRequest.getParameter("jdbcUrl");
                String dbUser = actionRequest.getParameter("dbUser");
                String dbPassword = actionRequest.getParameter("dbPassword");
                String dbProperties = actionRequest
                        .getParameter("dbProperties");
                String poolMaxSize = actionRequest.getParameter("poolMaxSize");
                String poolInitSize = actionRequest
                        .getParameter("poolInitSize");

                String[] args = { trimStr(dsName), trimStr(PARENT_ID),
                        trimStr(dependency), trimStr(dsName), trimStr(dbUser),
                        dbPassword, trimStr(driverClass), trimStr(jdbcUrl),
                        trimStr(poolMaxSize), trimStr(poolInitSize),
                        trimStr(jndiName), trimStr(dbProperties) };
                helper.deployPlan(args);
                // Set mode to list after creating the new datasource
                actionResponse.setRenderParameter("mode", "list");
            }
            return;
        }

        String name = actionRequest.getParameter("name");
        if (name != null) {
            ObjectName gbeanName = null;
            try {
                gbeanName = new ObjectName(name);
            } catch (MalformedObjectNameException e) {
                System.err.println("Invalid GBeanName: "
                        + actionRequest.getParameter("name"));
                actionResponse.setRenderParameter("mode", "list");
                return;
            }
            // Process action if not MSSQL datasource
            if (name.indexOf(DATASOURCE_MSSQL) == -1) {
                connectionManagerRenderer.processAction(actionRequest,
                        actionResponse, gbeanName);
            }
            getRenderer(gbeanName).processAction(actionRequest, actionResponse,
                    gbeanName);
        }
    }

    private String trimStr(String str) {
        if (str != null) {
            return str.trim();
        }

        return "";
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        List dependencies = helper.getDependencies();
        // pass them to the render request
        renderRequest.setAttribute("dependencies", (String[]) dependencies
                .toArray(new String[dependencies.size()]));

        String name = renderRequest.getParameter("name");
        String mode = renderRequest.getParameter("mode");
        String check = renderRequest.getParameter("check");

        if ("addDS".equals(mode)) {
            addDSView.include(renderRequest, renderResponse);
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
        List dataSources = new ArrayList(gbeanNames.size());
        for (Iterator i = gbeanNames.iterator(); i.hasNext();) {
            ObjectName gbeanName = (ObjectName) i.next();

            // check that this connector is a DataSource
            try {
                Class cfInterface = Class.forName((String) kernel.getAttribute(
                        gbeanName, "connectionFactoryInterface"));
                if (!connectionFactoryClass.isAssignableFrom(cfInterface)) {
                    continue;
                }
            } catch (Exception e) {
                throw new PortletException(e);
            }

            DataSourceInfo info = new DataSourceInfo();
            info.setObjectName(gbeanName);
            info.setName(gbeanName.getKeyProperty("name"));
            try {
                info.setJndiName((String) kernel.getAttribute(gbeanName,
                        "globalJNDIName"));
                info
                        .setState((Integer) kernel.getAttribute(gbeanName,
                                "state"));
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
            dataSources.add(info);
        }
        Collections.sort(dataSources);
        renderRequest.setAttribute("dataSources", dataSources);

        normalView.include(renderRequest, renderResponse);
    }

    protected abstract void testConnection(Object cf) throws Exception;

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
        connectionManagerRenderer.addConnectionManagerInfo(renderRequest,
                gbeanName);
        getRenderer(gbeanName).render(renderRequest, renderResponse, gbeanName);
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        PortletContext context = portletConfig.getPortletContext();
        normalView = context.getRequestDispatcher(NORMAL_VIEW);
        helpView = context.getRequestDispatcher(HELP_VIEW);
        addDSView = context.getRequestDispatcher(ADDDS_VIEW);

        connectorRenderer = new ConnectorRenderer(kernel, context);
        setUpExplicitRenderers(context);
        connectionManagerRenderer = new ConnectionManagerRenderer(kernel);
    }

    protected abstract void setUpExplicitRenderers(PortletContext context);

    private DetailViewRenderer getRenderer(ObjectName gbeanName)
            throws PortletException {
        DetailViewRenderer renderer;
        try {
            String clsName = (String) kernel.getAttribute(gbeanName,
                    "managedConnectionFactoryClass");
            // ???
            //Class mcfClass = Class.forName((String)
            // kernel.getAttribute(gbeanName, "managedConnectionFactoryClass"));
            //renderer = (DetailViewRenderer)
            // RENDERERS.get(mcfClass.getName());
            renderer = (DetailViewRenderer) RENDERERS.get(clsName);
            if (renderer == null) {
                renderer = connectorRenderer;
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return renderer;
    }

    public void destroy() {

        connectorRenderer = null;
        connectionManagerRenderer = null;
        normalView = null;
        kernel = null;
        RENDERERS.clear();
        super.destroy();
    }
}
