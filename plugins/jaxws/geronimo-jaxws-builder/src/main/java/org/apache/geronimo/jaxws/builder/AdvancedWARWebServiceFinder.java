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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.xbeans.javaee.ServletMappingType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedWARWebServiceFinder implements WebServiceFinder {

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedWARWebServiceFinder.class);
    
    public Map<String, PortInfo> discoverWebServices(Module module, 
                                                     boolean isEJB,
                                                     Map correctedPortLocations)
            throws DeploymentException {
        Map<String, PortInfo> map = new HashMap<String, PortInfo>();
        discoverPOJOWebServices(module, correctedPortLocations, map);
        return map;
    }

    private void discoverPOJOWebServices(Module module,
                                         Map portLocations,
                                         Map<String, PortInfo> map) 
        throws DeploymentException {
        ClassLoader classLoader = module.getEarContext().getClassLoader();
        WebAppType webApp = (WebAppType) module.getSpecDD();

        if (webApp.isSetMetadataComplete()) {
            // full web.xml, just examine all servlet entries for web services
            
            ServletType[] servletTypes = webApp.getServletArray();
            for (ServletType servletType : servletTypes) {
                String servletName = servletType.getServletName().getStringValue().trim();
                PortInfo portInfo = getPortInfo(servletType, classLoader, portLocations);
                if (portInfo != null) {
                    LOG.debug("Found POJO Web Service: {}", servletName);
                    map.put(servletName, portInfo);
                }
            }
            
        } else {
            // partial web.xml, discover all web service classes 
            
            Map<String, List<String>> classServletMap = createClassServetMap(webApp);
            List<Class> services = WARWebServiceFinder.discoverWebServices(module.getModuleFile(), false, classLoader);
            String contextRoot = ((WebModule) module).getContextRoot();
            for (Class service : services) {
                // skip interfaces and such
                if (!JAXWSUtils.isWebService(service)) {
                    continue;
                }

                LOG.debug("Discovered POJO Web Service class: {}", service.getName());
                
                List<String> mappedServlets = classServletMap.get(service.getName());
                if (mappedServlets == null) {
                    // no <servlet/> entry, add one
                    
                    LOG.debug("POJO Web Service class {} is not mapped to any servlet", service.getName());
                    
                    ServletType servlet = webApp.addNewServlet();
                    servlet.addNewServletName().setStringValue(service.getName());
                    servlet.addNewServletClass().setStringValue(service.getName());
                    
                    String location = (String)portLocations.get(service.getName());
                    if (location == null) {
                        // add new <servlet-mapping/> element
                        location = "/" + JAXWSUtils.getServiceName(service);
                        ServletMappingType servletMapping = webApp.addNewServletMapping();
                        servletMapping.addNewServletName().setStringValue(service.getName());
                        servletMapping.addNewUrlPattern().setStringValue(location);
                    } else {
                        // weird, there was no servlet entry for this class but 
                        // servlet-mapping exists
                        LOG.warn("Found <servlet-mapping> but corresponding <servlet> was not defined");
                    }

                    // map service
                    PortInfo portInfo = new PortInfo();
                    portInfo.setLocation(contextRoot + location);
                    map.put(service.getName(), portInfo);
                } else {
                    // found at least one mapped <servlet/> entry
                    for (String servlet : mappedServlets) {
                        LOG.debug("POJO Web Service class {} is mapped to {} servlet", service.getName(), servlet);
                        PortInfo portInfo = createPortInfo(servlet, portLocations);
                        map.put(servlet, portInfo);
                    }
                }                
            }
            
            // double check servlets in case we missed something
            ServletType[] servletTypes = webApp.getServletArray();
            for (ServletType servletType : servletTypes) {
                String servletName = servletType.getServletName().getStringValue().trim();
                if (map.get(servletName) == null) {
                    PortInfo portInfo = getPortInfo(servletType, classLoader, portLocations);
                    if (portInfo != null) {
                        LOG.debug("Found POJO Web Service: {}", servletName);
                        map.put(servletName, portInfo);
                    }
                }
            }
        }               
    } 
        
    private PortInfo getPortInfo(ServletType servletType, 
                                 ClassLoader classLoader, 
                                 Map portLocations) throws DeploymentException {
        PortInfo portInfo = null;
        if (servletType.isSetServletClass()) {
            String servletClassName = servletType.getServletClass().getStringValue().trim();
            try {
                Class servletClass = classLoader.loadClass(servletClassName);
                if (JAXWSUtils.isWebService(servletClass)) {      
                    String servletName = servletType.getServletName().getStringValue().trim();
                    portInfo = createPortInfo(servletName, portLocations);
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Failed to load servlet class "
                                              + servletClassName, e);
            }
        }
        return portInfo;
    }
    
    private PortInfo createPortInfo(String servlet, Map portLocations) { 
        PortInfo portInfo = new PortInfo();            
        String location = (String)portLocations.get(servlet);
        if (location != null) {
            portInfo.setLocation(location);
        }        
        return portInfo;
    }
    
    /*
     * Create servlet-class to servlet-names mapping
     */
    private Map<String, List<String>> createClassServetMap(WebAppType webApp) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        
        ServletType[] servletTypes = webApp.getServletArray();
        if (servletTypes != null) {
            for (ServletType servletType : servletTypes) {
                String servletName = servletType.getServletName().getStringValue().trim();
                if (servletType.isSetServletClass()) {
                    String servletClassName = servletType.getServletClass().getStringValue().trim();
                    List<String> servlets = map.get(servletClassName);
                    if (servlets == null) {
                        servlets = new ArrayList<String>();
                        map.put(servletClassName, servlets);
                    }
                    servlets.add(servletName);
                }
            }
        }
        
        return map;
    }
    
}
