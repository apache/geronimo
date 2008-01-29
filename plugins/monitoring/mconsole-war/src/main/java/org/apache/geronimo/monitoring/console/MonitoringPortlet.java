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
package org.apache.geronimo.monitoring.console;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.monitoring.console.util.DBManager;
import org.apache.geronimo.util.EncryptionManager;

/**
 * STATS
 */
public class MonitoringPortlet extends GenericPortlet {

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/monitoringNormal.jsp";

    private static final String VIEWVIEWS_JSP = "/WEB-INF/view/monitoringViews.jsp";

    private static final String VIEWSERVERS_JSP = "/WEB-INF/view/monitoringServers.jsp";

    private static final String VIEWGRAPHS_JSP = "/WEB-INF/view/monitoringGraphs.jsp";

    private static final String PAGEVIEW_JSP = "/WEB-INF/view/monitoringPage.jsp";

    private static final String EDITVIEW_JSP = "/WEB-INF/view/monitoringEditView.jsp";

    private static final String ADDVIEW_JSP = "/WEB-INF/view/monitoringAddView.jsp";

    private static final String ADDGRAPH_JSP = "/WEB-INF/view/monitoringAddGraph.jsp";

    private static final String EDITGRAPH_JSP = "/WEB-INF/view/monitoringEditGraph.jsp";

    private static final String VIEWSERVER_JSP = "/WEB-INF/view/monitoringViewServer.jsp";

    private static final String EDITSERVER_JSP = "/WEB-INF/view/monitoringEditServer.jsp";

    private static final String ADDSERVER_JSP = "/WEB-INF/view/monitoringAddServer.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/monitoringHelp.jsp";

    private static final String EDITNORMALVIEW_JSP = "/WEB-INF/view/monitoringEdit.jsp";

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher viewViews;

    private PortletRequestDispatcher viewServers;

    private PortletRequestDispatcher viewGraphs;

    private PortletRequestDispatcher pageView;

    private PortletRequestDispatcher editView;

    private PortletRequestDispatcher addView;

    private PortletRequestDispatcher addGraph;

    private PortletRequestDispatcher editGraph;

    private PortletRequestDispatcher viewServer;

    private PortletRequestDispatcher editServer;

    private PortletRequestDispatcher addServer;

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher editNormalView;

