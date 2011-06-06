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

import java.util.ArrayList;
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

public class AdvancedWARWebServiceFinder extends AbstractWARWebServiceFinder {

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedWARWebServiceFinder.class);

    @Override
    public Map<String, PortInfo> discoverWebServices(WebModule module, Map<String, String> correctedPortLocations) throws DeploymentException {
        Map<String, PortInfo> servletNamePortInfoMap = new HashMap<String, PortInfo>();
        discoverPOJOWebServices(module, correctedPortLocations, servletNamePortInfoMap);
        return servletNamePortInfoMap;
    }

    private void discoverPOJOWebServices(WebModule module, Map<String, String> portLocations, Map<String, PortInfo> servletNamePortInfoMap) throws DeploymentException {

        Bundle bundle = module.getEarContext().getDeploymentBundle();
        WebApp webApp = module.getSpecDD();

        Set<String> ignoredEJBWebServiceClassNames = getEJBWebServiceClassNames(module);

        if (webApp.isMetadataComplete()) {
            // full web.xml, just examine all servlet entries for web services

            List<Servlet> servletTypes = webApp.getServlet();
            for (Servlet servletType : servletTypes) {
                String servletName = servletType.getServletName().trim();
                PortInfo portInfo = getPortInfo(servletType, bundle, portLocations);
                if (portInfo != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found POJO Web Service: {}", servletName);
                    }
                    servletNamePortInfoMap.put(servletName, portInfo);
                }
            }

        } else {
            // partial web.xml, discover all web service classes

            Map<String, List<String>> classServletMap = createClassServetMap(webApp);
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
                    LOG.debug("Discovered POJO Web Service class: {}", service.getName());
                }
                List<String> mappedServlets = classServletMap.get(service.getName());
                if (mappedServlets == null) {
                    // no <servlet/> entry, add one
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("POJO Web Service class {} is not mapped to any servlet", service.getName());
                    }
                    Servlet servlet = new Servlet();
                    servlet.setServletName(service.getName());
                    servlet.setServletClass(service.getName());
                    webApp.getServlet().add(servlet);

                    String location = portLocations.get(service.getName());
                    if (location == null) {
                        // add new <servlet-mapping/> element
                        location = "/" + JAXWSUtils.getServiceName(service);
                        ServletMapping servletMapping = new ServletMapping();
                        servletMapping.setServletName(service.getName());
                        servletMapping.getUrlPattern().add(location);
                        webApp.getServletMapping().add(servletMapping);
                    } else {
                        // weird, there was no servlet entry for this class but
                        // servlet-mapping exists
                        LOG.warn("Found <servlet-mapping> {} but corresponding <servlet> {}  was not defined", location, service.getName());
                    }

                    // map service
                    PortInfo portInfo = new PortInfo();
                    portInfo.setLocation(contextRoot + location);
                    portInfo.setHandlerChainsInfo(annotationHandlerChainFinder.buildHandlerChainFromClass(service));
                    portInfo.setWsdlService(JAXWSUtils.getServiceQName(service));
                    portInfo.setWsdlPort(JAXWSUtils.getPortQName(service));
                    servletNamePortInfoMap.put(service.getName(), portInfo);
                } else {
                    // found at least one mapped <servlet/> entry
                    for (String servlet : mappedServlets) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("POJO Web Service class {} is mapped to {} servlet", service.getName(), servlet);
                        }
                        PortInfo portInfo = createPortInfo(servlet, portLocations);
                        portInfo.setWsdlService(JAXWSUtils.getServiceQName(service));
                        portInfo.setWsdlPort(JAXWSUtils.getPortQName(service));
                        servletNamePortInfoMap.put(servlet, portInfo);
                    }
                }
            }

            // double check servlets in case we missed something
            List<Servlet> servletTypes = webApp.getServlet();
            for (Servlet servletType : servletTypes) {
                String servletName = servletType.getServletName().trim();
                if (servletNamePortInfoMap.get(servletName) == null) {
                    PortInfo portInfo = getPortInfo(servletType, bundle, portLocations);
                    if (portInfo != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found POJO Web Service: {}", servletName);
                        }
                        servletNamePortInfoMap.put(servletName, portInfo);
                    }
                }
            }
        }
    }

    private PortInfo getPortInfo(Servlet servletType, Bundle bundle, Map<String, String> portLocations) throws DeploymentException {
        PortInfo portInfo = null;
        if (servletType.getServletClass() != null) {
            String servletClassName = servletType.getServletClass().trim();
            try {
                Class<?> servletClass = bundle.loadClass(servletClassName);
                if (JAXWSUtils.isWebService(servletClass)) {
                    String servletName = servletType.getServletName().trim();
                    portInfo = createPortInfo(servletName, portLocations);
                    portInfo.setHandlerChainsInfo(annotationHandlerChainFinder.buildHandlerChainFromClass(servletClass));
                    portInfo.setWsdlService(JAXWSUtils.getServiceQName(servletClass));
                    portInfo.setWsdlPort(JAXWSUtils.getPortQName(servletClass));
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Failed to load servlet class " + servletClassName, e);
            }
        }
        return portInfo;
    }

    private PortInfo createPortInfo(String servlet, Map<String, String> portLocations) {
        PortInfo portInfo = new PortInfo();
        String location = portLocations.get(servlet);
        if (location != null) {
            portInfo.setLocation(location);
        }
        return portInfo;
    }

    /*
     * Create servlet-class to servlet-names mapping
     */
    private Map<String, List<String>> createClassServetMap(WebApp webApp) {
        Map<String, List<String>> classServletNameMap = new HashMap<String, List<String>>();
        List<Servlet> servletTypes = webApp.getServlet();
        if (servletTypes != null) {
            for (Servlet servletType : servletTypes) {
                String servletName = servletType.getServletName().trim();
                if (servletType.getServletClass() != null) {
                    String servletClassName = servletType.getServletClass().trim();
                    List<String> servlets = classServletNameMap.get(servletClassName);
                    if (servlets == null) {
                        servlets = new ArrayList<String>();
                        classServletNameMap.put(servletClassName, servlets);
                    }
                    servlets.add(servletName);
                }
            }
        }
        return classServletNameMap;
    }

}
