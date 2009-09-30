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
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.monitoring.console.util.DBManager;

/**
 * STATS
 */
public class MonitoringPortlet extends BasePortlet {
    
    private static final Log log = LogFactory.getLog(MonitoringPortlet.class);

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
    
    private String serverType;

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
        else if(action.equals("restoreData")){
            restoreData(actionRequest);
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
        if(request.isUserInRole("admin")){
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
        }//end admin
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

    private void restoreData(PortletRequest request) {
        Connection conn = null;
        try {
            conn = DBManager.createConnection();
            deleteDefaultServerView(conn);
            initializeDefaultServerView(conn);
            addInfoMessage(request, getLocalizedString(request, "infoMsg17"));
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "errorMsg20"), e.getMessage());
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (Exception e) {
                }
        }
    }
    
    private String getObjectNameByShortName(String shortName) {
        try {
            Object targetGBean = PortletManager.getKernel().getGBean(shortName);
            return PortletManager.getKernel().getAbstractNameFor(targetGBean).getObjectName().getCanonicalName();
        } catch (Exception e) {
            log.error("Fail to find the gbean object for the short name " + shortName);
            return null;
        }
    }

    private void initializeDefaultServerView(Connection conn) throws PortletException {
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement("select server_id from servers where ip='localhost' and protocol=1");
            ResultSet rs = preparedStmt.executeQuery();
            if (rs.next()) {
                return;
            }
            if (serverType.equals(WEB_SERVER_TOMCAT)) {
                initializeDefaultTomatServerView(conn);
            } else if (serverType.equals(WEB_SERVER_JETTY)) {
                initializeDefaultJettyServerView(conn);
            }
        } catch (SQLException e) {
            throw new PortletException(e);
        } finally {
            if (preparedStmt != null)
                try {
                    preparedStmt.close();
                } catch (Exception e) {
                }
        }
    }
    
    private void initializeDefaultTomatServerView(Connection conn) throws SQLException {
        int serverId = insertServer(conn, "Default Server", "localhost", "", "", "1099", "1");
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(
                    "INSERT INTO graphs (server_id, name, description, timeframe, mbean, dataname1, xlabel, ylabel, data1operation, operation, data2operation, dataname2, warninglevel1, warninglevel2, added, modified, last_seen, archive,enabled) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)",
                    Statement.RETURN_GENERATED_KEYS);
            int jvmGraphId = insertGraph(preparedStmt, "JVM Heap Size Current", serverId, "JVM Heap Size Current", 60, getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", "");
            String tomcatWebConnectorObjectName = getObjectNameByShortName("TomcatWebConnector");
            if (tomcatWebConnectorObjectName != null) {
                List<Integer> graphIdList = new ArrayList<Integer>(15);
                graphIdList.add(jvmGraphId);
                graphIdList.add(insertGraph(preparedStmt, "Active Request Count", serverId, "Active Request Count", 60, tomcatWebConnectorObjectName, "Active Request Count", "Active Request Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Busy Threads Max", serverId, "Busy Threads Max", 60, tomcatWebConnectorObjectName, "Busy Threads Max", "Busy Threads Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Busy Threads Current", serverId, "Busy Threads Current", 60, tomcatWebConnectorObjectName, "Busy Threads Current", "Busy Threads Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Busy Threads Min", serverId, "Busy Threads Min", 60, tomcatWebConnectorObjectName, "Busy Threads Min", "Busy Threads Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Bytes Received", serverId, "Bytes Received", 60, tomcatWebConnectorObjectName, "Bytes Received", "Bytes Received", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Bytes Sent", serverId, "Bytes Sent", 60, tomcatWebConnectorObjectName, "Bytes Sent", "Bytes Sent", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Error Count", serverId, "Error Count", 60, tomcatWebConnectorObjectName, "Error Count", "Error Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Current", serverId, "Open Connections Current", 60, tomcatWebConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Max", serverId, "Open Connections Max", 60, tomcatWebConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Min", serverId, "Open Connections Min", 60, tomcatWebConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Request Time CurrentTime", serverId, "Request Time CurrentTime", 60, tomcatWebConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Request Time MaxTime", serverId, "Request Time MaxTime", 60, tomcatWebConnectorObjectName, "Request Time MaxTime", "Request Time MaxTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Request Time MinTime", serverId, "Request Time MinTime", 60, tomcatWebConnectorObjectName, "Request Time MinTime", "Request Time MinTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Request Time TotalTime", serverId, "Request Time TotalTime", 60, tomcatWebConnectorObjectName, "Request Time TotalTime", "Request Time TotalTime", "Time", "A", "", "A", ""));
                insertView(conn, graphIdList.toArray(new Integer[0]), "TomcatWebConnector View", "TomcatWebConnector View");
            }
            String tomcatWebSSLConnectorObjectName = getObjectNameByShortName("TomcatWebSSLConnector");
            if (tomcatWebSSLConnectorObjectName != null) {
                List<Integer> graphIdList = new ArrayList<Integer>(15);
                graphIdList.add(jvmGraphId);
                graphIdList.add(insertGraph(preparedStmt, "SSL Active Request Count", serverId, "Active Request Count", 60, tomcatWebSSLConnectorObjectName, "Active Request Count", "Active Request Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Busy Threads Current", serverId, "Busy Threads Current", 60, tomcatWebSSLConnectorObjectName, "Busy Threads Current", "Busy Threads Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Busy Threads Max", serverId, "Busy Threads Max", 60, tomcatWebSSLConnectorObjectName, "Busy Threads Max", "Busy Threads Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Busy Threads Min", serverId, "Busy Threads Min", 60, tomcatWebSSLConnectorObjectName, "Busy Threads Min", "Busy Threads Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Bytes Received", serverId, "Bytes Received", 60, tomcatWebSSLConnectorObjectName, "Bytes Received", "Bytes Received", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Bytes Sent", serverId, "Bytes Sent", 60, tomcatWebSSLConnectorObjectName, "Bytes Sent", "Bytes Sent", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Error Count", serverId, "Error Count", 60, tomcatWebSSLConnectorObjectName, "Error Count", "Error Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Current", serverId, "Open Connections Current", 60, tomcatWebSSLConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Max", serverId, "Open Connections Max", 60, tomcatWebSSLConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Min", serverId, "Open Connections Min", 60, tomcatWebSSLConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Request Time CurrentTime", serverId, "Request Time CurrentTime", 60, tomcatWebSSLConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Request Time MaxTime", serverId, "Request Time MaxTime", 60, tomcatWebSSLConnectorObjectName, "Request Time MaxTime", "Request Time MaxTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Request Time MinTime", serverId, "Request Time MinTime", 60, tomcatWebSSLConnectorObjectName, "Request Time MinTime", "Request Time MinTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Request Time TotalTime", serverId, "Request Time TotalTime", 60, tomcatWebSSLConnectorObjectName, "Request Time TotalTime", "Request Time TotalTime", "Time", "A", "", "A", ""));
                insertView(conn, graphIdList.toArray(new Integer[0]), "TomcatWebSSLConnector View", "TomcatWebSSLConnector View");
            }
        } finally {
            if (preparedStmt != null)
                try {
                    preparedStmt.close();
                } catch (Exception e) {
                }
        }
    }

    private void initializeDefaultJettyServerView(Connection conn) throws SQLException {
        int serverId = insertServer(conn, "Default Server", "localhost", "", "", "1099", "1");
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(
                    "INSERT INTO graphs (server_id, name, description, timeframe, mbean, dataname1, xlabel, ylabel, data1operation, operation, data2operation, dataname2, warninglevel1, warninglevel2, added, modified, last_seen, archive,enabled) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,0)",
                    Statement.RETURN_GENERATED_KEYS);
            int jvmGraphId = insertGraph(preparedStmt, "JVM Heap Size Current", serverId, "JVM Heap Size Current", 60, getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", "");
            String jettyWebConnectorObjectName = getObjectNameByShortName("JettyWebConnector");
            if (jettyWebConnectorObjectName != null) {
                List<Integer> graphIdList = new ArrayList<Integer>();
                graphIdList.add(jvmGraphId);
                graphIdList.add(insertGraph(preparedStmt, "Connections Duration Count", serverId, "Connections Duration Count", 60, jettyWebConnectorObjectName, "Connections Duration Count", "Connections Duration Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Duration MaxTime", serverId, "Connections Duration MaxTime", 60, jettyWebConnectorObjectName, "Connections Duration MaxTime", "Connections Duration MaxTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Duration MinTime", serverId, "Connections Duration MinTime", 60, jettyWebConnectorObjectName, "Connections Duration MinTime", "Connections Duration MinTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Duration TotalTime", serverId, "Connections Duration TotalTime", 60, jettyWebConnectorObjectName, "Connections Duration TotalTime", "Connections Duration TotalTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Request Current", serverId, "Connections Request Current", 60, jettyWebConnectorObjectName, "Connections Request Current", "Connections Request Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Request Max", serverId, "Connections Request Max", 60, jettyWebConnectorObjectName, "Connections Request Max", "Connections Request Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Connections Request Min", serverId, "Connections Request Min", 60, jettyWebConnectorObjectName, "Connections Request Min", "Connections Request Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Current", serverId, "Open Connections Current", 60, jettyWebConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Max", serverId, "Open Connections Max", 60, jettyWebConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Open Connections Min", serverId, "Open Connections Min", 60, jettyWebConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "Request Time CurrentTime", serverId, "Request Time CurrentTime", 60, jettyWebConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", ""));
                insertView(conn, graphIdList.toArray(new Integer[0]), "JettyWebConnector View", "JettyWebConnector View");
            }
            String jettyWebSSLConnectorObjectName = getObjectNameByShortName("JettySSLConnector");
            if (jettyWebSSLConnectorObjectName != null) {
                List<Integer> graphIdList = new ArrayList<Integer>();
                graphIdList.add(jvmGraphId);
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Duration Count", serverId, "Connections Duration Count", 60, jettyWebSSLConnectorObjectName, "Connections Duration Count", "Connections Duration Count", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Duration MaxTime", serverId, "Connections Duration MaxTime", 60, jettyWebSSLConnectorObjectName, "Connections Duration MaxTime", "Connections Duration MaxTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Duration MinTime", serverId, "Connections Duration MinTime", 60, jettyWebSSLConnectorObjectName, "Connections Duration MinTime", "Connections Duration MinTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Duration TotalTime", serverId, "Connections Duration TotalTime", 60, jettyWebSSLConnectorObjectName, "Connections Duration TotalTime", "Connections Duration TotalTime", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Request Current", serverId, "Connections Request Current", 60, jettyWebSSLConnectorObjectName, "Connections Request Current", "Connections Request Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Request Max", serverId, "Connections Request Max", 60, jettyWebSSLConnectorObjectName, "Connections Request Max", "Connections Request Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Connections Request Min", serverId, "Connections Request Min", 60, jettyWebSSLConnectorObjectName, "Connections Request Min", "Connections Request Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Current", serverId, "Open Connections Current", 60, jettyWebSSLConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Max", serverId, "Open Connections Max", 60, jettyWebSSLConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Open Connections Min", serverId, "Open Connections Min", 60, jettyWebSSLConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", ""));
                graphIdList.add(insertGraph(preparedStmt, "SSL Request Time CurrentTime", serverId, "Request Time CurrentTime", 60, jettyWebSSLConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", ""));
                insertView(conn, graphIdList.toArray(new Integer[0]), "JettyWebSSLConnector View", "JettyWebSSLConnector View");
            }
        } finally {
            if (preparedStmt != null)
                try {
                    preparedStmt.close();
                } catch (Exception e) {
                }
        }                      
    }

    private int insertServer(Connection conn, String name, String ip, String username, String password, String port, String protocol) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute("INSERT INTO servers (enabled ,name, ip, username, password, modified, last_seen, added, port, protocol) VALUES (" + "0" + ",'" + name + "','" + ip + "','" + username + "','" + password + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP," + port + "," + protocol + ")", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Fail to insert the new server " + name);
            }
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
        }
    }

    private int insertGraph(PreparedStatement stmt, String name, int server_id, String description, int timeframe, String mbean, String dataname1, String xlabel, String ylabel, String data1operation, String operation, String data2operation, String dataname2) throws SQLException {
        stmt.setInt(1, server_id);
        stmt.setString(2, name);
        stmt.setString(3, description);
        stmt.setInt(4, timeframe);
        stmt.setString(5, mbean);
        stmt.setString(6, dataname1);
        stmt.setString(7, xlabel);
        stmt.setString(8, ylabel);
        stmt.setString(9, data1operation);
        stmt.setString(10, operation);
        stmt.setString(11, data2operation);
        stmt.setString(12, dataname2);
        if (stmt.executeUpdate() == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Fail to insert the graph " + name);
    }

    private void insertView(Connection conn, Integer[] graphIds, String name, String description) throws SQLException {
        Statement viewStmt = null;
        PreparedStatement preparedStmt = null;
        try {
            viewStmt = conn.createStatement();
            viewStmt.executeUpdate("INSERT INTO views (name, description, graph_count, modified, added) VALUES ('" + name + "','" + description + "'," + graphIds.length + ",CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = viewStmt.getGeneratedKeys();
            if (rs.next()) {
                int viewId = rs.getInt(1);
                preparedStmt = conn.prepareStatement("INSERT INTO views_graphs VALUES(?,?)");
                for (int i = 0; i < graphIds.length; i++) {
                    preparedStmt.setInt(1, viewId);
                    preparedStmt.setInt(2, graphIds[i]);
                    preparedStmt.executeUpdate();
                }
            }
        } finally {
            if (viewStmt != null)
                try {
                    viewStmt.close();
                } catch (Exception e) {
                }
            if (preparedStmt != null)
                try {
                    preparedStmt.close();
                } catch (Exception e) {
                }
        }
    }

    public void deleteDefaultServerView(Connection con) throws SQLException {       
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select server_id from servers where ip='localhost' and protocol=1");
            int server_id;
            if (rs.next()) {
                server_id = rs.getInt("server_id");
            } else {
                return;
            }
            rs.close();
            /**
             * 1. Delete all the relation records in the view_graphs
             * 2. Delete all empty view from views
             * 3. Delete all the graphs of the target server
             * 4. Delete the target server record
             */
            stmt.execute("delete from views_graphs where exists (select * from graphs where views_graphs.graph_id = graphs.graph_id and graphs.server_id=" + server_id + ")");
            stmt.execute("delete from views where not exists (select * from views_graphs where views_graphs.view_id=views.view_id)");
            stmt.execute("delete from graphs where server_id=" + server_id);
            stmt.execute("delete from servers where server_id=" + server_id);
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
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
        
        Connection conn = null;
        try {
            serverType = getWebServerType(PortletManager.getKernel().getGBean(WebContainer.class).getClass());
            conn = new DBManager().getConnection();
            initializeDefaultServerView(conn);
        } catch (Exception e) {
            log.error("Initialization failed",e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (Exception e) {
                }
        }
        
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
