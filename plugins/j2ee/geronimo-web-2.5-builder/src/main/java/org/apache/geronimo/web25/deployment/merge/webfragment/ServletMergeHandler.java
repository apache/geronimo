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
import org.apache.geronimo.xbeans.javaee6.ServletType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class ServletMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    private List<SubMergeHandler<ServletType, ServletType>> subMergeHandlers;

    public ServletMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<ServletType, ServletType>>();
        subMergeHandlers.add(new ServletInitParamMergeHandler());
        subMergeHandlers.add(new ServletLoadOnStartupMergeHandler());
    }

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServletType srcServlet : webFragment.getServletArray()) {
            String servletName = srcServlet.getServletName().getStringValue();
            ServletType targetServlet = (ServletType) mergeContext.getAttribute(createServletKey(servletName));
            if (targetServlet == null) {
                targetServlet = (ServletType) webApp.addNewServlet().set(srcServlet);
                mergeContext.setAttribute(createServletKey(servletName), targetServlet);
                for (SubMergeHandler<ServletType, ServletType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(targetServlet, mergeContext);
                }
            } else {
                for (SubMergeHandler<ServletType, ServletType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcServlet, targetServlet, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (SubMergeHandler<ServletType, ServletType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, mergeContext);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServletType servlet : webApp.getServletArray()) {
            mergeContext.setAttribute(createServletKey(servlet.getServletName().getStringValue()), servlet);
        }
        for (SubMergeHandler<ServletType, ServletType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, mergeContext);
        }
    }

    public static String createServletKey(String servletName) {
        return "servlet.servlet-name." + servletName;
    }

    public static boolean isServletConfigured(String servletName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletKey(servletName));
    }

    public static ServletType getServlet(String servletName, MergeContext mergeContext) {
        return (ServletType) mergeContext.getAttribute(createServletKey(servletName));
    }

    public static void addServlet(ServletType servlet, MergeContext mergeContext) {
        mergeContext.setAttribute(createServletKey(servlet.getServletName().getStringValue()), servlet);
    }
}
