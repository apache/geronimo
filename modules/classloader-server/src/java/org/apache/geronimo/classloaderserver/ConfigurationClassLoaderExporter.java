/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.classloaderserver;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationClassLoader;


/**
 *
 * @version $Rev: 109957 $ $Date: 2004-12-06 18:52:06 +1100 (Mon, 06 Dec 2004) $
 */
public class ConfigurationClassLoaderExporter implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ConfigurationClassLoaderExporter.class);
    
    private final ClassLoaderServer networkServer;
    private final Configuration configuration;
    private ClassLoaderInfoAdapter adapter;
    
    public ConfigurationClassLoaderExporter(ClassLoaderServer networkServer, Configuration configuration) {
        this.networkServer = networkServer;
        this.configuration = configuration;
    }

    public void doStart() throws Exception {
        ConfigurationClassLoader cl = configuration.getConfigurationClassLoader();
        
        adapter = new ClassLoaderInfoAdapter(cl);
        networkServer.export(adapter);
    }

    public void doStop() throws Exception {
        networkServer.unexport(adapter);
    }

    public void doFail() {
        try {
            networkServer.unexport(adapter);
        } catch (ClassLoaderServerException e) {
            log.error(e);
        }
    }
    
    private static class ClassLoaderInfoAdapter implements ClassLoaderInfo {
        private final ConfigurationClassLoader cl;
        
        private ClassLoaderInfoAdapter(ConfigurationClassLoader cl) {
            this.cl = cl;
        }
        
        public ClassLoader getClassLoader() {
            return cl;
        }

        public Object getID() {
            return cl.getID();
        }

        public void setClassLoaderServerURLs(URL[] urls) {
            cl.setClassLoaderServerURLs(urls);
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("ConfigurationClassLoader Exporter", ConfigurationClassLoaderExporter.class);

        infoBuilder.addReference("ClassLoaderServer", ClassLoaderServer.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("Configuration", Configuration.class, NameFactory.CONFIGURATION_ENTRY);
        
        infoBuilder.setConstructor(new String[] {"ClassLoaderServer", "Configuration"});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

