/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * Callback supplied by a deployer to allow DeploymentModules to build the
 * output Configuration.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:35 $
 */
public interface ConfigurationCallback {
    /**
     * Add a file to the output Configuration
     * @param path the path, relative to the root of the Configuration, to use for the file;
     *        to avoid conflicts between modules this path should typically include the
     *        module id supplied to the factory
     * @param source a stream that can be read to provide the file's content
     * @throws IOException if there was a problem adding the file to the configuration
     */
    void addFile(URI path, InputStream source) throws IOException;

    /**
     * Add a location to the Configuration's classpath.
     * This would typically be a relative URI for resolution against the
     * Configuration root and include the module id; absolute references to
     * external resources are allowed.
     * @param uri the location to add to the classpath
     */
    void addToClasspath(URI uri);

    /**
     * Add a GMBean to the Configuration
     * @param name the JMX name to use for the bean
     * @param gbean the GBean
     */
    void addGBean(ObjectName name, GBeanMBean gbean);
}
