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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class ServletInitParamMergeHandler implements SubMergeHandler<Servlet, Servlet> {

    @Override
    public void add(Servlet servlet, MergeContext mergeContext) throws DeploymentException {
        String servletName = servlet.getServletName();
        for (ParamValue paramValue : servlet.getInitParam()) {
            addServletInitParam(servletName, paramValue, ElementSource.WEB_FRAGMENT, mergeContext.getCurrentJarUrl(), mergeContext);
        }
    }

    @Override
    public void merge(Servlet srcServlet, Servlet targetServlet, MergeContext mergeContext) throws DeploymentException {
        String servletName = srcServlet.getServletName();
        for (ParamValue paramValue : srcServlet.getInitParam()) {
            MergeItem existedMergeItem = (MergeItem) mergeContext.getAttribute(createServletInitParamKey(servletName, paramValue.getParamName()));
            if (existedMergeItem == null) {
                targetServlet.getInitParam().add(paramValue);
                addServletInitParam(servletName, paramValue, ElementSource.WEB_FRAGMENT, mergeContext.getCurrentJarUrl(), mergeContext);
            } else {
                ParamValue existedParamValue = (ParamValue) existedMergeItem.getValue();
                switch (existedMergeItem.getSourceType()) {
                case WEB_XML:
                    continue;
                case WEB_FRAGMENT:
                    if (existedParamValue.getParamValue().equals(paramValue.getParamValue())
                            || existedMergeItem.getBelongedURL().equals(mergeContext.getCurrentJarUrl())) {
                        break;
                    } else {
                        throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("servlet " + servletName, "param-name", paramValue.getParamName(),
                                "param-value", existedParamValue.getParamValue(), existedMergeItem.getBelongedURL(), paramValue.getParamValue(), mergeContext
                                        .getCurrentJarUrl()));
                    }
                case ANNOTATION:
                    //Spec 8.1.n.iii Init params for servlets and filters defined via annotations, will be
                    //overridden in the descriptor if the name of the init param exactly matches
                    //the name specified via the annotation. Init params are additive between the
                    //annotations and descriptors.
                    //In my understanding, the value of init-param should be overridden even if it is merged from annotation before the current web-fragment.xml file
                    existedParamValue.setParamValue(paramValue.getParamValue());
                    existedMergeItem.setBelongedURL(mergeContext.getCurrentJarUrl());
                    existedMergeItem.setSourceType(ElementSource.WEB_FRAGMENT);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (Servlet servlet : webApp.getServlet()) {
            String servletName = servlet.getServletName();
            for (ParamValue paramValue : servlet.getInitParam()) {
                addServletInitParam(servletName, paramValue, ElementSource.WEB_XML, null, context);
            }
        }
    }

    public static String createServletInitParamKey(String servletName, String paramName) {
        return "servlet." + servletName + ".init-param.param-name." + paramName;
    }

    public static boolean isServletInitParamConfigured(String servletName, String paramName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletInitParamKey(servletName, paramName));
    }

    public static void addServletInitParam(String servletName, ParamValue paramValue, ElementSource source, String relativeUrl, MergeContext mergeContext) {
        mergeContext.setAttribute(createServletInitParamKey(servletName, paramValue.getParamName()), new MergeItem(paramValue, relativeUrl, source));
    }
}
