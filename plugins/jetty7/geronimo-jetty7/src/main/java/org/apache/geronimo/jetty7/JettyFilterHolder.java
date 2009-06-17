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

import java.util.Map;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty7.handler.IntegrationContext;
import org.eclipse.jetty.servlet.FilterHolder;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.WEB_FILTER)
public class JettyFilterHolder implements GBeanLifecycle {

    private final FilterHolder filterHolder;

    public JettyFilterHolder(@ParamAttribute(name = "filterName") String filterName,
                             @ParamAttribute(name = "filterClass") String filterClass,
                             @ParamAttribute(name = "initParams") Map initParams,
                             @ParamReference(name = "JettyServletRegistration", namingType = NameFactory.WEB_MODULE) JettyServletRegistration jettyServletRegistration) throws Exception {
        filterHolder = new InternalFilterHolder(jettyServletRegistration);
        if (jettyServletRegistration != null) {
            filterHolder.setName(filterName);
            filterHolder.setClassName(filterClass);
            filterHolder.setInitParameters(initParams);
            (jettyServletRegistration.getServletHandler()).addFilter(filterHolder);
        }
    }

    public String getFilterName() {
        return filterHolder.getName();
    }

    public void doStart() throws Exception {
        filterHolder.start();
    }

    public void doStop() throws Exception {
        filterHolder.stop();
    }

    public void doFail() {
        try {
            filterHolder.stop();
        } catch (Exception e) {
            //ignore?
        }
    }

    private static class InternalFilterHolder extends FilterHolder {
        private final JettyServletRegistration servletRegistration;
        private boolean destroyed;

        public InternalFilterHolder(JettyServletRegistration servletRegistration) {
            this.servletRegistration = servletRegistration;
        }


        public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
            return servletRegistration.newInstance(_className);
        }

        public void destroyInstance(Object o) throws Exception {
            if (!destroyed) {
                super.destroyInstance(o);
                servletRegistration.destroyInstance(o);
                destroyed = true;
            }
        }

        @Override
        public void doStart() throws Exception {
            IntegrationContext integrationContext = servletRegistration.getIntegrationContext();
            javax.naming.Context context = integrationContext.setContext();
            boolean txActive = integrationContext.isTxActive();
            SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
            ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
            try {
                try {
                    super.doStart();
                } finally {
                    integrationContext.restoreConnectorContext(connectorContext, null, newContext);
                }
            } finally {
                integrationContext.restoreContext(context);
                integrationContext.completeTx(txActive, null);
            }
        }

        @Override
        public void doStop() throws Exception {
            IntegrationContext integrationContext = servletRegistration.getIntegrationContext();
            javax.naming.Context context = integrationContext.setContext();
            boolean txActive = integrationContext.isTxActive();
            SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
            ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
            try {
                try {
                    super.doStop();
                } finally {
                    integrationContext.restoreConnectorContext(connectorContext, null, newContext);
                }
            } finally {
                integrationContext.restoreContext(context);
                integrationContext.completeTx(txActive, null);
            }
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyFilterHolder.class, NameFactory.WEB_FILTER);
        infoBuilder.addAttribute("filterName", String.class, true);
        infoBuilder.addAttribute("filterClass", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);

        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[]{"filterName", "filterClass", "initParams", "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
