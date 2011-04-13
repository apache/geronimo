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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean(name="Axis2ConfigGBean")
public class Axis2ConfigGBean implements GBeanLifecycle {

    private final static Logger LOG = LoggerFactory.getLogger(Axis2ConfigGBean.class);

    private AbstractName moduleName;
    private ClassLoader classLoder;
    private Axis2ModuleRegistry axis2ModuleRegistry;

    public Axis2ConfigGBean(@ParamAttribute(name = "moduleName") AbstractName moduleName,
                                                   @ParamReference(name = "Axis2ModuleRegistry") Axis2ModuleRegistry axis2ModuleRegistry,
                                                   @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) {
        this.moduleName = moduleName;
        this.classLoder = classLoader;
        this.axis2ModuleRegistry = axis2ModuleRegistry;
    }

    public synchronized static Axis2ClientConfigurationFactory registerClientConfigurationFactory(Axis2ModuleRegistry axis2ModuleRegistry) {
        ClientConfigurationFactory factory =
            (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);
        if (factory instanceof Axis2ClientConfigurationFactory) {
            return (Axis2ClientConfigurationFactory)factory;
        } else {
            factory = new Axis2ClientConfigurationFactory(axis2ModuleRegistry, false);
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, factory);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered client configuration factory: " + factory);
            }
            // ensure that the factory was installed at the right time
            if (factory != DescriptionFactoryImpl.getClientConfigurationFactory()) {
                throw new RuntimeException("Client configuration factory was registered too late");
            }
            return (Axis2ClientConfigurationFactory)factory;
        }
    }

    public void doStart() throws Exception {
        registerClientConfigurationFactory(axis2ModuleRegistry);
    }

    public void doStop() throws Exception {
        ConfigurationContext configContext =
            registerClientConfigurationFactory(axis2ModuleRegistry).clearCache(this.classLoder);
        DescriptionFactoryImpl.clearServiceDescriptionCache(configContext);
    }

    public void doFail() {
    }
}
