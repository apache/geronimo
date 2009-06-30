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
package org.apache.geronimo.jetty7;

import java.util.Collection;
import java.util.EnumSet;

import org.eclipse.jetty.server.DispatcherType;

import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.URL_WEB_FILTER_MAPPING)
public class JettyFilterMapping extends FilterMapping {

    private final String[] urlPatterns;
    private final boolean requestDispatch;
    private final boolean forwardDispatch;
    private final boolean includeDispatch;
    private final boolean errorDispatch;
    private final FilterHolderWrapper jettyFilterHolder;
    private final Collection<ServletNameSource> jettyServletHolders;
    private final JettyFilterMapping previous;
    private final JettyServletRegistration jettyServletRegistration;

    public JettyFilterMapping(@ParamAttribute(name = "urlPatterns")String[] urlPatterns,
            @ParamAttribute(name = "requestDispatch")boolean requestDispatch,
            @ParamAttribute(name = "forwardDispatch")boolean forwardDispatch,
            @ParamAttribute(name = "includeDispatch")boolean includeDispatch,
            @ParamAttribute(name = "errorDispatch")boolean errorDispatch,
            @ParamReference(name="Filter", namingType = NameFactory.WEB_FILTER) FilterHolderWrapper jettyFilterHolder,
            @ParamReference(name="Servlets", namingType = NameFactory.SERVLET)Collection<ServletNameSource> jettyServletHolders,
            @ParamReference(name="Previous", namingType = NameFactory.URL_WEB_FILTER_MAPPING)JettyFilterMapping previous,
            @ParamReference(name="JettyServletRegistration", namingType = NameFactory.WEB_MODULE)JettyServletRegistration jettyServletRegistration) {
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
            EnumSet<DispatcherType> dispatches = EnumSet.noneOf(DispatcherType.class);
            if (requestDispatch) {
                dispatches.add(DispatcherType.REQUEST);
            }
            if (forwardDispatch) {
                dispatches.add(DispatcherType.FORWARD);
            }
            if (includeDispatch) {
                dispatches.add(DispatcherType.INCLUDE);
            }
            if (errorDispatch) {
                dispatches.add(DispatcherType.ERROR);
            }

            setFilterName(filterName);
            setDispatcherTypes(dispatches);
            setPathSpecs(urlPatterns);
            if (jettyServletHolders != null) {
                resetServlets();
                if (jettyServletHolders instanceof ReferenceCollection) {
                    ((ReferenceCollection) jettyServletHolders).addReferenceCollectionListener(new ReferenceCollectionListener() {

                        public void memberAdded(ReferenceCollectionEvent event) {
                            resetServlets();
                            resetJettyFilterMappings();
                        }

                        public void memberRemoved(ReferenceCollectionEvent event) {
                            resetServlets();
                            resetJettyFilterMappings();
                        }
                    });
                }
            }

            jettyServletRegistration.getServletHandler().addFilterMapping(this);
        }
    }

    private void resetJettyFilterMappings() {
        //This causes jetty to recompute the filter to servlet mappings based on the
        //current servlet names in the filter mappings.  Pretty inefficient.
        ServletHandler servletHandler = jettyServletRegistration.getServletHandler();
        FilterMapping[] filterMappings = servletHandler.getFilterMappings();
        FilterMapping[] copy = filterMappings.clone();
        servletHandler.setFilterMappings(copy);
    }

    private void resetServlets() {
        String[] servletNames = new String[jettyServletHolders.size()];
        int i = 0;
        for (ServletNameSource jettyServletHolder : jettyServletHolders) {
            servletNames[i++] = jettyServletHolder.getServletName();
        }
        setServletNames(servletNames);
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

    public FilterHolderWrapper getFilter() {
        return jettyFilterHolder;
    }

    public Collection<ServletNameSource> getServlets() {
        return jettyServletHolders;
    }

    public JettyFilterMapping getPrevious() {
        return previous;
    }

    public JettyServletRegistration getJettyServletRegistration() {
        return jettyServletRegistration;
    }

}
