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

package org.apache.geronimo.console.internaldb;

import java.io.IOException;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;

public class RunSQLPortlet extends BasePortlet {

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/internaldb/runSQLNormal.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/internaldb/runSQLMaximized.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/internaldb/runSQLHelp.jsp";

    private static final String CREATEDB_ACTION = "Create";

    private static final String DELETEDB_ACTION = "Delete";

    private static final String RUNSQL_ACTION = "Run SQL";

    private static final String BACKUPDB_ACTION = "Backup";

    private static final String RESTOREDB_ACTION = "Restore";

    private static RunSQLHelper sqlHelper = new RunSQLHelper();

    private static DBViewerHelper dbHelper = new DBViewerHelper();

    private static String derbyHome = System.getProperty("derby.system.home");

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private Collection databases;

    private String action;

    private String createDB;

    private String deleteDB;

    private String useDB;

    private String backupDB;

    private String restoreDB;

    private String sqlStmts;

    private String actionResult;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        // Getting parameters here because it fails on doView()
        action = actionRequest.getParameter("action");
        createDB = actionRequest.getParameter("createDB");
        deleteDB = actionRequest.getParameter("deleteDB");
        useDB = actionRequest.getParameter("useDB");
        backupDB = actionRequest.getParameter("backupDB");
        restoreDB = actionRequest.getParameter("restoreDB");
        sqlStmts = actionRequest.getParameter("sqlStmts");
        actionResult = "";
        if (CREATEDB_ACTION.equals(action)) {
            actionResult = sqlHelper.createDB(createDB);
        } else if (DELETEDB_ACTION.equals(action)) {
            actionResult = sqlHelper.deleteDB(derbyHome, deleteDB);
        } else if (RUNSQL_ACTION.equals(action)) {
            actionResult = sqlHelper.runSQL(useDB, sqlStmts);
        } else if (BACKUPDB_ACTION.equals(action)) {
            actionResult = sqlHelper.backupDB(derbyHome, backupDB);
        } else if (RESTOREDB_ACTION.equals(action)) {
            actionResult = sqlHelper.restoreDB(derbyHome, restoreDB);
        }
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        String singleSelectStmt;
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            databases = dbHelper.getDerbyDatabases(derbyHome);
            renderRequest.setAttribute("databases", databases);
            if (RUNSQL_ACTION.equals(action)) {
                // check if it's a single Select statement
                if ((sqlStmts != null) && (sqlStmts.trim().indexOf(';') == -1)
                        && sqlStmts.trim().toUpperCase().startsWith("SELECT")
                        && RunSQLHelper.SQL_SUCCESS_MSG.equals(actionResult)) {
                    singleSelectStmt = sqlStmts.trim();
                    // set action result to blank so it won't display
                    actionResult = "";
                } else {
                    singleSelectStmt = "";
                }
                renderRequest.setAttribute("useDB", useDB);
                renderRequest
                        .setAttribute("singleSelectStmt", singleSelectStmt);
                renderRequest.setAttribute("ds", DerbyConnectionUtil
                        .getDataSource(useDB));
            }
            if ((action != null) && (action.trim().length() > 0)) {
                renderRequest.setAttribute("actionResult", actionResult);
                //set action to null so that subsequent renders of portlet
                // won't display
                //action result if there is no action to process
                action = null;
            }
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
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
        databases = dbHelper.getDerbyDatabases(derbyHome);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

}
