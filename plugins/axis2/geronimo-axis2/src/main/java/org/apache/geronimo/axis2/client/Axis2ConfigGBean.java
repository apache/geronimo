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
package org.apache.geronimo.axis2.client;

import java.net.URL;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;

public class Axis2ConfigGBean implements GBeanLifecycle {

    private final static Logger LOG = LoggerFactory.getLogger(Axis2ConfigGBean.class);
    
    private AbstractName moduleName;
    private ClassLoader classLoder;   

    public Axis2ConfigGBean(ClassLoader classLoader,                          
                            Kernel kernel,                          
                            URL configurationBaseUrl,
                            AbstractName moduleName) {
        this.moduleName = moduleName;
        this.classLoder = classLoader;
    }

    public synchronized static Axis2ClientConfigurationFactory registerClientConfigurationFactory() {
        ClientConfigurationFactory factory =
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        if (factory instanceof Axis2ClientConfigurationFactory) {
            return (Axis2ClientConfigurationFactory)factory;
        } else {
            factory = new Axis2ClientConfigurationFactory(false);
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, factory);
            LOG.debug("Registered client configuration factory: " + factory);
            // ensure that the factory was installed at the right time
            if (factory != DescriptionFactoryImpl.getClientConfigurationFactory()) {
                throw new RuntimeException("Client configuration factory was registered too late");           
            }
            return (Axis2ClientConfigurationFactory)factory;
        }
    }
    
    public void doStart() throws Exception {
        registerClientConfigurationFactory();
    }
    
    public void doStop() throws Exception {             
        ConfigurationContext configContext =
            registerClientConfigurationFactory().clearCache(this.classLoder);
        DescriptionFactoryImpl.clearServiceDescriptionCache(configContext);
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Axis2ConfigGBean.class, Axis2ConfigGBean.class, NameFactory.GERONIMO_SERVICE);
                
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);
        infoFactory.addAttribute("moduleName", AbstractName.class, true);
        
        infoFactory.setConstructor(new String[]{"classLoader", "kernel", "configurationBaseUrl", "moduleName"});
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
