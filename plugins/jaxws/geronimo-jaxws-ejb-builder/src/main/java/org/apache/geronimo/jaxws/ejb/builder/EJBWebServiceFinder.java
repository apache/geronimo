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

package org.apache.geronimo.jaxws.ejb.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.WebServiceFinder;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EJBWebServiceFinder implements WebServiceFinder<EjbModule> {

    private static final Logger LOG = LoggerFactory.getLogger(EJBWebServiceFinder.class);

    public Map<String, PortInfo> discoverWebServices(EjbModule module, Map<String, String> correctedPortLocations) throws DeploymentException {
        Map<String, PortInfo> map = new HashMap<String, PortInfo>();
        discoverEJBWebServices(module, correctedPortLocations, map);
        return map;
    }

    private void discoverEJBWebServices(EjbModule ejbModule,
                                        Map<String, String> correctedPortLocations,
                                        Map<String, PortInfo> map)
        throws DeploymentException {
        Bundle bundle = ejbModule.getEarContext().getDeploymentBundle();
        Set<String> ejbWebServiceClassNames = new HashSet<String>();
        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {
            if (bean.type != EnterpriseBeanInfo.STATELESS && bean.type != EnterpriseBeanInfo.SINGLETON) {
                continue;
            }
            try {
                Class<?> ejbClass = bundle.loadClass(bean.ejbClass);
                if (JAXWSUtils.isWebService(ejbClass)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found EJB Web Service: " + bean.ejbName);
                    }
                    PortInfo portInfo = new PortInfo();
                    String location = correctedPortLocations.get(bean.ejbName);
                    if (location == null) {
                        // set default location, i.e. /@WebService.serviceName/@WebService.name
                        location = "/" + JAXWSUtils.getServiceName(ejbClass) + "/" + JAXWSUtils.getName(ejbClass);
                    }
                    portInfo.setLocation(location);
                    portInfo.setWsdlService(JAXWSUtils.getServiceQName(ejbClass));
                    portInfo.setWsdlPort(JAXWSUtils.getPortQName(ejbClass));
                    map.put(bean.ejbName, portInfo);
                    ejbWebServiceClassNames.add(bean.ejbClass);
                }
            } catch (Exception e) {
                throw new DeploymentException("Failed to load ejb class "
                                              + bean.ejbName, e);
            }
        }
        ejbModule.getSharedContext().put(EJB_WEB_SERVICE_CLASS_NAMES, ejbWebServiceClassNames);
    }
}
