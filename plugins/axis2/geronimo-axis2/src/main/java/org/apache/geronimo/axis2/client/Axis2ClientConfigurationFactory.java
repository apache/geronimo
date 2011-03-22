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

import java.util.Hashtable;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.geronimo.axis2.GeronimoConfigurator;
import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Axis2ClientConfigurationFactory extends ClientConfigurationFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(Axis2ClientConfigurationFactory.class);

    private Map<ClassLoader, ConfigurationContext> contextCache =
        new Hashtable<ClassLoader, ConfigurationContext>();

    private boolean reuseConfigurationContext;
    private Axis2ModuleRegistry axis2ModuleRegistry;

    public Axis2ClientConfigurationFactory(Axis2ModuleRegistry axis2ModuleRegistry, boolean reuse) {
        this.reuseConfigurationContext = reuse;
        this.axis2ModuleRegistry = axis2ModuleRegistry;
    }

    public ConfigurationContext getClientConfigurationContext() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            if (this.reuseConfigurationContext) {
                cl = ClientConfigurationFactory.class.getClassLoader();
            } else {
                return createConfigurationContext();
            }
        }

        synchronized (cl) {
            return getConfigurationContext(cl);
        }
    }

    private ConfigurationContext getConfigurationContext(ClassLoader cl) {
        ConfigurationContext context = this.contextCache.get(cl);
        if (context == null) {
            context = createConfigurationContext();
            axis2ModuleRegistry.configureModules(context);
            this.contextCache.put(cl, context);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created new configuration context " + context + "  for " + cl);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Configuration context " + context + " reused for " + cl);
            }
        }
        return context;
    }

    private ConfigurationContext removeConfigurationContext(ClassLoader cl) {
        return this.contextCache.remove(cl);
    }

    public void clearCache() {
        this.contextCache.clear();
    }

    public ConfigurationContext clearCache(ClassLoader cl) {
        ConfigurationContext context = null;
        if (cl != null) {
            synchronized (cl) {
                context = removeConfigurationContext(cl);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Removed configuration context " + context + " for " + cl);
            }
        }

        return context;
    }

    private ConfigurationContext createConfigurationContext() {
        try {
            GeronimoConfigurator configurator = new GeronimoConfigurator("META-INF/geronimo-axis2.xml");
            return ConfigurationContextFactory.createConfigurationContext(configurator);
        } catch (AxisFault e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
