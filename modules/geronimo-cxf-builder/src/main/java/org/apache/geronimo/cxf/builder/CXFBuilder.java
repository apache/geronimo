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
package org.apache.geronimo.cxf.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxws.javaee.PortComponentType;
import org.apache.cxf.jaxws.javaee.ServiceImplBeanType;
import org.apache.cxf.jaxws.javaee.WebserviceDescriptionType;
import org.apache.cxf.jaxws.javaee.WebservicesType;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.cxf.CXFWebServiceContainerFactoryGBean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class CXFBuilder extends JAXWSServiceBuilder {
    private static final Log LOG = LogFactory.getLog(CXFBuilder.class);

    public CXFBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected GBeanInfo getContainerFactoryGBeanInfo() {
        return CXFWebServiceContainerFactoryGBean.GBEAN_INFO;
    }

    protected Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
                                                              URL wsDDUrl,
                                                              JarFile moduleFile,
                                                              boolean isEJB,
                                                              Map correctedPortLocations)
            throws DeploymentException {

        LOG.debug("Parsing descriptor " + wsDDUrl);

        Map<String, PortInfo> map = null;

        try {
            JAXBContext ctx = JAXBContext.newInstance(WebservicesType.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(new StreamSource(in), WebservicesType.class);

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement) obj).getValue();
            }

            if (!(obj instanceof WebservicesType)) {
                return map;
            }
            WebservicesType wst = (WebservicesType) obj;

            for (WebserviceDescriptionType desc : wst.getWebserviceDescription()) {
                String wsdlFile = null;
                if (desc.getWsdlFile() != null) {
                    wsdlFile = getString(desc.getWsdlFile().getValue());
                }

                String serviceName = desc.getWebserviceDescriptionName().getValue();

                for (PortComponentType port : desc.getPortComponent()) {

                    PortInfo portInfo = new PortInfo();

                    String serviceLink = null;
                    ServiceImplBeanType beanType = port.getServiceImplBean();
                    if (beanType.getEjbLink() != null) {
                        serviceLink = beanType.getEjbLink().getValue();
                    } else if (beanType.getServletLink().getValue() != null) {
                        serviceLink = beanType.getServletLink().getValue();
                    }
                    portInfo.setServiceLink(serviceLink);

                    if (port.getServiceEndpointInterface() != null) {
                        String sei = port.getServiceEndpointInterface().getValue();
                        portInfo.setServiceEndpointInterfaceName(sei);
                    }

                    String portName = port.getPortComponentName().getValue();
                    portInfo.setPortName(portName);

                    portInfo.setProtocolBinding(port.getProtocolBinding());
                    portInfo.setServiceName(serviceName);
                    portInfo.setWsdlFile(wsdlFile);

                    if (port.getEnableMtom() != null) {
                        portInfo.setEnableMTOM(port.getEnableMtom().isValue());
                    }

                    portInfo.setHandlers(HandlerChainsType.class, port.getHandlerChains());

                    if (port.getWsdlPort() != null) {
                        portInfo.setWsdlPort(port.getWsdlPort().getValue());
                    }

                    if (port.getWsdlService() != null) {
                        portInfo.setWsdlService(port.getWsdlService().getValue());
                    }

                    String location = (String) correctedPortLocations.get(serviceLink);
                    portInfo.setLocation(location);

                    if (map == null) {
                        map = new HashMap<String, PortInfo>();
                    }
                    map.put(serviceLink, portInfo);
                }
            }

            return map;
        } catch (FileNotFoundException e) {
            return Collections.EMPTY_MAP;
        } catch (IOException ex) {
            throw new DeploymentException("Unable to read " + wsDDUrl, ex);
        } catch (JAXBException ex) {
            throw new DeploymentException("Unable to parse " + wsDDUrl, ex);
        } catch (Exception ex) {
            throw new DeploymentException("Unknown deployment error", ex);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static String getString(String in) {
        if (in != null) {
            in = in.trim();
            if (in.length() == 0) {
                return null;
            }
        }
        return in;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
