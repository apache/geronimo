/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb;

import java.util.Map;

import org.apache.geronimo.openwebbeans.GeronimoSingletonService;
import org.apache.geronimo.openwebbeans.OpenWebBeansWebInitializer;
import org.apache.geronimo.openwebbeans.OsgiMetaDataScannerService;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiResourceInjectionService;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.lifecycle.StandaloneLifeCycle;
import org.apache.webbeans.spi.ResourceInjectionService;

/**
 * @version $Rev$ $Date$
 */
public class ThreadSingletonServiceAdapter extends GeronimoSingletonService implements ThreadSingletonService {
    public ThreadSingletonServiceAdapter() {
        super();
    }

    @Override
    public void initialize(org.apache.openejb.cdi.OWBContext owbContext) {
        Object old = contextEntered(owbContext);
        try {
            setConfiguration(OpenWebBeansConfiguration.getInstance());
        } finally {
            contextExited(old);
        }
    }

    private void setConfiguration(OpenWebBeansConfiguration configuration) {
        configuration.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");

        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, StandaloneLifeCycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, OpenWebBeansWebInitializer.NoopJndiService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
        configuration.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
//        configuration.setProperty(ELAdaptor.class.getName(), EL22Adaptor.class.getName());
    }

    @Override
    public Object contextEntered(org.apache.openejb.cdi.OWBContext owbContext) {
        return GeronimoSingletonService.contextEntered(owbContext.getSingletons());
    }

    @Override
    public void contextExited(Object oldContext) {
        if (oldContext != null && !(oldContext instanceof Map)) throw new IllegalArgumentException("Expecting a Map<String, Object> not " + oldContext.getClass().getName());
        GeronimoSingletonService.contextExited((Map<String, Object>)oldContext);
    }
}
