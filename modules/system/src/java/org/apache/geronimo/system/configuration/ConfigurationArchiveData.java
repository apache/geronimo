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

import java.io.Serializable;
import java.net.URL;

/**
 * The metadata included with a downloaded CAR file
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigurationArchiveData implements Serializable {
    private final URL repository;
    private final URL[] backups;
    private final ConfigurationMetadata configuration;

    public ConfigurationArchiveData(URL repository, URL[] backups, ConfigurationMetadata configuration) {
        this.repository = repository;
        this.backups = backups;
        this.configuration = configuration;
    }

    public URL getRepository() {
        return repository;
    }

    public URL[] getBackups() {
        return backups;
    }

    public ConfigurationMetadata getConfiguration() {
        return configuration;
    }
}
