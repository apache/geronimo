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

package org.apache.geronimo.console.internaldb;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBViewerPortlet extends BasePortlet {

    private static final Logger log = LoggerFactory.getLogger(DBViewerPortlet.class);

    private static final int RDBMS_DERBY = 1;

    private static final int RDBMS_MSSQL = 2;

    private static final String HELPVIEW_JSP = "/WEB-INF/view/internaldb/dbViewerHelp.jsp";

    private static final String LISTDATABASES_JSP = "/WEB-INF/view/internaldb/listDatabases.jsp";

    private static final String LISTDATABASES_MAXIMIZEDVIEW_JSP = "/WEB-INF/view/internaldb/listDatabasesMaximized.jsp";

    private static final String LISTTABLES_JSP = "/WEB-INF/view/internaldb/listTables.jsp";

    private static final String LISTTABLES_MAXIMIZEDVIEW_JSP = "/WEB-INF/view/internaldb/listTablesMaximized.jsp";

    private static final String VIEWTABLECONTENTS_JSP = "/WEB-INF/view/internaldb/viewTableContents.jsp";

    private static final String VIEWTABLECONTENTS_MAXIMIZEDVIEW_JSP = "/WEB-INF/view/internaldb/viewTableContentsMaximized.jsp";

    private static final String LISTDB_ACTION = "listDatabases";

    private static final String LISTTBLS_ACTION = "listTables";

    private static final String VIEWTBLCONTENTS_ACTION = "viewTableContents";

    private static DBViewerHelper helper = new DBViewerHelper();

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher listDatabasesView;

    private PortletRequestDispatcher listTablesView;

    private PortletRequestDispatcher viewTableContentsView;

    private PortletRequestDispatcher listDatabasesMaximizedView;

    private PortletRequestDispatcher listTablesMaximizedView;

    private PortletRequestDispatcher viewTableContentsMaximizedView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        // getting parameters here because it fails on doView()
        String action = actionRequest.getParameter("action");
        String db = actionRequest.getParameter("db");
        String tbl = actionRequest.getParameter("tbl");
        String viewTables = actionRequest.getParameter("viewTables");
        String rdbms = actionRequest.getParameter("rdbms");
        // pass them to the render request
        if (action != null) {
            actionResponse.setRenderParameter("action", action);
        }
        if (db != null) {
            actionResponse.setRenderParameter("db", db);
        }
        if (tbl != null) {
            actionResponse.setRenderParameter("tbl", tbl);
        }
        if (viewTables != null) {
            actionResponse.setRenderParameter("viewTables", viewTables);
        }
        if (rdbms != null) {
            actionResponse.setRenderParameter("rdbms", rdbms);
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String action = renderRequest.getParameter("action");
        String db = renderRequest.getParameter("db");
        String tbl = renderRequest.getParameter("tbl");
        String viewTables = renderRequest.getParameter("viewTables");
        String rdbms = renderRequest.getParameter("rdbms");
        int rdbmsParam = (rdbms == null ? RDBMS_DERBY : Integer.parseInt(rdbms));

        if (rdbmsParam == RDBMS_DERBY) {
            // Check is database & table is valid
            if (LISTTBLS_ACTION.equals(action)
                    || VIEWTBLCONTENTS_ACTION.equals(action)) {
                if (!helper.isDBValid(DerbyConnectionUtil.getDerbyHome(), db)) {
                    // DB not valid
                    log.error("Database is not valid: " + db);
                    action = "";
                }
            }
            if (VIEWTBLCONTENTS_ACTION.equals(action)) {
                if (!helper.isTblValid(db, tbl)) {
                    // Table not valid
                    log.error("Table is not valid: " + tbl);
                    action = "";
                }
            }
        }

        renderRequest.setAttribute("rdbms", rdbms);
        if (LISTTBLS_ACTION.equals(action)) {
            renderRequest.setAttribute("db", db);
            renderRequest.setAttribute("viewTables", viewTables);
            renderRequest.setAttribute("ds", DerbyConnectionUtil
                    .getDataSourceForDataBaseName(db));

            if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            	listTablesView.include(renderRequest, renderResponse);
            } else {
            	listTablesMaximizedView.include(renderRequest, renderResponse);
            }
        } else if (VIEWTBLCONTENTS_ACTION.equals(action)) {
            renderRequest.setAttribute("db", db);
            renderRequest.setAttribute("tbl", tbl);
            renderRequest.setAttribute("viewTables", viewTables);
            renderRequest.setAttribute("ds", DerbyConnectionUtil
                    .getDataSourceForDataBaseName(db));
            if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            	viewTableContentsView.include(renderRequest, renderResponse);
            } else {
            	viewTableContentsMaximizedView.include(renderRequest, renderResponse);
            }
        } else {
            renderRequest.setAttribute("databases", helper
                    .getDerbyDatabases(DerbyConnectionUtil.getDerbyHome()));
            if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            	listDatabasesView.include(renderRequest, renderResponse);
            } else {
            	listDatabasesMaximizedView.include(renderRequest, renderResponse);
            }
        }
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
        listDatabasesView = portletConfig.getPortletContext()
                .getRequestDispatcher(LISTDATABASES_JSP);
        listTablesView = portletConfig.getPortletContext()
                .getRequestDispatcher(LISTTABLES_JSP);
        viewTableContentsView = portletConfig.getPortletContext()
                .getRequestDispatcher(VIEWTABLECONTENTS_JSP);
        listDatabasesMaximizedView = portletConfig.getPortletContext()
		        .getRequestDispatcher(LISTDATABASES_MAXIMIZEDVIEW_JSP);
		listTablesMaximizedView = portletConfig.getPortletContext()
		        .getRequestDispatcher(LISTTABLES_MAXIMIZEDVIEW_JSP);
		viewTableContentsMaximizedView = portletConfig.getPortletContext()
		        .getRequestDispatcher(VIEWTABLECONTENTS_MAXIMIZEDVIEW_JSP);
    }

    public void destroy() {
        helpView = null;
        listDatabasesView = null;
        listTablesView = null;
        viewTableContentsView = null;
        listDatabasesMaximizedView = null;
        listTablesMaximizedView = null;
        viewTableContentsMaximizedView = null;
        super.destroy();
    }

}