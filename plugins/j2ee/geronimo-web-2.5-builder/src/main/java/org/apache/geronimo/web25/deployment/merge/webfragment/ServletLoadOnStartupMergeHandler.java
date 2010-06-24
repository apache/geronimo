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

import java.math.BigInteger;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class ServletLoadOnStartupMergeHandler implements SubMergeHandler<Servlet, Servlet> {

    /**
     *  This method will be invoked while a new servlet is found in the current webfragment.xml, while it is not found in the web.xml file and those merged web-fragment.xml files
     */
    @Override
    public void add(Servlet servlet, MergeContext mergeContext) throws DeploymentException {
        if (servlet.getLoadOnStartup() != null) {
            mergeContext.setAttribute(createServletLoadOnStartupKey(servlet.getServletName()), new MergeItem(servlet.getLoadOnStartup(), mergeContext.getCurrentJarUrl(),
                    ElementSource.WEB_FRAGMENT));
        }
    }

    @Override
    public void merge(Servlet srcServlet, Servlet targetServlet, MergeContext mergeContext) throws DeploymentException {
        String servletName = srcServlet.getServletName();
        //If the same servlet in the initial web.xml has already set the load-on-startup option, then we just ignore the setting in webfragment.xml file
        if (isServletLoadOnStartupConfiguredInWebXML(servletName, mergeContext)) {
            return;
        }
        if (srcServlet.getLoadOnStartup() != null) {
            Integer srcLoadOnStartupValue = srcServlet.getLoadOnStartup();
            MergeItem existedLoadOnStartup = (MergeItem) mergeContext.getAttribute(createServletLoadOnStartupKey(servletName));
            if (existedLoadOnStartup == null) {
                targetServlet.setLoadOnStartup(srcLoadOnStartupValue);
                mergeContext.setAttribute(createServletLoadOnStartupKey(servletName), new MergeItem(srcLoadOnStartupValue, mergeContext.getCurrentJarUrl(), ElementSource.WEB_XML));
            } else if (!existedLoadOnStartup.getValue().equals(srcLoadOnStartupValue)) {
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateValueMessage("servlet " + servletName, "load-on-startup", existedLoadOnStartup.getValue().toString(),
                        existedLoadOnStartup.getBelongedURL(), srcLoadOnStartupValue.toString(), mergeContext.getCurrentJarUrl()));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (Servlet servlet : webApp.getServlet()) {
            if (servlet.getLoadOnStartup() != null) {
                context.setAttribute(createServletLoadOnStartupConfiguredInWebXMLKey(servlet.getServletName()), Boolean.TRUE);
            }
        }
    }

    public static String createServletLoadOnStartupConfiguredInWebXMLKey(String servletName) {
        return "servlet.servlet-name." + servletName + ".load-on-startup.configured_in_web_xml";
    }

    public static String createServletLoadOnStartupKey(String servletName) {
        return "servlet.servlet-name." + servletName + ".load-on-startup";
    }

    public static boolean isServletLoadOnStartupConfiguredInWebXML(String servletName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletLoadOnStartupConfiguredInWebXMLKey(servletName));
    }
}
