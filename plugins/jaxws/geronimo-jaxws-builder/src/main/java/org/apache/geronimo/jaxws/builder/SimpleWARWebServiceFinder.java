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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.xbeans.javaee.ServletMappingType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.WebAppType;

public class SimpleWARWebServiceFinder implements WebServiceFinder {

    private static final Log LOG = LogFactory.getLog(SimpleWARWebServiceFinder.class);
    
    public Map<String, PortInfo> discoverWebServices(Module module, 
                                                     boolean isEJB,
                                                     Map correctedPortLocations)
            throws DeploymentException {
        Map<String, PortInfo> map = new HashMap<String, PortInfo>();
        discoverPOJOWebServices(module, correctedPortLocations, map);
        return map;
    }

    private void discoverPOJOWebServices(Module module,
                                         Map correctedPortLocations,
                                         Map<String, PortInfo> map) 
        throws DeploymentException {
        ClassLoader classLoader = module.getEarContext().getClassLoader();
        WebAppType webApp = (WebAppType) module.getSpecDD();

        // find web services
        ServletType[] servletTypes = webApp.getServletArray();

        if (webApp.getDomNode().getChildNodes().getLength() == 0) {
            // web.xml not present (empty really), discover annotated
            // classes and update DD
            List<Class> services = WARWebServiceFinder.discoverWebServices(module.getModuleFile(), false, this.getClass().getClassLoader());
            String contextRoot = ((WebModule) module).getContextRoot();
            for (Class service : services) {
                // skip interfaces and such
                if (!JAXWSUtils.isWebService(service)) {
                    continue;
                }

                LOG.debug("Discovered POJO Web Service: " + service.getName());
                
                // add new <servlet/> element
                ServletType servlet = webApp.addNewServlet();
                servlet.addNewServletName().setStringValue(service.getName());
                servlet.addNewServletClass().setStringValue(service.getName());

                // add new <servlet-mapping/> element
                String location = "/" + JAXWSUtils.getServiceName(service);
                ServletMappingType servletMapping = webApp.addNewServletMapping();
                servletMapping.addNewServletName().setStringValue(service.getName());
                servletMapping.addNewUrlPattern().setStringValue(location);

                // map service
                PortInfo portInfo = new PortInfo();
                portInfo.setLocation(contextRoot + location);
                map.put(service.getName(), portInfo);
            }
        } else {
            // web.xml present, examine servlet classes and check for web
            // services
            for (ServletType servletType : servletTypes) {
                String servletName = servletType.getServletName().getStringValue().trim();
                if (servletType.isSetServletClass()) {
                    String servletClassName = servletType.getServletClass().getStringValue().trim();
                    try {
                        Class servletClass = classLoader.loadClass(servletClassName);
                        if (JAXWSUtils.isWebService(servletClass)) {
                            LOG.debug("Found POJO Web Service: " + servletName);
                            PortInfo portInfo = new PortInfo();
                            map.put(servletName, portInfo);
                        }
                    } catch (Exception e) {
                        throw new DeploymentException("Failed to load servlet class "
                                                      + servletClassName, e);
                    }
                }
            }

            // update web service locations
            for (Map.Entry entry : map.entrySet()) {
                String servletName = (String) entry.getKey();
                PortInfo portInfo = (PortInfo) entry.getValue();

                String location = (String) correctedPortLocations.get(servletName);
                if (location != null) {
                    portInfo.setLocation(location);
                }
            }
        }
    } 

}