    @Override
    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        actionResponse.setRenderParameter("action", action);
        if (action.equals("showView")) {
            String view_id = actionRequest.getParameter("view_id");
            actionResponse.setRenderParameter("view_id", view_id);
        } else if (action.equals("showAllViews")) {
            // no parameters needed to be redirected to doView()
        } else if (action.equals("showAllServers")) {
            // no parameters needed to be redirected to doView()
        } else if (action.equals("showAllGraphs")) {
            // no parameters needed to be redirected to doView()
        } else if (action.equals("showEditView")) {
            String view_id = actionRequest.getParameter("view_id");
            actionResponse.setRenderParameter("view_id", view_id);
        } else if (action.equals("saveEditView")) {
            updateView(actionRequest, actionResponse);
        } else if (action.equals("showAddView")) {
            // no parameters needed to be redirected to doView()
        } else if (action.equals("saveAddView")) {
            addView(actionRequest, actionResponse);
        } else if (action.equals("showAddGraph")) {
            String server_id = actionRequest.getParameter("server_id");
            if (server_id != null)
                actionResponse.setRenderParameter("server_id", server_id);

            String mbean = actionRequest.getParameter("mbean");
            if (mbean != null)
                actionResponse.setRenderParameter("mbean", mbean);

            String dataname = actionRequest.getParameter("dataname");
            if (dataname != null)
                actionResponse.setRenderParameter("dataname", dataname);
        } else if (action.equals("saveAddGraph")) {
            addGraph(actionRequest, actionResponse);
        } else if (action.equals("showEditGraph")) {
            String graph_id = actionRequest.getParameter("graph_id");
            actionResponse.setRenderParameter("graph_id", graph_id);
        } else if (action.equals("saveEditGraph")) {
            updateGraph(actionRequest, actionResponse);
        } else if (action.equals("deleteGraph")) {
            deleteGraph(actionRequest, actionResponse);
        } else if (action.equals("deleteView")) {
            deleteView(actionRequest, actionResponse);
        } else if (action.equals("showServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("showEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("saveEditServer")) {
            updateServer(actionRequest, actionResponse);
        } else if (action.equals("showAddServer")) {
            // no parameters needed to be redirected to doView()
        } else if (action.equals("deleteServer")) {
            deleteServer(actionRequest, actionResponse);
        } else if (action.equals("saveAddServer")) {
            addServer(actionRequest, actionResponse);
        } else if (action.equals("startTrackingMbean")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
            String mbean = actionRequest.getParameter("mbean");
            actionResponse.setRenderParameter("mbean", mbean);
        } else if (action.equals("stopTrackingMbean")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
            String mbean = actionRequest.getParameter("mbean");
            actionResponse.setRenderParameter("mbean", mbean);
        } else if (action.equals("stopThread")
                || action.equals("disableServerViewQuery")) {
            String server_id = actionRequest.getParameter("server_id");
            String message = stopThread(server_id);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("message", message);
        } else if (action.equals("startThread")
                || action.equals("enableServerViewQuery")) {
            String server_id = actionRequest.getParameter("server_id");
            String snapshotDuration = actionRequest
                    .getParameter("snapshotDuration");
            String message = startThread(server_id, new Long(snapshotDuration));
            actionResponse.setRenderParameter("message", message);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("snapshotDuration",
                    snapshotDuration);
        } else if (action.equals("disableServer")
                || action.equals("disableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
            ;
            actionResponse.setRenderParameter("message", alterServerState(
                    server_id, false));
        } else if (action.equals("enableServer")
                || action.equals("enableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("message", alterServerState(
                    server_id, true));
            actionResponse.setRenderParameter("server_id", server_id);
            ;
        } else if (action.equals("testAddServerConnection")) {
            String name = actionRequest.getParameter("name");
            String ip = actionRequest.getParameter("ip");
            String username = actionRequest.getParameter("username");
            String password = actionRequest.getParameter("password");
            String password2 = actionRequest.getParameter("password2");
            Integer port = Integer.parseInt(actionRequest.getParameter("port"));
            Integer protocol = Integer.parseInt(actionRequest.getParameter("protocol"));
            String message = testConnection(name, ip, username, password, port, protocol);
            actionResponse.setRenderParameter("message", message);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            actionResponse.setRenderParameter("password", password);
            actionResponse.setRenderParameter("password2", password2);
            actionResponse.setRenderParameter("port", "" + port);
            actionResponse.setRenderParameter("protocol", "" + protocol); 
        } else if (action.equals("testEditServerConnection")) {
            String name = actionRequest.getParameter("name");
            String ip = actionRequest.getParameter("ip");
            String username = actionRequest.getParameter("username");
            String password = actionRequest.getParameter("password");
            String password2 = actionRequest.getParameter("password2");
            String server_id = actionRequest.getParameter("server_id");
            String snapshot = actionRequest.getParameter("snapshot");
            String retention = actionRequest.getParameter("retention");
            Integer port = Integer.parseInt(actionRequest.getParameter("port"));
            Integer protocol = Integer.parseInt(actionRequest.getParameter("protocol"));
            String message = testConnection(name, ip, username, password, port, protocol);
            actionResponse.setRenderParameter("message", message);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            actionResponse.setRenderParameter("password", password);
            actionResponse.setRenderParameter("password2", password2);
            actionResponse.setRenderParameter("snapshot", snapshot);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("retention", retention);
            actionResponse.setRenderParameter("port", "" + port);
            actionResponse.setRenderParameter("protocol", "" + protocol);
        }
    }

    private String testConnection(String name, String ip, String username,
            String password, int port, int protocol) {
        try {
            MRCConnector mrc = new MRCConnector(ip, username, password, port, protocol);

            return "<font color=\"green\"><strong><li>Connection was successfully established.</li></strong></font>";
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>Failed to create a connection to server.</li></strong></font>";
        }
    }

