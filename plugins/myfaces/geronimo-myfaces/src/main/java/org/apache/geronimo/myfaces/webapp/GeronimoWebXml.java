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

package org.apache.geronimo.myfaces.webapp;

import java.util.ArrayList;
import java.util.List;

import javax.faces.webapp.FacesServlet;

import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.FilterMappingInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.myfaces.shared_impl.webapp.webxml.DelegatedFacesServlet;
import org.apache.myfaces.shared_impl.webapp.webxml.FilterMapping;
import org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebXml extends WebXml {

    private List<ServletMapping> facesServletMappings;

    private List<FilterMapping> facesExtensionsFilterMapppings;

    private boolean errorPagePresent;

    //TODO remove once upgrade to MyFaces 2.0.3
    private String delegateFacesServlet;

    public GeronimoWebXml(Bundle bundle, WebAppInfo webAppInfo, String delegateFacesServlet) {
        errorPagePresent = webAppInfo.errorPages != null && webAppInfo.errorPages.size() > 0;
        this.delegateFacesServlet = delegateFacesServlet;
        facesServletMappings = new ArrayList<ServletMapping>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (ServletInfo servletInfo : webAppInfo.servlets) {
            if (servletInfo.servletClass == null) {
                continue;
            }
            Class<?> servletClass;
            try {
                servletClass = contextClassLoader.loadClass(servletInfo.servletClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Could not load the servlet class " + servletInfo.servletClass, e);
            }
            if (isFacesServlet(servletClass)) {
                for (String urlPattern : servletInfo.servletMappings)
                    facesServletMappings.add(new ServletMapping(servletInfo.servletName, servletClass, urlPattern));
            }
        }
        facesExtensionsFilterMapppings = new ArrayList<FilterMapping>();
        for (FilterInfo filterInfo : webAppInfo.filters) {
            Class<?> filterClass;
            try {
                filterClass = contextClassLoader.loadClass(filterInfo.filterClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Could not load the filter class " + filterInfo.filterClass, e);
            }
            if (!isFacesExtensionsFilter(filterClass)) {
                continue;
            }
            for (FilterMappingInfo filterMappingInfo : filterInfo.urlMappings) {
                for (String urlPattern : filterMappingInfo.mapping) {
                    facesExtensionsFilterMapppings.add(new FilterMapping(filterInfo.filterName, filterClass, urlPattern));
                }
            }
        }
    }

    @Override
    public List<ServletMapping> getFacesServletMappings() {
        return facesServletMappings;
    }

    @Override
    public List<FilterMapping> getFacesExtensionsFilterMappings() {
        return facesExtensionsFilterMapppings;
    }

    @Override
    public boolean isErrorPagePresent() {
        return errorPagePresent;
    }

    //TODO remove once upgrade to MyFaces 2.0.3
    protected boolean isFacesServlet(Class<?> servletClass) {
        return FacesServlet.class.isAssignableFrom(servletClass) || DelegatedFacesServlet.class.isAssignableFrom(servletClass) || servletClass.getName().equals(delegateFacesServlet);
    }

    protected boolean isFacesExtensionsFilter(Class<?> filterClass) {
        return "org.apache.myfaces.component.html.util.ExtensionsFilter".equals(filterClass.getName()) || "org.apache.myfaces.webapp.filter.ExtensionsFilter".equals(filterClass.getName());
    }
}
