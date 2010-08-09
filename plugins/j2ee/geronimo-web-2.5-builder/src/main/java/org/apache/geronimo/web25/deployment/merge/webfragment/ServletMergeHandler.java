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

package org.apache.geronimo.web25.deployment.merge.webfragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class ServletMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    private List<SubMergeHandler<Servlet, Servlet>> subMergeHandlers;

    public ServletMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<Servlet, Servlet>>(2);
        subMergeHandlers.add(new ServletInitParamMergeHandler());
        subMergeHandlers.add(new ServletLoadOnStartupMergeHandler());
    }

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Servlet srcServlet : webFragment.getServlet()) {
            String servletName = srcServlet.getServletName();
            Servlet targetServlet = (Servlet) mergeContext.getAttribute(createServletKey(servletName));
            if (targetServlet == null) {
                webApp.getServlet().add(srcServlet);
                mergeContext.setAttribute(createServletKey(servletName), srcServlet);
                for (SubMergeHandler<Servlet, Servlet> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(srcServlet, mergeContext);
                }
            } else {
                for (SubMergeHandler<Servlet, Servlet> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcServlet, targetServlet, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (SubMergeHandler<Servlet, Servlet> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, mergeContext);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Servlet servlet : webApp.getServlet()) {
            mergeContext.setAttribute(createServletKey(servlet.getServletName()), servlet);
        }
        for (SubMergeHandler<Servlet, Servlet> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, mergeContext);
        }
    }

    public static String createServletKey(String servletName) {
        return "servlet.servlet-name." + servletName;
    }

    public static boolean isServletConfigured(String servletName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletKey(servletName));
    }

    public static Servlet getServlet(String servletName, MergeContext mergeContext) {
        return (Servlet) mergeContext.getAttribute(createServletKey(servletName));
    }

    public static void addServlet(Servlet servlet, MergeContext mergeContext) {
        mergeContext.setAttribute(createServletKey(servlet.getServletName()), servlet);
    }
}
