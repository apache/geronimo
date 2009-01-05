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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.transaction.UserTransaction;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.monitoring.console.data.Graph;
import org.apache.geronimo.monitoring.console.data.Node;
import org.apache.geronimo.monitoring.console.data.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATS
 */
public class MonitoringPortlet extends GenericPortlet {
    Logger log = LoggerFactory.getLogger(MonitoringPortlet.class);

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

    //annotations don't work in portlets yet, see init method for initialization
    @Resource
    UserTransaction userTransaction;

    @PersistenceContext
    EntityManager entityManager;

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
            String snapshotDuration = actionRequest.getParameter("snapshotDuration");
            String message = startThread(server_id, new Long(snapshotDuration));
            actionResponse.setRenderParameter("message", message);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("snapshotDuration", snapshotDuration);
        } else if (action.equals("disableServer")
                || action.equals("disableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("message", alterServerState(
                    server_id, false));
        } else if (action.equals("enableServer")
                || action.equals("enableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("message", alterServerState(
                    server_id, true));
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("testAddServerConnection")) {
            String name = actionRequest.getParameter("name");
            String ip = actionRequest.getParameter("ip");
            String username = actionRequest.getParameter("username");
            String password = actionRequest.getParameter("password");
            String password2 = actionRequest.getParameter("password2");
            Integer port = Integer.parseInt(actionRequest.getParameter("port"));
            String protocol = actionRequest.getParameter("protocol");
            String message = testConnection(ip, username, password, port, protocol);
            actionResponse.setRenderParameter("message", message);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            actionResponse.setRenderParameter("password", password);
            actionResponse.setRenderParameter("password2", password2);
            actionResponse.setRenderParameter("port", "" + port);
            actionResponse.setRenderParameter("protocol", protocol);
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
            String protocol = actionRequest.getParameter("protocol");
            if (snapshot == null) {
                snapshot = "";
            }
            if (retention == null) {
                retention = "";
            }
            String message = testConnection(ip, username, password, port, protocol);
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

    private String testConnection(String ip, String username,
                                  String password, int port, String protocol) {
        try {
            new MRCConnector(ip, username, password, port, protocol);

            return "<font color=\"green\"><strong><li>Connection was successfully established.</li></strong></font>";
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>Failed to create a connection to server.</li></strong></font>";
        }
    }

    private String alterServerState(String server_id, boolean enable) {
        try {
            userTransaction.begin();
            try {
                Node node = (Node) entityManager.createNamedQuery("nodeByName").setParameter("name", server_id).getSingleResult();
                node.setEnabled(enable);
            } finally {
                userTransaction.commit();
            }
            return "<font color=\"green\"><strong><li>Server " + server_id
                    + " was successfully " + (enable? "enabled":"disabled") + ".</li></strong></font>";
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>[ERROR] Server with server_id = "
                    + server_id
                    + " could not be " + (enable? "enabled":"disabled") + ".</li></strong></font>";
        }
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "showNormal";
        if (action.equals("showView")) {
            addViewAttribute(request, true);
            pageView.include(request, response);
        } else if (action.equals("showAllViews")) {
            request.setAttribute("message", "");
            addAllViewsAttribute(request);
            viewViews.include(request, response);
        } else if (action.equals("showAllServers")) {
            request.setAttribute("message", "");
            addAllNodesAttribute(request);
            viewServers.include(request, response);
        } else if (action.equals("showAllGraphs")) {
            request.setAttribute("message", "");
            addAllGraphsAttribute(request);
            viewGraphs.include(request, response);
        } else if (action.equals("showServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else if (action.equals("startTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String message = startTrackingMbean(server_id, mbean);
            request.setAttribute("message", message);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else if (action.equals("stopTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String message = stopTrackingMbean(server_id, mbean);
            request.setAttribute("message", message);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else if (action.equals("stopThread")) {
            normalView(request, response);
        } else if (action.equals("startThread")) {
            normalView(request, response);
        } else if (action.equals("disableServerViewQuery") || action.equals("enableServerViewQuery")) {
            String server_id = request.getParameter("server_id");
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            request.setAttribute("server_id", server_id);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else {
            normalView(request, response);
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
            addViewAttribute(request, false);
            editView.include(request, response);
        } else if (action.equals("saveEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            addViewAttribute(request, false);
            editView.include(request, response);
        } else if (action.equals("showAddView")) {
            addAllGraphsAttribute(request);
            addView.include(request, response);
        } else if (action.equals("saveAddView")) {
            normalView(request, response);
        } else if (action.equals("showAddGraph")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            String dataname = request.getParameter("dataname");
            request.setAttribute("dataname", dataname);
            addAllNodesAttribute(request);
            addGraph.include(request, response);
        } else if (action.equals("saveAddGraph")) {
            normalView(request, response);
        } else if (action.equals("showEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            addGraphAttribute(request);
            addAllNodesAttribute(request);
            editGraph.include(request, response);
        } else if (action.equals("saveEditGraph")) {
            String graph_id = request.getParameter("graph_id");
            request.setAttribute("graph_id", graph_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            addGraphAttribute(request);
            addAllNodesAttribute(request);
            editGraph.include(request, response);
        } else if (action.equals("deleteGraph")) {
            normalView(request, response);
        } else if (action.equals("deleteView")) {
            normalView(request, response);
        } else if (action.equals("showEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            addNodeAttribute(request);
            editServer.include(request, response);
        } else if (action.equals("saveEditServer")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String message = request.getParameter("message");
            request.setAttribute("message", message);
            addNodeAttribute(request);
            editServer.include(request, response);
        } else if (action.equals("showAddServer")) {
            addServer.include(request, response);
        } else if (action.equals("saveAddServer")) {
            normalView(request, response);
        } else if (action.equals("deleteServer")) {
            normalView(request, response);
        } else if (action.equals("testAddServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request.setAttribute("password2", request.getParameter("password2"));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("port", request.getParameter("port"));
            addServer.include(request, response);
        } else if (action.equals("testEditServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("port", request.getParameter("port"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request.setAttribute("password2", request.getParameter("password2"));
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("server_id", request.getParameter("server_id"));
            request.setAttribute("snapshot", request.getParameter("snapshot"));
            request.setAttribute("retention", request.getParameter("retention"));
            addNodeAttribute(request);
            editServer.include(request, response);
        } else if (action.equals("disableEditServer")
                || action.equals("enableEditServer")) {
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("server_id", request.getParameter("server_id"));
            addNodeAttribute(request);
            editServer.include(request, response);
        } else if (action.equals("disableServer")
                || action.equals("enableServer")) {
            request.setAttribute("server_id", request.getParameter("server_id"));
            normalView(request, response);
        } else {
            //TODO may need to avoid setting message
            normalView(request, response);
        }
    }

    private void normalView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        String message = request.getParameter("message");
        request.setAttribute("message", message);
        addAllViewsAttribute(request);
        addAllNodesAttribute(request);
        addAllGraphsAttribute(request);

        normalView.include(request, response);
    }

    private void addViewAttribute(RenderRequest request, boolean includeGraphs) throws PortletException {
        int viewId = Integer.parseInt(request.getParameter("view_id"));
        try {
            userTransaction.begin();
            try {
                View view = entityManager.find(View.class, viewId);
                request.setAttribute("view", view);
                if (includeGraphs) {
                    List<Graph> graphs = view.getGraphs();
                    GraphsBuilder builder = new GraphsBuilder();
                    List<StatsGraph> statsGraphs = new ArrayList<StatsGraph>();
                    for (Graph graph: graphs) {
                        StatsGraph statsGraph = builder.getStatsGraph(graph);
                        if (statsGraph != null) {
                            statsGraphs.add(statsGraph);
                        }
                    }
                    request.setAttribute("statsGraphs", statsGraphs);
                }
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void addAllViewsAttribute(RenderRequest request) throws PortletException {
        try {
            userTransaction.begin();
            try {
                List<View> views = entityManager.createNamedQuery("allViews").getResultList();
                request.setAttribute("views", views);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void addGraphAttribute(RenderRequest request) throws PortletException {
        try {
            userTransaction.begin();
            try {
                String graphIdString = request.getParameter("graph_id");
                int graphId = Integer.parseInt(graphIdString);
                Graph graph = entityManager.find(Graph.class, graphId);
                request.setAttribute("graph", graph);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void addAllGraphsAttribute(RenderRequest request) throws PortletException {
        try {
            userTransaction.begin();
            try {
                List<Graph> graphs = entityManager.createNamedQuery("allGraphs").getResultList();
                request.setAttribute("graphs", graphs);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void addNodeAttribute(RenderRequest request) throws PortletException {
        request.setAttribute("node", getNodeByName(request.getParameter("server_id")));
    }

    private Node getNodeByName(String name) throws PortletException {
        try {
            userTransaction.begin();
            try {
                return entityManager.find(Node.class, name);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void addAllNodesAttribute(RenderRequest request) throws PortletException {
        try {
            userTransaction.begin();
            try {
                List<Node> nodes = entityManager.createNamedQuery("allNodes").getResultList();
                request.setAttribute("nodes", nodes);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void updateView(ActionRequest actionRequest,
                            ActionResponse actionResponse) {
        String view_id = actionRequest.getParameter("view_id");
        actionResponse.setRenderParameter("view_id", view_id);

        try {
            userTransaction.begin();
            try {
                View view = entityManager.find(View.class, Integer.parseInt(view_id));
                view.setName(actionRequest.getParameter("name"));
                view.setDescription(actionRequest.getParameter("description"));
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>View " + actionRequest.getParameter("name")
                            + " has been updated</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error updating View "
                            + actionRequest.getParameter("name") + "</li></strong></font>" + e.getMessage());
        }

    }

    private void addView(ActionRequest actionRequest,
                         ActionResponse actionResponse) {
        String name = actionRequest.getParameter("name");
        try {
            userTransaction.begin();
            try {
                View view = new View();
                view.setName(name);
                view.setDescription(actionRequest.getParameter("description"));
                String[] graphsArray = actionRequest.getParameterValues("graph_ids");
                if (graphsArray != null) {
                    for (String graphIdString: graphsArray) {
                        int graphId = Integer.parseInt(graphIdString);
                        Graph graph = entityManager.find(Graph.class, graphId);
                        view.getGraphs().add(graph);
                        graph.getViews().add(view);
                    }
                }
                entityManager.persist(view);
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>View " + name
                            + " has been added</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding View " + name
                            + "</li></strong></font>" + e.getMessage());
        }
    }

    private void updateServer(ActionRequest actionRequest,
                              ActionResponse actionResponse) {
        String server_id = actionRequest.getParameter("server_id");
        actionResponse.setRenderParameter("server_id", server_id);

        try {
            userTransaction.begin();
            try {
                Node node = entityManager.find(Node.class, server_id);
                node.setName(actionRequest.getParameter("name"));
                node.setHost(actionRequest.getParameter("ip"));
                node.setUserName(actionRequest.getParameter("username"));
                String password = actionRequest.getParameter("password");
                if (password != null && !password.equals("")) {
                    password = EncryptionManager.encrypt(password);
                    node.setPassword(password);
                }
                node.setPort(Integer.parseInt(actionRequest.getParameter("port")));
                node.setProtocol(actionRequest.getParameter("protocol"));
                //TODO retention??
                String snapshot = actionRequest.getParameter("snapshot");
                String retention = actionRequest.getParameter("retention");
                if (snapshot != null && retention != null) {
                    MRCConnector connector = new MRCConnector(node);
                    connector.setSnapshotDuration(Long.parseLong(snapshot) * 1000 * 60);
                    connector.setSnapshotRetention(Integer.parseInt(retention));
                    //close?
                }
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter(
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
        try {
            userTransaction.begin();
            String name = actionRequest.getParameter("name");
            String host = actionRequest.getParameter("ip");
            try {
                Node node = new Node();
                node.setName(name);
                node.setHost(host);
                node.setUserName(actionRequest.getParameter("username"));
                String password = actionRequest.getParameter("password");
                if (password != null && !password.equals("")) {
                    password = EncryptionManager.encrypt(password);
                    node.setPassword(password);
                }
                node.setPort(Integer.parseInt(actionRequest.getParameter("port")));
                node.setProtocol(actionRequest.getParameter("protocol"));
                entityManager.persist(node);
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>Server " + name + " at "
                            + host + " has been added.</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding server</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void deleteServer(ActionRequest actionRequest,
                              ActionResponse actionResponse) {
        String server_id = actionRequest.getParameter("server_id");
        actionResponse.setRenderParameter("server_id", server_id);

        try {
            userTransaction.begin();
            try {
                Node node = entityManager.find(Node.class, server_id);
                entityManager.remove(node);
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter(
                            "message",
                            "<font color=\"green\"><strong><li>Server and associated graphs have been deleted</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting server</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void deleteView(ActionRequest actionRequest,
                            ActionResponse actionResponse) {
        String view_id = actionRequest.getParameter("view_id");
        actionResponse.setRenderParameter("view_id", view_id);

        try {
            userTransaction.begin();
            try {
                View view = entityManager.find(View.class, Integer.parseInt(view_id));
                entityManager.remove(view);
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter("message",
                            "<font color=\"green\"><strong><li>View has been deleted</li></strong></font>");
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting view</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void addGraph(ActionRequest actionRequest,
                          ActionResponse actionResponse) {
        Graph graph = new Graph();

        updateGraphFromRequest(actionRequest, graph);
        try {
            userTransaction.begin();
            try {
                Node node = entityManager.find(Node.class, actionRequest.getParameter("server_id"));
                graph.setNode(node);
                entityManager.persist(graph);
            } finally {
                userTransaction.commit();
            }
            actionResponse.setRenderParameter("message",
                    "<font color=\"green\"><strong><li>Graph " + graph.getGraphName1()
                            + " has been added.</li></strong></font>");

        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error adding graph</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void updateGraphFromRequest(ActionRequest actionRequest, Graph graph) {
        graph.setGraphName1(actionRequest.getParameter("name"));
        graph.setDescription(actionRequest.getParameter("description"));
        graph.setXlabel(actionRequest.getParameter("xlabel"));
        graph.setYlabel(actionRequest.getParameter("ylabel"));
        graph.setTimeFrame(Integer.parseInt(actionRequest.getParameter("timeframe")));
        graph.setMBeanName(actionRequest.getParameter("mbean"));
        graph.setDataName1(actionRequest.getParameter("dataname1"));
        graph.setData1operation(actionRequest.getParameter("data1operation").charAt(0));

        graph.setOperation(actionRequest.getParameter("operation"));
        if (graph.getOperation().equals("other")) {
            graph.setOperation(actionRequest.getParameter("othermath"));
        }

        graph.setShowArchive(actionRequest.getParameter("showArchive") != null
                && actionRequest.getParameter("showArchive").equals("on"));

        graph.setDataName2(actionRequest.getParameter("dataname2"));
        graph.setData2operation(actionRequest.getParameter("data2operation") == null? 'A': actionRequest.getParameter("data2operation").charAt(0));
    }

    private void updateGraph(ActionRequest actionRequest,
                             ActionResponse actionResponse) {
        String graph_id = actionRequest.getParameter("graph_id");
        actionResponse.setRenderParameter("graph_id", graph_id);
        try {
            userTransaction.begin();
            try {
                Graph graph = entityManager.find(Graph.class, Integer.parseInt(graph_id));
                Node node = entityManager.find(Node.class, actionRequest.getParameter("server_id"));
                graph.setNode(node);
                updateGraphFromRequest(actionRequest, graph);
                actionResponse.setRenderParameter("message",
                        "<font color=\"green\"><strong><li>Graph " + graph.getGraphName1()
                                + " has been updated.</li></strong></font>");
            } finally {
                userTransaction.commit();
            }

        } catch (Exception e) {
            log.info("error updating graph", e);
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error updating graph</li></strong></font>"
                            + e.getMessage());
        }
    }

    private void deleteGraph(ActionRequest actionRequest,
                             ActionResponse actionResponse) {
        String graph_id = actionRequest.getParameter("graph_id");
        actionResponse.setRenderParameter("graph_id", graph_id);
        try {
            userTransaction.begin();
            try {
                Graph graph = entityManager.find(Graph.class, Integer.parseInt(graph_id));
                entityManager.remove(graph);
                actionResponse.setRenderParameter("message",
                        "<font color=\"green\"><strong><li>Graph " + graph.getGraphName1()
                                + " has been deleted.</li></strong></font>");
            } finally {
                userTransaction.commit();
            }

        } catch (Exception e) {
            log.info("error deleting graph", e);
            actionResponse.setRenderParameter("message",
                    "<font color=\"red\"><strong><li>Error deleting graph</li></strong></font>"
                            + e.getMessage());
        }
    }

    private String startTrackingMbean(String server_id, String mbean) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        MRCConnector mrc;
        try {
            mrc = new MRCConnector(node);
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + node.getHost()
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";

        }

        // tell the mrc server to start tracking an mbean
        try {
            if (mrc.startTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"green\"><strong><li>MBean " + mbarr[1]
                        + " tracking on server " + node.getName()
                        + "</li></strong></font>";
            } else {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"red\"><strong><li>ERROR: MBean "
                        + mbarr[1] + " could <b>NOT</b> be tracked on server "
                        + node.getName() + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: MBean " + mbean
                    + " could <b>NOT</b> be tracked on server " + node.getHost()
                    + ": " + e.getMessage() + "</li></strong></font>";
        }
    }

    private String stopTrackingMbean(String server_id, String mbean) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            return "<font color=\"red\"><strong><li>DATABASE ERROR: "
                    + e.getMessage() + "</li></strong></font>";
        }
        MRCConnector mrc;
        try {
            mrc = new MRCConnector(node);
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Unable to connect to server "
                    + node.getHost()
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
                        + node.getName() + "</li></strong></font>";
            } else {
                String mbarr[] = mbean.split("name=");
                return "<font color=\"red\"><strong><li>ERROR: MBean "
                        + mbarr[1]
                        + " could <b>NOT</b> be removed from tracking on server "
                        + node.getName() + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: MBean " + mbean
                    + " could <b>NOT</b> be removed from tracking on server "
                    + node.getHost() + ": " + e.getMessage()
                    + "</li></strong></font>";
        }
    }

    private String stopThread(String server_id) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            log.info("error", e);
            return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
        }
        try {
            MRCConnector mrc = new MRCConnector(node);
            if (mrc.stopSnapshotThread()) {
                return "<font color=\"green\"><strong><li>Snapshot thread stopped on server "
                        + server_id + "</li></strong></font>";
            } else {
                return "<font color=\"red\"><strong><li>ERROR: Snapshot thread could <b>NOT</b> be stopped on server "
                        + server_id + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Snapshot thread could <b>NOT</b> be stopped on server "
                    + server_id
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";

        }
    }

    private String startThread(String server_id, Long snapshotDuration) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            log.info("error", e);
            return "<font color=\"red\"><strong><li>DATABASE ERROR: Server id "
                        + server_id
                        + " not found in database</li></strong></font>";
        }
        try {
            MRCConnector mrc = new MRCConnector(node);
            if (mrc.startSnapshotThread(snapshotDuration)) {
                return "<font color=\"green\"><strong><li>Snapshot thread started on server "
                        + server_id + "</li></strong></font>";
            } else {
                return "<font color=\"red\"><strong><li>ERROR: Snapshot thread could <b>NOT</b> be started on server "
                        + server_id + "</li></strong></font>";
            }
        } catch (Exception e) {
            return "<font color=\"red\"><strong><li>MRC ERROR: Snapshot thread could <b>NOT</b> be started on server "
                    + server_id
                    + ": "
                    + e.getMessage()
                    + "</li></strong></font>";

        }
    }

    @Override
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(NORMALVIEW_JSP);
        viewViews = portletConfig.getPortletContext().getRequestDispatcher(VIEWVIEWS_JSP);
        viewServers = portletConfig.getPortletContext().getRequestDispatcher(VIEWSERVERS_JSP);
        viewGraphs = portletConfig.getPortletContext().getRequestDispatcher(VIEWGRAPHS_JSP);
        pageView = portletConfig.getPortletContext().getRequestDispatcher(PAGEVIEW_JSP);
        editView = portletConfig.getPortletContext().getRequestDispatcher(EDITVIEW_JSP);
        addView = portletConfig.getPortletContext().getRequestDispatcher(ADDVIEW_JSP);
        addGraph = portletConfig.getPortletContext().getRequestDispatcher(ADDGRAPH_JSP);
        editGraph = portletConfig.getPortletContext().getRequestDispatcher(EDITGRAPH_JSP);
        viewServer = portletConfig.getPortletContext().getRequestDispatcher(VIEWSERVER_JSP);
        editServer = portletConfig.getPortletContext().getRequestDispatcher(EDITSERVER_JSP);
        addServer = portletConfig.getPortletContext().getRequestDispatcher(ADDSERVER_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(HELPVIEW_JSP);
        editNormalView = portletConfig.getPortletContext().getRequestDispatcher(EDITNORMALVIEW_JSP);

        try {
            Context ctx = new InitialContext();
            userTransaction = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
            entityManager = (EntityManager) ctx.lookup("java:comp/env/jpa/monitoring");
        } catch (NamingException e) {
            throw new PortletException(e);
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
