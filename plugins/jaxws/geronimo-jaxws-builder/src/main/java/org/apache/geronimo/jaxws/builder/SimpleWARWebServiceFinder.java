/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jaxws.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWARWebServiceFinder extends AbstractWARWebServiceFinder {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleWARWebServiceFinder.class);

    @Override
    public Map<String, PortInfo> discoverWebServices(WebModule module, Map<String, String> correctedPortLocations) throws DeploymentException {

        Map<String, PortInfo> servletNamePortInfoMap = new HashMap<String, PortInfo>();

        Bundle bundle = module.getEarContext().getDeploymentBundle();
        WebApp webApp = module.getSpecDD();

        // find web services
        List<Servlet> servletTypes = webApp.getServlet();
        Set<String> ignoredEJBWebServiceClassNames = getEJBWebServiceClassNames(module);

        if (webApp.getServlet().size() == 0) {
            // web.xml not present (empty really), discover annotated
            // classes and update DD
            List<Class<?>> services = discoverWebServices(module);
            String contextRoot = (module).getContextRoot();
            for (Class<?> service : services) {
                // skip interfaces and such
                if (!JAXWSUtils.isWebService(service)) {
                    continue;
                }

                if (ignoredEJBWebServiceClassNames.contains(service.getName())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Web service " + service.getClass().getName() + "  is ignored as it is also an EJB, it will exposed as an EJB Web Service ");
                    }
                    continue;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Discovered POJO Web Service: " + service.getName());
                }
                // add new <servlet/> element
                Servlet servlet = new Servlet();
                servlet.setServletName(service.getName());
                servlet.setServletClass(service.getName());
                webApp.getServlet().add(servlet);
                // add new <servlet-mapping/> element
                String location = "/" + JAXWSUtils.getServiceName(service);
                ServletMapping servletMapping = new ServletMapping();
                servletMapping.setServletName(service.getName());
                servletMapping.getUrlPattern().add(location);

                // map service
                PortInfo portInfo = new PortInfo();
                portInfo.setLocation(contextRoot + location);
                servletNamePortInfoMap.put(service.getName(), portInfo);
            }
        } else {
            // web.xml present, examine servlet classes and check for web
            // services
            for (Servlet servletType : servletTypes) {
                String servletName = servletType.getServletName().trim();
                if (servletType.getServletClass() != null) {
                    String servletClassName = servletType.getServletClass().trim();
                    try {
                        Class<?> servletClass = bundle.loadClass(servletClassName);
                        if (JAXWSUtils.isWebService(servletClass)) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Found POJO Web Service: " + servletName);
                            }
                            PortInfo portInfo = new PortInfo();
                            servletNamePortInfoMap.put(servletName, portInfo);
                        }
                    } catch (Exception e) {
                        throw new DeploymentException("Failed to load servlet class " + servletClassName + " from bundle " + bundle, e);
                    }
                }
            }

            // update web service locations
            for (Map.Entry<String, PortInfo> entry : servletNamePortInfoMap.entrySet()) {
                String servletName = entry.getKey();
                PortInfo portInfo = entry.getValue();

                String location = correctedPortLocations.get(servletName);
                if (location != null) {
                    portInfo.setLocation(location);
                }
            }
        }

        return servletNamePortInfoMap;
    }
}
