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
package org.apache.geronimo.jetty6;

import java.io.Serializable;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.jetty.servlet.FilterMapping;
import org.mortbay.jetty.Handler;

/**
 * @version $Rev$ $Date$
 */
public class JettyFilterMapping extends FilterMapping {

    private final String[] urlPatterns;
    private final boolean requestDispatch;
    private final boolean forwardDispatch;
    private final boolean includeDispatch;
    private final boolean errorDispatch;
    private final JettyFilterHolder jettyFilterHolder;
    private final Collection jettyServletHolders;
    private final JettyFilterMapping previous;
    private final JettyServletRegistration jettyServletRegistration;

    //todo use an interface for endpoints.
    public JettyFilterMapping() {
        this.urlPatterns = null;
        this.requestDispatch = false;
        this.forwardDispatch = false;
        this.includeDispatch = false;
        this.errorDispatch = false;
        this.jettyFilterHolder = null;
        this.jettyServletHolders = null;
        this.previous = null;
        this.jettyServletRegistration = null;
    }

    public JettyFilterMapping(String[] urlPatterns,
                              boolean requestDispatch,
                              boolean forwardDispatch,
                              boolean includeDispatch,
                              boolean errorDispatch,
                              JettyFilterHolder jettyFilterHolder,
                              Collection jettyServletHolders,
                              JettyFilterMapping previous,
                              JettyServletRegistration jettyServletRegistration) {
       this.urlPatterns = urlPatterns;
        this.requestDispatch = requestDispatch;
        this.forwardDispatch = forwardDispatch;
        this.includeDispatch = includeDispatch;
        this.errorDispatch = errorDispatch;
        this.jettyFilterHolder = jettyFilterHolder;
        this.jettyServletHolders = jettyServletHolders;
        this.previous = previous;
        this.jettyServletRegistration = jettyServletRegistration;

        if (jettyServletRegistration != null) {
            assert jettyServletHolders != null ^ urlPatterns != null;

            String filterName = jettyFilterHolder.getFilterName();
            int dispatches = 0;
            if (requestDispatch) {
                dispatches |=  Handler.REQUEST;
            }
            if (forwardDispatch) {
                dispatches |= Handler.FORWARD;
            }
            if (includeDispatch) {
                dispatches |= Handler.INCLUDE;
            }
            if (errorDispatch) {
                dispatches |= Handler.ERROR;
            }


            setFilterName(filterName);
            setDispatches(dispatches);
            setPathSpecs(urlPatterns);
            if (jettyServletHolders != null) {
                String[] servletNames = new String[jettyServletHolders.size()];
                int i = 0;
                for (Iterator iterator = jettyServletHolders.iterator(); iterator.hasNext();) {
                    JettyServletHolder jettyServletHolder = (JettyServletHolder) iterator.next();
                    servletNames[i++] = jettyServletHolder.getServletName();
                }
                setServletNames(servletNames);
            }

            (jettyServletRegistration.getServletHandler()).addFilterMapping(this);
        }
    }

    public String[] getUrlPatterns() {
        return urlPatterns;
    }

    public boolean isRequestDispatch() {
        return requestDispatch;
    }

    public boolean isForwardDispatch() {
        return forwardDispatch;
    }

    public boolean isIncludeDispatch() {
        return includeDispatch;
    }

    public boolean isErrorDispatch() {
        return errorDispatch;
    }

    public JettyFilterHolder getFilter() {
        return jettyFilterHolder;
    }

    public Collection getServlets() {
        return jettyServletHolders;
    }

    public JettyFilterMapping getPrevious() {
        return previous;
    }

    public JettyServletRegistration getJettyServletRegistration() {
        return jettyServletRegistration;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyFilterMapping.class, NameFactory.URL_WEB_FILTER_MAPPING);
        infoBuilder.addAttribute("urlPatterns", String[].class, true);
        infoBuilder.addAttribute("requestDispatch", boolean.class, true);
        infoBuilder.addAttribute("forwardDispatch", boolean.class, true);
        infoBuilder.addAttribute("includeDispatch", boolean.class, true);
        infoBuilder.addAttribute("errorDispatch", boolean.class, true);

        infoBuilder.addReference("Filter", JettyFilterHolder.class, NameFactory.WEB_FILTER);
        infoBuilder.addReference("Servlets", JettyServletHolder.class, NameFactory.SERVLET);
        infoBuilder.addReference("Previous", JettyFilterMapping.class, NameFactory.URL_WEB_FILTER_MAPPING);
        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[]{"urlPatterns",
                                                "requestDispatch",
                                                "forwardDispatch",
                                                "includeDispatch",
                                                "errorDispatch",
                                                "Filter",
                                                "Servlets",
                                                "Previous",
                                                "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
