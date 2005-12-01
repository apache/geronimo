/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty;

import java.io.Serializable;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * @version $Rev$ $Date$
 */
public class JettyFilterMapping implements Serializable {

    private final String urlPattern;
    private final boolean requestDispatch;
    private final boolean forwardDispatch;
    private final boolean includeDispatch;
    private final boolean errorDispatch;
    private final JettyFilterHolder jettyFilterHolder;
    private final JettyServletHolder jettyServletHolder;
    private final JettyFilterMapping previous;
    private final JettyServletRegistration jettyServletRegistration;

    //todo use an interface for endpoints.
    public JettyFilterMapping() {
        this.urlPattern = null;
        this.requestDispatch = false;
        this.forwardDispatch = false;
        this.includeDispatch = false;
        this.errorDispatch = false;
        this.jettyFilterHolder = null;
        this.jettyServletHolder = null;
        this.previous = null;
        this.jettyServletRegistration = null;
    }

    public JettyFilterMapping(String urlPattern,
                              boolean requestDispatch,
                              boolean forwardDispatch,
                              boolean includeDispatch,
                              boolean errorDispatch,
                              JettyFilterHolder jettyFilterHolder,
                              JettyServletHolder jettyServletHolder,
                              JettyFilterMapping previous,
                              JettyServletRegistration jettyServletRegistration) {
       this.urlPattern = urlPattern;
        this.requestDispatch = requestDispatch;
        this.forwardDispatch = forwardDispatch;
        this.includeDispatch = includeDispatch;
        this.errorDispatch = errorDispatch;
        this.jettyFilterHolder = jettyFilterHolder;
        this.jettyServletHolder = jettyServletHolder;
        this.previous = previous;
        this.jettyServletRegistration = jettyServletRegistration;

        if (jettyServletRegistration != null) {
            assert jettyServletHolder != null ^ urlPattern != null;

            String filterName = jettyFilterHolder.getFilterName();
            int dispatches = 0;
            if (requestDispatch) {
                dispatches |= Dispatcher.__REQUEST;
            }
            if (forwardDispatch) {
                dispatches |= Dispatcher.__FORWARD;
            }
            if (includeDispatch) {
                dispatches |= Dispatcher.__INCLUDE;
            }
            if (errorDispatch) {
                dispatches |= Dispatcher.__ERROR;
            }


            if (jettyServletHolder == null) {
                ((WebApplicationHandler)jettyServletRegistration.getServletHandler()).addFilterPathMapping(urlPattern, filterName, dispatches);
            } else {
                String servletName = jettyServletHolder.getServletName();
                ((WebApplicationHandler)jettyServletRegistration.getServletHandler()).addFilterServletMapping(servletName, filterName, dispatches);
            }
        }
    }

    public String getUrlPattern() {
        return urlPattern;
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

    public JettyServletHolder getServlet() {
        return jettyServletHolder;
    }

    public JettyFilterMapping getPrevious() {
        return previous;
    }

    public JettyServletRegistration getJettyServletRegistration() {
        return jettyServletRegistration;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyFilterMapping.class, NameFactory.WEB_FILTER_MAPPING);
        infoBuilder.addAttribute("urlPattern", String.class, true);
        infoBuilder.addAttribute("requestDispatch", boolean.class, true);
        infoBuilder.addAttribute("forwardDispatch", boolean.class, true);
        infoBuilder.addAttribute("includeDispatch", boolean.class, true);
        infoBuilder.addAttribute("errorDispatch", boolean.class, true);

        infoBuilder.addReference("Filter", JettyFilterHolder.class, NameFactory.WEB_FILTER);
        infoBuilder.addReference("Servlet", JettyServletHolder.class, NameFactory.SERVLET);
        infoBuilder.addReference("Previous", JettyFilterMapping.class, NameFactory.WEB_FILTER_MAPPING);
        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[]{"urlPattern",
                                                "requestDispatch",
                                                "forwardDispatch",
                                                "includeDispatch",
                                                "errorDispatch",
                                                "Filter",
                                                "Servlet",
                                                "Previous",
                                                "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