    private String alterServerState(String server_id, boolean b) {
        Connection conn = (new DBManager()).getConnection();
        String message = "";
        String name = "";
        try {
            PreparedStatement pStmt = conn
                    .prepareStatement("SELECT * FROM servers WHERE server_id="
                            + server_id);
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
            rs.close();
            conn.close();
            conn = (new DBManager()).getConnection();
            Statement stmt = conn.createStatement();
            if (!b) {
                stmt
                        .executeUpdate("UPDATE SERVERS SET ENABLED = 0 WHERE SERVER_ID="
                                + server_id);
                stmt
                        .executeUpdate("UPDATE GRAPHS SET ENABLED = 0 WHERE SERVER_ID="
                                + server_id);
                message = "<font color=\"green\"><strong><li>Server " + name
                        + " was successfully disabled.</li></strong></font>";
            } else {
                stmt
                        .executeUpdate("UPDATE SERVERS SET ENABLED = 1 WHERE SERVER_ID="
                                + server_id);
                stmt
                        .executeUpdate("UPDATE GRAPHS SET ENABLED = 1 WHERE SERVER_ID="
                                + server_id);
                message = "<font color=\"green\"><strong><li>Server " + name
                        + " was successfully enabled.</li></strong></font>";
            }
        } catch (SQLException e) {
            if (!b)
                message = "<font color=\"red\"><strong><li>[ERROR] Server with server_id = "
                        + server_id
                        + " could not be disabled.</li></strong></font>";
            else
                message = "<font color=\"red\"><strong><li>[ERROR] Server with server_id = "
                        + server_id
                        + " could not be enabled.</li></strong></font>";
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {

                }
            }
        }
        return message;
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "showNormal";
        if (action.equals("showView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            pageView.include(request, response);
        } else if (action.equals("showAllViews")) {
            request.setAttribute("message", "");
            viewViews.include(request, response);
        } else if (action.equals("showAllServers")) {
            request.setAttribute("message", "");
            viewServers.include(request, response);
        } else if (action.equals("showAllGraphs")) {
            request.setAttribute("message", "");
            viewGraphs.include(request, response);
        } else if (action.equals("showServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            viewServer.include(request, response);
        } else if (action.equals("startTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String message = startTrackingMbean(server_id, mbean);
            request.setAttribute("message", message);
            viewServer.include(request, response);
        } else if (action.equals("stopTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String message = stopTrackingMbean(server_id, mbean);
            request.setAttribute("message", message);
            viewServer.include(request, response);
        } else if (action.equals("stopThread")) {
            String server_id = request.getParameter("server_id");
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("startThread")) {
            String server_id = request.getParameter("server_id");
            Long snapshotDuration = java.lang.Long.parseLong(
                    request.getParameter("snapshotDuration"));
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("disableServerViewQuery") || action.equals("enableServerViewQuery")) {
            String server_id = request.getParameter("server_id");
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            request.setAttribute("server_id", server_id);
            viewServer.include(request, response);
        } else {
            request.setAttribute("message", request.getParameter("message"));
            normalView.include(request, response);
        }
    }

    @Override
    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String action = request.getParameter("action");
        if (action.equals("showEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            editView.include(request, response);
        } else if (action.equals("saveEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            editView.include(request, response);
        } else if (action.equals("showAddView")) {
            addView.include(request, response);
        } else if (action.equals("saveAddView")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("showAddGraph")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String dataname = request.getParameter("dataname");
            request.setAttribute("dataname", dataname);
            addGraph.include(request, response);
        } else if (action.equals("saveAddGraph")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("showEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            editGraph.include(request, response);
        } else if (action.equals("saveEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            editGraph.include(request, response);
        } else if (action.equals("deleteGraph")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("deleteView")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("showEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            editServer.include(request, response);
        } else if (action.equals("saveEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            editServer.include(request, response);
        } else if (action.equals("showAddServer")) {
            addServer.include(request, response);
        } else if (action.equals("saveAddServer")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("deleteServer")) {
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            normalView.include(request, response);
        } else if (action.equals("testAddServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request
                    .setAttribute("password2", request
                            .getParameter("password2"));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("port", request.getParameter("port"));
            addServer.include(request, response);
        } else if (action.equals("testEditServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("port", request.getParameter("port"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request
                    .setAttribute("password2", request
                            .getParameter("password2"));
            request.setAttribute("message", request.getParameter("message"));
            request
                    .setAttribute("server_id", request
                            .getParameter("server_id"));
            request.setAttribute("snapshot", request.getParameter("snapshot"));
            request
                    .setAttribute("retention", request
                            .getParameter("retention"));
            editServer.include(request, response);
        } else if (action.equals("disableEditServer")
                || action.equals("enableEditServer")) {
            request.setAttribute("message", request.getParameter("message"));
            request
                    .setAttribute("server_id", request
                            .getParameter("server_id"));
            editServer.include(request, response);
        } else if (action.equals("disableServer")
                || action.equals("enableServer")) {
            request.setAttribute("message", request.getParameter("message"));
            request
                    .setAttribute("server_id", request
                            .getParameter("server_id"));
            normalView.include(request, response);
        } else {
            editNormalView.include(request, response);
        }
    }

    private void updateView(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String view_id = actionRequest.getParameter("view_id");
        actionResponse.setRenderParameter("view_id", view_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("description");
        String[] graphsArray = actionRequest.getParameterValues("graph_ids");
        if (graphsArray == null) {
            graphsArray = new String[0];
        }
        try {
            PreparedStatement pStmt = con
                    .prepareStatement("UPDATE views SET name='" + name
                            + "', description='" + description
                            + "', graph_count=" + graphsArray.length
                            + ", modified=CURRENT_TIMESTAMP WHERE view_id="
                            + view_id);
            pStmt.executeUpdate();
            pStmt = con
                    .prepareStatement("DELETE FROM views_graphs WHERE view_id="
                            + view_id);
            pStmt.executeUpdate();
            if (graphsArray != null)
                for (int i = 0; i < graphsArray.length; i++) {
                    pStmt = con
                            .prepareStatement("INSERT INTO views_graphs VALUES("
                                    + view_id + "," + graphsArray[i] + ")");
                    pStmt.executeUpdate();
                }
            con.close();
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>View " + name
                            + " has been updated</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error updating View "
                            + name + "</li></strong></font>" + e.getMessage());
            return;
        }
    }

    private void addView(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("description");
        String[] graphsArray = actionRequest.getParameterValues("graph_ids");
        if (graphsArray == null) {
            graphsArray = new String[0];
        }
        try {
            PreparedStatement pStmt = con
                    .prepareStatement("INSERT INTO views (name, description, graph_count, modified, added) VALUES ('"
                            + name
                            + "','"
                            + description
                            + "',"
                            + graphsArray.length
                            + ",CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
            pStmt.executeUpdate();
            pStmt = con
                    .prepareStatement("select view_id from views ORDER BY view_id DESC");
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                Integer view_id = rs.getInt("view_id");
                for (int i = 0; i < graphsArray.length; i++) {
                    pStmt = con
                            .prepareStatement("INSERT INTO views_graphs VALUES("
                                    + view_id + "," + graphsArray[i] + ")");
                    pStmt.executeUpdate();
                }
            }
            con.close();
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>View " + name
                            + " has been added</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding View " + name
                            + "</li></strong></font>" + e.getMessage());
        } finally {
            try {
                con.close();
            } catch (Exception e) {

            }
        }
    }

    private void updateServer(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String server_id = actionRequest.getParameter("server_id");
        actionResponse.setRenderParameter("server_id", server_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String ip = actionRequest.getParameter("ip");
        String password = actionRequest.getParameter("password");
        String username = actionRequest.getParameter("username");
        String snapshot = actionRequest.getParameter("snapshot");
        String retention = actionRequest.getParameter("retention");
        Integer port = Integer.parseInt(actionRequest.getParameter("port"));
        Integer protocol = Integer.parseInt(actionRequest.getParameter("protocol"));
        // encrypt the password
        if (password != null && !password.equals("")) {
            password = EncryptionManager.encrypt(password);
        }

        try {
            // update the client side db (table = SERVERS)
            if (password.equals("") || password == null) {
                PreparedStatement pStmt = con
                        .prepareStatement("UPDATE servers SET name='"
                                + name
                                + "', ip='"
                                + ip
                                + "', username='"
                                + username
                                + "', modified=CURRENT_TIMESTAMP, last_seen=CURRENT_TIMESTAMP, "
                                + "port=" + port + ",protocol="+protocol+" WHERE server_id="
                                + server_id);
                pStmt.executeUpdate();
                // when user did not specify the password, just grab it from the
                // db
                pStmt = con
                        .prepareStatement("SELECT password FROM servers WHERE server_id="
                                + server_id);
                ResultSet s = pStmt.executeQuery();
                if (s.next()) {
                    password = s.getString("password");
                } else {
                    actionResponse
                            .setRenderParameter(
                                    "message",
                                    "<font color=\"red\"><strong><li>Error updating server</li></strong></font>"
                                            + "Password was not found in the database for server_id="
                                            + server_id);
                    con.close();
                    return;
                }
            } else {
                PreparedStatement pStmt = con
                        .prepareStatement("UPDATE servers SET name='"
                                + name
                                + "', ip='"
                                + ip
                                + "', username='"
                                + username
                                + "', password='"
                                + password
                                + "', modified=CURRENT_TIMESTAMP, last_seen=CURRENT_TIMESTAMP, "
                                + "port=" + port + ",protocol="+protocol+" WHERE server_id="
                                + server_id);
                pStmt.executeUpdate();
            }
            con.close();
            // update the server side db
            if (snapshot == null || retention == null) {
                // do not update if we do not know
            } else {
                    (new MRCConnector(ip, username, password, port, protocol))
                        .setSnapshotDuration(Long.parseLong(snapshot) * 1000 * 60);
                    (new MRCConnector(ip, username, password, port, protocol))
                        .setSnapshotRetention(Integer.parseInt(retention));
            }
            // set success message
            actionResponse
                    .setRenderParameter(
                            "message",
                            "<font color=\"green\"><strong><li>Server has been updated</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error updating server</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void addServer(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String ip = actionRequest.getParameter("ip");
        int protocol = Integer.parseInt(actionRequest.getParameter("protocol"));
        int port = Integer.parseInt(actionRequest.getParameter("port"));
        String password = actionRequest.getParameter("password");
        String username = actionRequest.getParameter("username");
        // encrypt the password
        if (password != null && !password.equals("")) {
            password = EncryptionManager.encrypt(password);
        }
        try {
            PreparedStatement pStmt = con
                    .prepareStatement("INSERT INTO servers (name, ip, username, password, modified, last_seen, added, port, protocol) VALUES ('"
                            + name
                            + "','"
                            + ip
                            + "','"
                            + username
                            + "','"
                            + password
                            + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,"
                            + port
                            + ","
                            + protocol + ")");
            pStmt.executeUpdate();
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>Server " + name + " at "
                            + ip + " has been added.</li></strong></font>");

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding server</li></strong></font>"
                            + e.getMessage());
        } finally {
            try {
                con.close();
            } catch (Exception e) {

            }
        }
    }

    private void deleteServer(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String server_id = actionRequest.getParameter("server_id");
        actionResponse.setRenderParameter("server_id", server_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();

        try {
            PreparedStatement pStmt = con
                    .prepareStatement("DELETE FROM graphs WHERE server_id="
                            + server_id);
            pStmt.executeUpdate();

            pStmt = con.prepareStatement("DELETE FROM servers WHERE server_id="
                    + server_id);
            pStmt.executeUpdate();
            con.close();
            actionResponse
                    .setRenderParameter(
                            "message",
                            "<font color=\"green\"><strong><li>Server and associated graphs have been deleted</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting server</li></strong></font>"
                            + e.getMessage());
            return;
        }
    }

    private void deleteView(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String view_id = actionRequest.getParameter("view_id");
        actionResponse.setRenderParameter("view_id", view_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();

        try {
            PreparedStatement pStmt = con
                    .prepareStatement("DELETE FROM views WHERE view_id="
                            + view_id);
            pStmt.executeUpdate();
            pStmt = con
                    .prepareStatement("DELETE FROM views_graphs WHERE view_id="
                            + view_id);
            pStmt.executeUpdate();
            con.close();
            actionResponse
                    .setRenderParameter("message",
                            "<font color=\"green\"><strong><li>View has been deleted</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting view</li></strong></font>"
                            + e.getMessage());
            return;
        }
    }

    private void addGraph(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("description");
        String server_id = actionRequest.getParameter("server_id");
        String xlabel = actionRequest.getParameter("xlabel");
        String ylabel = actionRequest.getParameter("ylabel");
        String timeframe = actionRequest.getParameter("timeframe");
        String mbean = actionRequest.getParameter("mbean");
        String dataname1 = actionRequest.getParameter("dataname1");
        String data1operation = actionRequest.getParameter("data1operation");
        String operation = actionRequest.getParameter("operation");
        int showArchive = 0;
        if (actionRequest.getParameter("showArchive") != null
                && actionRequest.getParameter("showArchive").equals("on")) {
            showArchive = 1;
        }

        if (operation.equals("other")) {
            operation = actionRequest.getParameter("othermath");
        }
        String dataname2 = actionRequest.getParameter("dataname2");
        String data2operation = actionRequest.getParameter("data2operation");
        if (data2operation == null)
            data2operation = "A";
        try {
            PreparedStatement pStmt = con
                    .prepareStatement("INSERT INTO graphs (server_id, name, description, timeframe, mbean, dataname1, xlabel, ylabel, data1operation, operation, data2operation, dataname2, warninglevel1, warninglevel2, added, modified, last_seen, archive) VALUES ("
                            + server_id
                            + ",'"
                            + name
                            + "','"
                            + description
                            + "',"
                            + timeframe
                            + ",'"
                            + mbean
                            + "','"
                            + dataname1
                            + "','"
                            + xlabel
                            + "','"
                            + ylabel
                            + "','"
                            + data1operation
                            + "','"
                            + operation
                            + "','"
                            + data2operation
                            + "','"
                            + dataname2
                            + "',0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,"
                            + showArchive + ")");
            pStmt.executeUpdate();
            con.close();
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>Graph " + name
                            + " has been added.</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding graph</li></strong></font>"
                            + e.getMessage());
            return;
        }
    }

    private void updateGraph(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String graph_id = actionRequest.getParameter("graph_id");
        actionResponse.setRenderParameter("graph_id", graph_id);

        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("description");
        String server_id = actionRequest.getParameter("server_id");
        String xlabel = actionRequest.getParameter("xlabel");
        String ylabel = actionRequest.getParameter("ylabel");
        String timeframe = actionRequest.getParameter("timeframe");
        String mbean = actionRequest.getParameter("mbean");
        String dataname1 = actionRequest.getParameter("dataname1");
        String data1operation = actionRequest.getParameter("data1operation");
        String operation = actionRequest.getParameter("operation");
        int archive = 0;
        if (actionRequest.getParameter("showArchive") != null
                && actionRequest.getParameter("showArchive").equals("on")) {
            archive = 1;
        }

        if (operation.equals("other")) {
            operation = actionRequest.getParameter("othermath");
        }
        String dataname2 = actionRequest.getParameter("dataname2");
        String data2operation = actionRequest.getParameter("data2operation");
        if (data2operation == null)
            data2operation = "A";
        try {
            PreparedStatement pStmt = con
                    .prepareStatement("UPDATE graphs SET server_id="
                            + server_id
                            + ", name='"
                            + name
                            + "', description='"
                            + description
                            + "', timeframe="
                            + timeframe
                            + ", mbean='"
                            + mbean
                            + "', dataname1='"
                            + dataname1
                            + "', xlabel='"
                            + xlabel
                            + "', ylabel='"
                            + ylabel
                            + "', data1operation='"
                            + data1operation
                            + "', operation='"
                            + operation
                            + "', data2operation='"
                            + data2operation
                            + "', dataname2='"
                            + dataname2
                            + "', warninglevel1=0, warninglevel2=0, modified=CURRENT_TIMESTAMP, archive="
                            + archive + " WHERE graph_id=" + graph_id);
            pStmt.executeUpdate();
            con.close();
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>Graph " + name
                            + " has been updated.</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error editing graph</li></strong></font>"
                            + e.getMessage());
            return;
        }
    }

    private void deleteGraph(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String graph_id = actionRequest.getParameter("graph_id");
        actionResponse.setRenderParameter("graph_id", graph_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();

        try {
            // remove the graph
            PreparedStatement pStmt = con
                    .prepareStatement("DELETE FROM graphs WHERE graph_id="
                            + graph_id);
            pStmt.executeUpdate();
            // fetch all views associated with this graph
            pStmt = con
                    .prepareStatement("SELECT view_id FROM views_graphs WHERE graph_id="
                            + graph_id);
            ResultSet view_ids = pStmt.executeQuery();
            // reduce the graph_count from all views associated with the graph
            while (view_ids.next()) {
                pStmt = con
                        .prepareStatement("UPDATE views SET graph_count=graph_count-1 WHERE view_id="
                                + view_ids.getString("view_id"));
                pStmt.executeUpdate();
            }
            // remove the relationship between graphs and views
            pStmt = con
                    .prepareStatement("DELETE FROM views_graphs WHERE graph_id="
                            + graph_id);
            pStmt.executeUpdate();
            con.close();
            actionResponse
                    .setRenderParameter("message",
                            "<font color=\"green\"><strong><li>Graph has been deleted</li></strong></font>");
            return;

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting graph</li></strong></font>"
                            + e.getMessage());
            return;
        }
    }

    private String startTrackingMbean(String server_id, String mbean) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        MRCConnector mrc = null;
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String server_ip = null;
        String username = null;
        String password = null;
        int protocol = 0;
        int port = -1;
        // fetch server information
        try {
            pStmt = con
                    .prepareStatement("SELECT * FROM servers WHERE server_id="
                            + server_id);
            rs = pStmt.executeQuery();
            if (!rs.next()) {
                return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        // attempt to connect to the mrc server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }

        // tell the mrc server to start tracking an mbean
        try {
            if (mrc.startTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"green\"><strong><li>MBean " + mbarr[1]
                        + " tracking on server " + rs.getString("name")
                        + "</li></strong></font>";
            } else {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"red\"><strong><li>ERROR: MBean "
                        + mbarr[1] + " could <b>NOT</b> be tracked on server "
                        + rs.getString("name") + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: MBean " + mbean
                    + " could <b>NOT</b> be tracked on server " + server_ip
                    + ": " + e.getMessage() + "</li></strong></font>";
        }
    }

    private String stopTrackingMbean(String server_id, String mbean) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        MRCConnector mrc = null;
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String server_ip = null;
        String username = null;
        String password = null;
        int port = -1;
        int protocol = 0;
        // fetch server's information
        try {
            pStmt = con
                    .prepareStatement("SELECT * FROM servers WHERE server_id="
                            + server_id);
            rs = pStmt.executeQuery();
            if (!rs.next()) {
                return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
                mrc = new MRCConnector(server_ip, username, password, port, protocol);
       } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }
        // tell the mrc-server to stop tracking some mbean
        try {
            if (mrc.stopTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"green\"><strong><li>MBean " + mbarr[1]
                        + " removed from tracking on server "
                        + rs.getString("name") + "</li></strong></font>";
            } else {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"red\"><strong><li>ERROR: MBean "
                        + mbarr[1]
                        + " could <b>NOT</b> be removed from tracking on server "
                        + rs.getString("name") + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: MBean " + mbean
                    + " could <b>NOT</b> be removed from tracking on server "
                    + server_ip + ": " + e.getMessage()
                    + "</li></strong></font>";
        }
    }

    private String stopThread(String server_id) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        MRCConnector mrc = null;
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String server_ip = null;
        String username = null;
        String password = null;
        int port = -1;
        int protocol = 0;
        // fetch the server's information
        try {
            pStmt = con
                    .prepareStatement("SELECT * FROM servers WHERE server_id="
                            + server_id);
            rs = pStmt.executeQuery();
            if (!rs.next()) {
                return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }
        // tell the mrc-server to stop taking snapshots
        try {
            if (mrc.stopSnapshotThread()) {
                return "<font color=\"green\"><strong><li>Snapshot thread stopped on server "
                        + rs.getString("name") + "</li></strong></font>";
            } else {
                return "<font color=\"red\"><strong><li>ERROR: Snapshot thread could <b>NOT</b> be stopped on server "
                        + rs.getString("name") + "</li></strong></font>";
            }

        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Snapshot thread could <b>NOT</b> be stopped on server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }
    }

    private String startThread(String server_id, Long snapshotDuration) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        MRCConnector mrc = null;
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String server_ip = null;
        String username = null;
        String password = null;
        int port = -1;
        int protocol = 0;
        // fetch the server's information
        try {
            pStmt = con
                    .prepareStatement("SELECT * FROM servers WHERE server_id="
                            + server_id);
            rs = pStmt.executeQuery();
            if (!rs.next()) {
                return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }
        // tell the mrc-server to start the collection of statistics
        try {
            if (mrc.startSnapshotThread(new Long(snapshotDuration))) {
                return "<font color=\"green\"><strong><li>Snapshot thread started on server "
                        + rs.getString("name") + "</li></strong></font>";
            } else {
                return "<font color=\"red\"><strong><li>ERROR: Snapshot thread could <b>NOT</b> be started on server "
                        + rs.getString("name") + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Snapshot thread could <b>NOT</b> be started on server "
                    + server_ip
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";
        }
    }

    @Override
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        viewViews = portletConfig.getPortletContext().getRequestDispatcher(
                VIEWVIEWS_JSP);
        viewServers = portletConfig.getPortletContext().getRequestDispatcher(
                VIEWSERVERS_JSP);
        viewGraphs = portletConfig.getPortletContext().getRequestDispatcher(
                VIEWGRAPHS_JSP);
        pageView = portletConfig.getPortletContext().getRequestDispatcher(
                PAGEVIEW_JSP);
        editView = portletConfig.getPortletContext().getRequestDispatcher(
                EDITVIEW_JSP);
        addView = portletConfig.getPortletContext().getRequestDispatcher(
                ADDVIEW_JSP);
        addGraph = portletConfig.getPortletContext().getRequestDispatcher(
                ADDGRAPH_JSP);
        editGraph = portletConfig.getPortletContext().getRequestDispatcher(
                EDITGRAPH_JSP);
        viewServer = portletConfig.getPortletContext().getRequestDispatcher(
                VIEWSERVER_JSP);
        editServer = portletConfig.getPortletContext().getRequestDispatcher(
                EDITSERVER_JSP);
        addServer = portletConfig.getPortletContext().getRequestDispatcher(
                ADDSERVER_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
        editNormalView = portletConfig.getPortletContext()
                .getRequestDispatcher(EDITNORMALVIEW_JSP);
    }

    @Override
    public void destroy() {
        normalView = null;
        viewViews = null;
        viewServers = null;
        viewGraphs = null;
        pageView = null;
        editView = null;
        addView = null;
        addGraph = null;
        editGraph = null;
        viewServer = null;
        editServer = null;
        addServer = null;
        helpView = null;
        editNormalView = null;
        super.destroy();
    }
}
