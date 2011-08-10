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
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.transaction.UserTransaction;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.monitoring.console.data.Graph;
import org.apache.geronimo.monitoring.console.data.Node;
import org.apache.geronimo.monitoring.console.data.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STATS
 */
public class MonitoringPortlet extends BasePortlet {
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
    
    private String server;        

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
            stopThread(server_id, actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("startThread")
                || action.equals("enableServerViewQuery")) {
            String server_id = actionRequest.getParameter("server_id");
            String snapshotDuration = actionRequest.getParameter("snapshotDuration");
            startThread(server_id, new Long(snapshotDuration), actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
            actionResponse.setRenderParameter("snapshotDuration", snapshotDuration);
        } else if (action.equals("disableServer")
                || action.equals("disableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            actionResponse.setRenderParameter("server_id", server_id);
            alterServerState(server_id, false, actionRequest);
        } else if (action.equals("enableServer")
                || action.equals("enableEditServer")) {
            String server_id = actionRequest.getParameter("server_id");
            alterServerState(server_id, true, actionRequest);
            actionResponse.setRenderParameter("server_id", server_id);
        } else if (action.equals("testAddServerConnection")) {
            String name = actionRequest.getParameter("name");
            String ip = actionRequest.getParameter("ip");
            String username = actionRequest.getParameter("username");
            String password = actionRequest.getParameter("password");
            String password2 = actionRequest.getParameter("password2");
            Integer port = Integer.parseInt(actionRequest.getParameter("port"));
            String protocol = actionRequest.getParameter("protocol");
            testConnection(ip, username, password, port, protocol, actionRequest);
            actionResponse.setRenderParameter("name", name);
            actionResponse.setRenderParameter("username", username);
            actionResponse.setRenderParameter("ip", ip);
            //  Don't return the password in the output
//            actionResponse.setRenderParameter("password", password);
//            actionResponse.setRenderParameter("password2", password2);
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
            restoreData(actionRequest,actionResponse);
        }
    }

    private void testConnection(String ip, String username,
                                  String password, int port, String protocol, PortletRequest request) {
        MRCConnector mrc = null;
        try {
            mrc = new MRCConnector(ip, username, password, port, protocol);
            addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg01"));
        } catch (Exception e) {
            addInfoMessage(request, getLocalizedString(request, "mconsole.errorMsg01"), e.getMessage());
        } finally
        {
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void alterServerState(String server_id, boolean enable, PortletRequest request) {
        try {
            userTransaction.begin();
            try {
                Node node = (Node) entityManager.createNamedQuery("nodeByName").setParameter("name", server_id).getSingleResult();
                node.setEnabled(enable);
            } finally {
                userTransaction.commit();
            }
            if (enable) {
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg02", server_id));
            }
            else {
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg03", server_id));
            }
        } catch (Exception e) {
            if (enable) {
                addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg02", server_id), e.getMessage());
            }
            else {
            	addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg03", server_id), e.getMessage());
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
            addViewAttribute(request, true);
            pageView.include(request, response);
        } else if (action.equals("showAllViews")) {
            addAllViewsAttribute(request);
            viewViews.include(request, response);
        } else if (action.equals("showAllServers")) {
            addAllNodesAttribute(request);
            viewServers.include(request, response);
        } else if (action.equals("showAllGraphs")) {
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
            startTrackingMbean(server_id, mbean, request);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else if (action.equals("stopTrackingMbean")) {
            String server_id = request.getParameter("server_id");
            request.setAttribute("server_id", server_id);
            String mbean = request.getParameter("mbean");
            request.setAttribute("mbean", mbean);
            stopTrackingMbean(server_id, mbean, request);
            addNodeAttribute(request);
            viewServer.include(request, response);
        } else if (action.equals("stopThread")) {
            normalView(request, response);
        } else if (action.equals("startThread")) {
            normalView(request, response);
        } else if (action.equals("disableServerViewQuery") || action.equals("enableServerViewQuery")) {
            String server_id = request.getParameter("server_id");
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
        if(request.isUserInRole("admin")){
        if (action.equals("showEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            addViewAttribute(request, false);
            addAllGraphsAttribute(request);
            editView.include(request, response);
        } else if (action.equals("saveEditView")) {
            String view_id = request.getParameter("view_id");
            request.setAttribute("view_id", view_id);
            addViewAttribute(request, false);
            addAllGraphsAttribute(request);
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
            request.setAttribute("protocol", request.getParameter("protocol"));
            request.setAttribute("port", request.getParameter("port"));
            addServer.include(request, response);
        } else if (action.equals("testEditServerConnection")) {
            request.setAttribute("name", request.getParameter("name"));
            request.setAttribute("ip", request.getParameter("ip"));
            request.setAttribute("port", request.getParameter("port"));
            request.setAttribute("username", request.getParameter("username"));
            request.setAttribute("password", request.getParameter("password"));
            request.setAttribute("password2", request.getParameter("password2"));
            request.setAttribute("server_id", request.getParameter("server_id"));
            request.setAttribute("snapshot", request.getParameter("snapshot"));
            request.setAttribute("retention", request.getParameter("retention"));
            addNodeAttribute(request);
            editServer.include(request, response);
        } else if (action.equals("disableEditServer")
                || action.equals("enableEditServer")) {
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
        }//end request.isUserInRole("admin")
    }

    private void normalView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
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
                       if(!graph.getNode().isEnabled())
                           continue;
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
                view.setDescription(actionRequest.getParameter("minxss_description"));
                for(Graph graph: view.getGraphs())
                {
                    for(View view_in : graph.getViews())
                    {
                        if(view_in.getId() == view.getId())
                        {//No grantee for that view and view_in have the same reference?
                            graph.getViews().remove(view_in);
                            break;
                        }
                    }
                }
                view.getGraphs().clear();
                
                String[] graphsArray = actionRequest.getParameterValues("graph_ids");
                if (graphsArray != null) {
                    for (String graphIdString: graphsArray) {
                        int graphId = Integer.parseInt(graphIdString);
                        Graph graph = entityManager.find(Graph.class, graphId);
                        view.getGraphs().add(graph);
                        graph.getViews().add(view);
                    }
                }
            } finally {
                userTransaction.commit();
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg08", actionRequest.getParameter("name")));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg11", actionRequest.getParameter("name")), e.getMessage());
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
                view.setDescription(actionRequest.getParameter("minxss_description"));
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg09", name));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg12", name), e.getMessage());
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
                    connector.dispose();
                }
            } finally {
                userTransaction.commit();
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg10"));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg13"), e.getMessage());
        }
    }

    private void addServer(ActionRequest actionRequest,
                           ActionResponse actionResponse) {
        try {
            String name = actionRequest.getParameter("name");
            String host = actionRequest.getParameter("ip");
            if (entityManager.find(Node.class, name) != null) {
                final String message = "The server name already exists. Please use a different one.";
                throw new Exception(message);
            }
            try {
                userTransaction.begin();
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg11", name, host));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg14"), e.getMessage());
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
                // check if there is any graph created against the node before delete it.
                List<Graph> graphs = entityManager.createNamedQuery("graphsByNode").setParameter("name", node.getName()).getResultList();
                if (!(graphs == null || graphs.isEmpty())) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg20"));
                    return;
                }
                // check whether the snapshot query is enabled, if does, close it first
                MRCConnector mrc = null;
                try {
                    mrc = new MRCConnector(node);
                    if (mrc.isSnapshotRunning() == 1) {
                        if (mrc.stopSnapshotThread()) {
                            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg06", server_id));
                        } else {
                            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg09", server_id));
                        }
                    }
                    
                } catch (Exception e) {
                } finally {
                    if(null != mrc)mrc.dispose();
                }
                entityManager.remove(node);
            } finally {
                userTransaction.commit();
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg12"));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg15"), e.getMessage());
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg13"));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg16"), e.getMessage());
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
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg14", graph.getGraphName1()));
        } catch (Exception e) {
        	addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg17"), e.getMessage());
        }
    }

    private void updateGraphFromRequest(ActionRequest actionRequest, Graph graph) {
        graph.setGraphName1(actionRequest.getParameter("name"));
        graph.setDescription(actionRequest.getParameter("minxss_description"));
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
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg15", graph.getGraphName1()));
            } finally {
                userTransaction.commit();
            }

        } catch (Exception e) {
            log.info("error updating graph", e);
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg18"), e.getMessage());
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
                addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg16", graph.getGraphName1()));
            } finally {
                userTransaction.commit();
            }

        } catch (Exception e) {
            log.error("error deleting graph", e);
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg19"), e.getMessage());
        }
    }
    
    private void restoreData(ActionRequest actionRequest, ActionResponse actionResponse) {
        try {
            if (server == null) {
                server = getWebServerType(PortletManager.getKernel().getGBean(WebContainer.class).getClass());
            }
            deleteDefaultServerView(actionRequest, actionResponse);
            initializeDefaultServerView(server);
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg17"));
        } catch (Exception e) {
            log.error("error deleting graph", e);
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.errorMsg21"), e.getMessage());
        }
    }


    private void startTrackingMbean(String server_id, String mbean, PortletRequest request) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            addInfoMessage(request, getLocalizedString(request, "mconsole.errorMsg04"), e.getMessage());
            return;
        }
        MRCConnector mrc =null;
        try {
            mrc = new MRCConnector(node);
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg05", node.getHost()), e.getMessage());
            return;
        }

        // tell the mrc server to start tracking an mbean
        try {
            if (mrc.startTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg04", mbarr[1], node.getHost()));
            } else {
                String mbarr[] = mbean.split("name=");
                addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg06", mbarr[1], node.getHost()));
            }
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg06", mbean, node.getHost()), e.getMessage());
        }
        mrc.dispose();
    }

    private void stopTrackingMbean(String server_id, String mbean, PortletRequest request) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            addInfoMessage(request, getLocalizedString(request, "mconsole.errorMsg04"), e.getMessage());
            return;
        }
        MRCConnector mrc;
        try {
            mrc = new MRCConnector(node);
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg05", node.getHost()), e.getMessage());
            return;

        }
        // tell the mrc-server to stop tracking some mbean
        try {
            if (mrc.stopTrackingMbean(mbean)) {
                String mbarr[] = mbean.split("name=");
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg05", mbarr[1], node.getHost()));
            } else {
                String mbarr[] = mbean.split("name=");
                addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg06", mbarr[1], node.getHost()));
            }
        } catch (Exception e) {
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg06", mbean, node.getHost()), e.getMessage());
        }
        mrc.dispose();
    }

    private void stopThread(String server_id, PortletRequest request) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            log.info("error", e);
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg08", server_id), e.getMessage());
            return;
        }
        MRCConnector mrc = null;
        try {
            mrc = new MRCConnector(node);
            if (mrc.stopSnapshotThread()) {
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg06", server_id));
            } else {
            	addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg09", server_id));
            }
        } catch (Exception e) {
        	addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg09", server_id), e.getMessage());
        } finally {
            if(null != mrc)
                mrc.dispose();
        }
    }

    private void startThread(String server_id, Long snapshotDuration, PortletRequest request) {
        Node node;
        try {
            node = getNodeByName(server_id);
        } catch (PortletException e) {
            log.info("error", e);
            addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg08", server_id), e.getMessage());
            return;
        }
        MRCConnector mrc = null;
        try {
            mrc = new MRCConnector(node);
            if (mrc.startSnapshotThread(snapshotDuration)) {
                addInfoMessage(request, getLocalizedString(request, "mconsole.infoMsg07", server_id));
            } else {
            	addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg10", server_id));
            }
        } catch (Exception e) {
        	addErrorMessage(request, getLocalizedString(request, "mconsole.errorMsg10", server_id), e.getMessage());

        } finally {
            if(null != mrc)mrc.dispose();
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
            server= getWebServerType(PortletManager.getKernel().getGBean(WebContainer.class).getClass());
            if (!isPredefinedServerViewExist()) {
                initializeDefaultServerView(server);
            }
        } catch (NamingException e) {
            throw new PortletException(e);
        }catch (Exception e) {
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

    private void initializeDefaultServerView(String server) throws Exception {
        if (server == null) {
            throw new Exception("Unrecognized web server type");
        } else if (server.equalsIgnoreCase("tomcat")) {
            initializeDefaultTomatServerView();
        } else if (server.equalsIgnoreCase("jetty")) {
            initializeDefaultJettyServerView();
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

    private void initializeDefaultTomatServerView() throws Exception {
        String tomcatWebConnectorObjectName = getObjectNameByShortName("TomcatWebConnector");
        String tomcatWebSSLConnectorObjectName = getObjectNameByShortName("TomcatWebSSLConnector");
        try {
            userTransaction.begin();
            Node node = persistDefaultServer();
            if (tomcatWebConnectorObjectName != null) {
                Graph[] webConnectorGraphs = new Graph[15];
                webConnectorGraphs[0] = createGraph(node, "JVM Heap Size Current", "JVM Heap Size Current", "60", getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", null, null);
                webConnectorGraphs[1] = createGraph(node, "Active Request Count", "Active Request Count", "60", tomcatWebConnectorObjectName, "Active Request Count", "Active Request Count", "Time", "A", "", "A", null, null);
                webConnectorGraphs[2] = createGraph(node, "Busy Threads Max", "Busy Threads Max", "60", tomcatWebConnectorObjectName, "Busy Threads Max", "Busy Threads Max", "Time", "A", "", "A", null, null);
                webConnectorGraphs[3] = createGraph(node, "Busy Threads Current", "Busy Threads Current", "60", tomcatWebConnectorObjectName, "Busy Threads Current", "Busy Threads Current", "Time", "A", "", "A", null, null);
                webConnectorGraphs[4] = createGraph(node, "Busy Threads Min", "Busy Threads Min", "60", tomcatWebConnectorObjectName, "Busy Threads Min", "Busy Threads Min", "Time", "A", "", "A", null, null);
                webConnectorGraphs[5] = createGraph(node, "Bytes Received", "Bytes Received", "60", tomcatWebConnectorObjectName, "Bytes Received", "Bytes Received", "Time", "A", "", "A", null, null);
                webConnectorGraphs[6] = createGraph(node, "Bytes Sent", "Bytes Sent", "60", tomcatWebConnectorObjectName, "Bytes Sent", "Bytes Sent", "Time", "A", "", "A", null, null);
                webConnectorGraphs[7] = createGraph(node, "Error Count", "Error Count", "60", tomcatWebConnectorObjectName, "Error Count", "Error Count", "Time", "A", "", "A", null, null);
                webConnectorGraphs[8] = createGraph(node, "Open Connections Current", "Open Connections Current", "60", tomcatWebConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", null, null);
                webConnectorGraphs[9] = createGraph(node, "Open Connections Max", "Open Connections Max", "60", tomcatWebConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", null, null);
                webConnectorGraphs[10] = createGraph(node, "Open Connections Min", "Open Connections Min", "60", tomcatWebConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", null, null);
                webConnectorGraphs[11] = createGraph(node, "Request Time CurrentTime", "Request Time CurrentTime", "60", tomcatWebConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", null, null);
                webConnectorGraphs[12] = createGraph(node, "Request Time MaxTime", "Request Time MaxTime", "60", tomcatWebConnectorObjectName, "Request Time MaxTime", "Request Time MaxTime", "Time", "A", "", "A", null, null);
                webConnectorGraphs[13] = createGraph(node, "Request Time MinTime", "Request Time MinTime", "60", tomcatWebConnectorObjectName, "Request Time MinTime", "Request Time MinTime", "Time", "A", "", "A", null, null);
                webConnectorGraphs[14] = createGraph(node, "Request Time TotalTime", "Request Time TotalTime", "60", tomcatWebConnectorObjectName, "Request Time TotalTime", "Request Time TotalTime", "Time", "A", "", "A", null, null);
                persistView("TomcatWebConnector View", "TomcatWebConnector View", webConnectorGraphs);
            }
            if (tomcatWebSSLConnectorObjectName != null) {
                Graph[] webSSLConnectorGraphs = new Graph[15];               
                webSSLConnectorGraphs[0] = createGraph(node, "JVM Heap Size Current", "JVM Heap Size Current", "60", getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[1] = createGraph(node, "SSL Active Request Count", "Active Request Count", "60", tomcatWebSSLConnectorObjectName, "Active Request Count", "Active Request Count", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[2] = createGraph(node, "SSL Busy Threads Current", "Busy Threads Current", "60", tomcatWebSSLConnectorObjectName, "Busy Threads Current", "Busy Threads Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[3] = createGraph(node, "SSL Busy Threads Max", "Busy Threads Max", "60", tomcatWebSSLConnectorObjectName, "Busy Threads Max", "Busy Threads Max", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[4] = createGraph(node, "SSL Busy Threads Min", "Busy Threads Min", "60", tomcatWebSSLConnectorObjectName, "Busy Threads Min", "Busy Threads Min", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[5] = createGraph(node, "SSL Bytes Received", "Bytes Received", "60", tomcatWebSSLConnectorObjectName, "Bytes Received", "Bytes Received", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[6] = createGraph(node, "SSL Bytes Sent", "Bytes Sent", "60", tomcatWebSSLConnectorObjectName, "Bytes Sent", "Bytes Sent", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[7] = createGraph(node, "SSL Error Count", "Error Count", "60", tomcatWebSSLConnectorObjectName, "Error Count", "Error Count", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[8] = createGraph(node, "SSL Open Connections Current", "Open Connections Current", "60", tomcatWebSSLConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[9] = createGraph(node, "SSL Open Connections Max", "Open Connections Max", "60", tomcatWebSSLConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[10] = createGraph(node, "SSL Open Connections Min", "Open Connections Min", "60", tomcatWebSSLConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[11] = createGraph(node, "SSL Request Time CurrentTime", "Request Time CurrentTime", "60", tomcatWebSSLConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[12] = createGraph(node, "SSL Request Time MaxTime", "Request Time MaxTime", "60", tomcatWebSSLConnectorObjectName, "Request Time MaxTime", "Request Time MaxTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[13] = createGraph(node, "SSL Request Time MinTime", "Request Time MinTime", "60", tomcatWebSSLConnectorObjectName, "Request Time MinTime", "Request Time MinTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[14] = createGraph(node, "SSL Request Time TotalTime", "Request Time TotalTime", "60", tomcatWebSSLConnectorObjectName, "Request Time TotalTime", "Request Time TotalTime", "Time", "A", "", "A", "", null);               
                persistView("TomcatWebSSLConnector View", "TomcatWebSSLConnector View", webSSLConnectorGraphs);
            }
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        } finally {
            userTransaction.commit();
        }
    }
    
    private void initializeDefaultJettyServerView() throws Exception {
        String jettyWebConnectorObjectName = getObjectNameByShortName("JettyWebConnector");
        String jettyWebSSLConnectorObjectName = getObjectNameByShortName("JettySSLConnector");
        try {
            userTransaction.begin();
            Node node = persistDefaultServer();
            if (jettyWebConnectorObjectName != null) {
                Graph[] webConnectorGraphs = new Graph[12];
                webConnectorGraphs[0] = createGraph(node, "JVM Heap Size Current", "JVM Heap Size Current", "60", getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", "", null);
                webConnectorGraphs[1] = createGraph(node, "Connections Duration Count", "Connections Duration Count", "60", jettyWebConnectorObjectName, "Connections Duration Count", "Connections Duration Count", "Time", "A", "", "A", "", null);
                webConnectorGraphs[2] = createGraph(node, "Connections Duration MaxTime", "Connections Duration MaxTime", "60", jettyWebConnectorObjectName, "Connections Duration MaxTime", "Connections Duration MaxTime", "Time", "A", "", "A", "", null);
                webConnectorGraphs[3] = createGraph(node, "Connections Duration MinTime", "Connections Duration MinTime", "60", jettyWebConnectorObjectName, "Connections Duration MinTime", "Connections Duration MinTime", "Time", "A", "", "A", "", null);
                webConnectorGraphs[4] = createGraph(node, "Connections Duration TotalTime", "Connections Duration TotalTime", "60", jettyWebConnectorObjectName, "Connections Duration TotalTime", "Connections Duration TotalTime", "Time", "A", "", "A", "", null);
                webConnectorGraphs[5] = createGraph(node, "Connections Request Current", "Connections Request Current", "60", jettyWebConnectorObjectName, "Connections Request Current", "Connections Request Current", "Time", "A", "", "A", "", null);
                webConnectorGraphs[6] = createGraph(node, "Connections Request Max", "Connections Request Max", "60", jettyWebConnectorObjectName, "Connections Request Max", "Connections Request Max", "Time", "A", "", "A", "", null);
                webConnectorGraphs[7] = createGraph(node, "Connections Request Min", "Connections Request Min", "60", jettyWebConnectorObjectName, "Connections Request Min", "Connections Request Min", "Time", "A", "", "A", "", null);
                webConnectorGraphs[8] = createGraph(node, "Open Connections Current", "Open Connections Current", "60", jettyWebConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", "", null);
                webConnectorGraphs[9] = createGraph(node, "Open Connections Max", "Open Connections Max", "60", jettyWebConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", "", null);
                webConnectorGraphs[10] = createGraph(node, "Open Connections Min", "Open Connections Min", "60", jettyWebConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", "", null);
                webConnectorGraphs[11] = createGraph(node, "Request Time CurrentTime", "Request Time CurrentTime", "60", jettyWebConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", "", null);
                persistView("JettyWebConnector View", "JettyWebConnector View", webConnectorGraphs);
            }
            if (jettyWebSSLConnectorObjectName != null) {
                Graph[] webSSLConnectorGraphs = new Graph[12];
                webSSLConnectorGraphs[0] = createGraph(node, "JVM Heap Size Current", "JVM Heap Size Current", "60", getObjectNameByShortName("JVM"), "JVM Heap Size Current", "JVM Heap Size Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[1] = createGraph(node, "Connections Duration Count", "Connections Duration Count", "60", jettyWebSSLConnectorObjectName, "Connections Duration Count", "Connections Duration Count", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[2] = createGraph(node, "Connections Duration MaxTime", "Connections Duration MaxTime", "60", jettyWebSSLConnectorObjectName, "Connections Duration MaxTime", "Connections Duration MaxTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[3] = createGraph(node, "Connections Duration MinTime", "Connections Duration MinTime", "60", jettyWebSSLConnectorObjectName, "Connections Duration MinTime", "Connections Duration MinTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[4] = createGraph(node, "Connections Duration TotalTime", "Connections Duration TotalTime", "60", jettyWebSSLConnectorObjectName, "Connections Duration TotalTime", "Connections Duration TotalTime", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[5] = createGraph(node, "Connections Request Current", "Connections Request Current", "60", jettyWebSSLConnectorObjectName, "Connections Request Current", "Connections Request Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[6] = createGraph(node, "Connections Request Max", "Connections Request Max", "60", jettyWebSSLConnectorObjectName, "Connections Request Max", "Connections Request Max", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[7] = createGraph(node, "Connections Request Min", "Connections Request Min", "60", jettyWebSSLConnectorObjectName, "Connections Request Min", "Connections Request Min", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[8] = createGraph(node, "Open Connections Current", "Open Connections Current", "60", jettyWebSSLConnectorObjectName, "Open Connections Current", "Open Connections Current", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[9] = createGraph(node, "Open Connections Max", "Open Connections Max", "60", jettyWebSSLConnectorObjectName, "Open Connections Max", "Open Connections Max", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[10] = createGraph(node, "Open Connections Min", "Open Connections Min", "60", jettyWebSSLConnectorObjectName, "Open Connections Min", "Open Connections Min", "Time", "A", "", "A", "", null);
                webSSLConnectorGraphs[11] = createGraph(node, "Request Time CurrentTime", "Request Time CurrentTime", "60", jettyWebSSLConnectorObjectName, "Request Time CurrentTime", "Request Time CurrentTime", "Time", "A", "", "A", "", null);
                persistView("JettySSLConnector View", "JettySSLConnector View", webSSLConnectorGraphs);
            }
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        } finally {
            userTransaction.commit();
        }
    }
    
    private Node persistDefaultServer() throws Exception {
        Node node = new Node();
        node.setName("Default Server");
        node.setHost("localhost");
        node.setUserName("");
        node.setPassword("");
        node.setPort(1099);
        node.setProtocol("JMX");
        entityManager.persist(node);
        return node;
    }
    
    private Graph createGraph(Node node, String name, String description, String timeframe, String mbean, String dataname1, String xlabel, String ylabel, String data1operation, String operation,
            String data2operation, String dataname2, String showArchive) throws Exception {
        Graph graph = new Graph();
        graph.setNode(node);
        graph.setGraphName1(name);
        graph.setDescription(description);
        graph.setXlabel(xlabel);
        graph.setYlabel(ylabel);
        graph.setTimeFrame(Integer.parseInt(timeframe));
        graph.setMBeanName(mbean);
        graph.setDataName1(dataname1);
        graph.setData1operation(data1operation.charAt(0));
        graph.setOperation(operation);
        graph.setShowArchive(showArchive != null && showArchive.equals("on"));
        graph.setDataName2(dataname2);
        graph.setData2operation(data2operation == null ? 'A' : data2operation.charAt(0));
        return graph;
    }
    
    private View persistView(String name, String description, Graph[] graphsArray) throws Exception {
        View view = new View();
        view.setName(name);
        view.setDescription(description);
        if (graphsArray != null) {
            for (Graph graph : graphsArray) {
                view.getGraphs().add(graph);
                graph.getViews().add(view);
            }
        }
        entityManager.persist(view);
        return view;
    }
    
    private boolean isPredefinedServerViewExist() {
        Node node = entityManager.find(Node.class, "Default Server");
        return node != null;
    }
    
    private void deleteDefaultServerView(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        try {
            userTransaction.begin();
            Node node = entityManager.find(Node.class, "Default Server");
            if (node != null) {
                List<Graph> graphs = entityManager.createNamedQuery("graphsByNode").setParameter("name", node.getName()).getResultList();
                if (!(graphs == null || graphs.isEmpty())) {
                    for (Graph graph : graphs) {
                        List<View> views = graph.getViews();
                        for (View view : views) {
                            entityManager.remove(view);
                        }
                        entityManager.remove(graph);
                    }
                }
                MRCConnector mrc = null;
                try {
                    mrc = new MRCConnector(node);
                    if (mrc.isSnapshotRunning() == 1) {
                        if (!mrc.stopSnapshotThread()) {
                            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "mconsole.infoMsg06", node));
                        }
                    }
                } finally {
                    if (null != mrc)
                        mrc.dispose();
                }
                entityManager.remove(node);
            }
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
    }

    
    
}
