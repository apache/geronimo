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
import java.text.MessageFormat;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.monitoring.console.util.DBManager;
import org.apache.geronimo.crypto.EncryptionManager;

/**
 * STATS
 */
public class MonitoringPortlet extends BasePortlet {

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
            stopThread(server_id, actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("startThread")
                || action.equals("enableServerViewQuery")) {
            String server_id = actionRequest.getParameter("server_id");
            String snapshotDuration = actionRequest
                    .getParameter("snapshotDuration");
            startThread(server_id, new Long(snapshotDuration), actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("snapshotDuration",
                    snapshotDuration);
        } else if (action.equals("disableServer")
                || action.equals("disableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            alterServerState(server_id, false, actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("enableServer")
                || action.equals("enableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            alterServerState(server_id, true, actionRequest);
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
            testConnection(ip, username, password, port, protocol, actionRequest);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            //  Don't return the password in the output
//            actionResponse.setRenderParameter("password", password);
//            actionResponse.setRenderParameter("password2", password2);
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
            if(snapshot == null) {
                snapshot = "";
            }
            if(retention == null) {
                retention = "";
            }
            testConnection(ip, username, password, port, protocol, actionRequest);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            //  Don't return the password in the output
//            actionResponse.setRenderParameter("password", password);
//            actionResponse.setRenderParameter("password2", password2);
            actionResponse.setRenderParameter("snapshot", snapshot);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("retention", retention);
            actionResponse.setRenderParameter("port", "" + port);
            actionResponse.setRenderParameter("protocol", "" + protocol);
        }
    }

    private void testConnection(String ip, String username,
            String password, int port, int protocol, PortletRequest request) {
        MRCConnector mrc = null;
        try {
            mrc = new MRCConnector(ip, username, password, port, protocol);
            addInfoMessage(request, getLocalizedString(request, "infoMsg01"));
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg01"), e.getMessage());
        }finally{
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void alterServerState(String server_id, boolean enable, PortletRequest request) {
        Connection conn = (new DBManager()).getConnection();
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
            if (enable) {
                stmt
                        .executeUpdate("UPDATE SERVERS SET ENABLED = 1 WHERE SERVER_ID="
                                + server_id);
                stmt
                        .executeUpdate("UPDATE GRAPHS SET ENABLED = 1 WHERE SERVER_ID="
                                + server_id);
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg02"), name));
            } else {
                stmt
                        .executeUpdate("UPDATE SERVERS SET ENABLED = 0 WHERE SERVER_ID="
                                + server_id);
                stmt
                        .executeUpdate("UPDATE GRAPHS SET ENABLED = 0 WHERE SERVER_ID="
                                + server_id);
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg03"), name));
            }
        } catch (SQLException e) {
            if (enable) {
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg02"), server_id), e.getMessage());
            } else {
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg03"), server_id), e.getMessage());
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {

                }
            }
        }
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
            startTrackingMbean(server_id, mbean, request);
            viewServer.include(request, response);
        } else if (action.equals("stopTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            stopTrackingMbean(server_id, mbean, request);
            viewServer.include(request, response);
        } else if (action.equals("stopThread")) {
            String server_id = request.getParameter("server_id");
            normalView.include(request, response);
        } else if (action.equals("startThread")) {
            String server_id = request.getParameter("server_id");
            Long snapshotDuration = java.lang.Long.parseLong(
                    request.getParameter("snapshotDuration"));
            normalView.include(request, response);
        } else if (action.equals("disableServerViewQuery") || action.equals("enableServerViewQuery")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            viewServer.include(request, response);
        } else {
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
        if (action == null)
            action = "showNormal";
        if (action.equals("showEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            editView.include(request, response);
        } else if (action.equals("saveEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            editView.include(request, response);
        } else if (action.equals("showAddView")) {
            addView.include(request, response);
        } else if (action.equals("saveAddView")) {
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
            normalView.include(request, response);
        } else if (action.equals("showEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            editGraph.include(request, response);
        } else if (action.equals("saveEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            editGraph.include(request, response);
        } else if (action.equals("deleteGraph")) {
            normalView.include(request, response);
        } else if (action.equals("deleteView")) {
            normalView.include(request, response);
        } else if (action.equals("showEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            editServer.include(request, response);
        } else if (action.equals("saveEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            editServer.include(request, response);
        } else if (action.equals("showAddServer")) {
            addServer.include(request, response);
        } else if (action.equals("saveAddServer")) {
            normalView.include(request, response);
        } else if (action.equals("deleteServer")) {
            normalView.include(request, response);
        } else if (action.equals("testAddServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request.setAttribute("password2", request.getParameter("password2"));
            request.setAttribute("protocol", request.getParameter("protocol"));
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
            request
                    .setAttribute("server_id", request
                            .getParameter("server_id"));
            editServer.include(request, response);
        } else if (action.equals("disableServer")
                || action.equals("enableServer")) {
            request
                    .setAttribute("server_id", request
                            .getParameter("server_id"));
            normalView.include(request, response);
        } else {
            normalView.include(request, response);
        }
    }

    private void updateView(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String view_id = actionRequest.getParameter("view_id");
        actionResponse.setRenderParameter("view_id", view_id);
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("minxss_description");
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg08"), name));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "errorMsg11"), name), e.getMessage());
            return;
        }
    }

    private void addView(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("minxss_description");
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg09"), name));
        } catch (Exception e) {
            addErrorMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "errorMsg12"), name), e.getMessage());
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
        MRCConnector mrc = null;
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
                                    "Error updating server "
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
                    mrc = new MRCConnector(ip, username, password, port, protocol);
                    mrc.setSnapshotDuration(Long.parseLong(snapshot) * 1000 * 60);
                    mrc.setSnapshotRetention(Integer.parseInt(retention));
            }
            // set success message
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "infoMsg10"));
        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg13"), e.getMessage());
        }finally{
            if(null != mrc)
                mrc.dispose();
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg11"), name, ip));
        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg14"), e.getMessage());
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "infoMsg12"));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg15"), e.getMessage());
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "infoMsg13"));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg16"), e.getMessage());
            return;
        }
    }

    private void addGraph(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        DBManager DBase = new DBManager();
        Connection con = DBase.getConnection();
        String name = actionRequest.getParameter("name");
        String description = actionRequest.getParameter("minxss_description");
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg14"), name));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg17"), e.getMessage());
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
        String description = actionRequest.getParameter("minxss_description");
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg15"), name));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg18"), e.getMessage());
            return;
        }
    }

    private void deleteGraph(ActionRequest actionRequest,
            ActionResponse actionResponse) {
        String graph_id = actionRequest.getParameter("graph_id");
        String name = actionRequest.getParameter("name");
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
            addInfoMessage(actionRequest, MessageFormat.format(getLocalizedString(actionRequest, "infoMsg16"), name));
            return;

        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "errorMsg19"), e.getMessage());
            return;
        }
    }

    private void startTrackingMbean(String server_id, String mbean, PortletRequest request) {
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
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg08"), server_id));
                return;
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg04"), e.getMessage());
            return;
        }
        // attempt to connect to the mrc server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg05"), server_ip), e.getMessage());
            return;
        }

        // tell the mrc server to start tracking an mbean
        try {
            if (mrc.startTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg04"), mbarr[1], rs.getString("name")));
                return;
            } else {
                String mbarr[] = mbean.split("name=");
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg06"), mbarr[1], rs.getString("name")));
                return;
            }
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg06"), mbean, server_ip), e.getMessage());
            return;
        }finally{
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void stopTrackingMbean(String server_id, String mbean, PortletRequest request) {
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
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg08"), server_id));
                return;
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg04"), e.getMessage());
            return;
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
                mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
           addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg05"), server_ip), e.getMessage());
           return;
        }
        // tell the mrc-server to stop tracking some mbean
        try {
            if (mrc.stopTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg05"), mbarr[1], rs.getString("name")));
                return;
            } else {
                String mbarr[] = mbean.split("name=");
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg07"), mbarr[1], rs.getString("name")));
                return;
            }
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg07"), mbean, server_ip), e.getMessage());
        }finally{
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void stopThread(String server_id, PortletRequest request) {
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
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg08"), server_id));
                return;
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg04"), e.getMessage());
            return;
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg05"), server_ip), e.getMessage());
            return;
        }
        // tell the mrc-server to stop taking snapshots
        try {
            if (mrc.stopSnapshotThread()) {
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg06"), server_ip));
                return;
            } else {
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg09"), server_ip));
                return;
            }

        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg09"), server_ip), e.getMessage());
            return;
        }finally{
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void startThread(String server_id, Long snapshotDuration, PortletRequest request) {
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
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg08"), server_id));
                return;
            }
            server_ip = rs.getString("ip");
            password = rs.getString("password");
            username = rs.getString("username");
            port = rs.getInt("port");
            protocol = rs.getInt("protocol");
        } catch (SQLException e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg04"), e.getMessage());
            return;
        }
        // attempt to connect to the mrc-server
        try {
            con.close();
            mrc = new MRCConnector(server_ip, username, password, port, protocol);
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg05"), server_ip), e.getMessage());
            return;
        }
        // tell the mrc-server to start the collection of statistics
        try {
            if (mrc.startSnapshotThread(new Long(snapshotDuration))) {
                addInfoMessage(request, MessageFormat.format(getLocalizedString(request, "infoMsg07"), rs.getString("name")));
                return;
            } else {
                addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg10"), rs.getString("name")));
                return;
            }
        } catch (Exception e) {
            addErrorMessage(request, MessageFormat.format(getLocalizedString(request, "errorMsg10"), server_ip), e.getMessage());
            return;
        }finally{
            if(null != mrc)
                mrc.dispose();
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
