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
import java.net.URI;
import java.io.IOException;

/**
 * Knows how to import and export configurations
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface ConfigurationInstaller {
    public ConfigurationMetadata[] listConfigurations(URL mavenRepository) throws IOException;
    public ConfigurationMetadata loadDependencies(URL mavenRepository, ConfigurationMetadata source) throws IOException;
    public DownloadResults install(URL mavenRepository, URI configId) throws IOException;
}
