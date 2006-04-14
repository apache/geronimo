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
package org.apache.geronimo.system.configuration;

import java.net.URL;
import java.io.IOException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Knows how to import and export configurations
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface ConfigurationInstaller {
    /**
     * Lists the configurations available for download in a particular Geronimo repository.
     * @param mavenRepository The base URL to the maven repository
     * @param username Optional username, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param password Optional password, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     */
    public ConfigurationMetadata[] listConfigurations(URL mavenRepository, String username, String password) throws IOException;

    /**
     * Populates the dependency information for a particular configuration descriptor.
     * @param mavenRepository The base URL to the maven repository
     * @param username Optional username, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param password Optional password, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param source   The configuration descriptor that should have dependency
     *                 information added (though if this is a remote call a different
     *                 object will be returned).
     */
    public ConfigurationMetadata loadDependencies(URL mavenRepository, String username, String password, ConfigurationMetadata source) throws IOException;

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.
     * @param mavenRepository The base URL to the maven repository
     * @param username Optional username, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param password Optional password, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param configId The identifier of the configuration to install.
     */
    public DownloadResults install(URL mavenRepository, String username, String password, Artifact configId) throws IOException;
}
