/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.monitoring.console;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.RequestDispatcher;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.portlet.RenderRequest;
import javax.portlet.PortletException;

import org.apache.geronimo.monitoring.console.data.Graph;

/**
 * @version $Rev$ $Date$
 */
public class PopUpGraphServlet extends GenericServlet {
    @Resource
    UserTransaction userTransaction;

    @PersistenceContext
    EntityManager entityManager;

    private RequestDispatcher jspDispatcher;

    /**
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being placed into service.  See {@link javax.servlet.Servlet#init}.
     * <p/>
     * <p>This implementation stores the {@link javax.servlet.ServletConfig}
     * object it receives from the servlet container for later use.
     * When overriding this form of the method, call
     * <code>super.init(config)</code>.
     *
     * @param config the <code>ServletConfig</code> object
     *               that contains configutation
     *               information for this servlet
     * @throws javax.servlet.ServletException if an exception occurs that
     *                                        interrupts the servlet's normal
     *                                        operation
     * @see javax.servlet.UnavailableException
     */

    @Override
    public void init(ServletConfig config) throws ServletException {
        jspDispatcher = config.getServletContext().getRequestDispatcher("/monitoringPopUpGraph.jsp");
    }

    /**
     * Called by the servlet container to allow the servlet to respond to
     * a request.  See {@link javax.servlet.Servlet#service}.
     * <p/>
     * <p>This method is declared abstract so subclasses, such as
     * <code>HttpServlet</code>, must override it.
     *
     * @param req the <code>ServletRequest</code> object
     *            that contains the client's request
     * @param res the <code>ServletResponse</code> object
     *            that will contain the servlet's response
     * @throws javax.servlet.ServletException if an exception occurs that
     *                                        interferes with the servlet's
     *                                        normal operation occurred
     * @throws java.io.IOException            if an input or output
     *                                        exception occurs
     */

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        addGraphAttribute(req);
        jspDispatcher.forward(req, res);
    }

    private void addGraphAttribute(ServletRequest request) throws ServletException {
        try {
            userTransaction.begin();
            try {
                String graphIdString = request.getParameter("graph_id");
                int graphId = Integer.parseInt(graphIdString);
                Graph graph = (Graph) entityManager.createNamedQuery("graphById").setParameter("id", graphId).getSingleResult();
                StatsGraph statsGraph = new GraphsBuilder().getStatsGraph(graph);
                request.setAttribute("statsGraph", statsGraph);
            } finally {
                userTransaction.commit();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
