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
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

/**
 * Lists the results of a configuration download operation.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DownloadResults implements Serializable {
    private List configurationsPresent = new ArrayList();
    private List configurationsInstalled = new ArrayList();
    private List dependenciesPresent = new ArrayList();
    private List dependenciesInstalled = new ArrayList();

    void addConfigurationPresent(URI config) {
        configurationsPresent.add(config);
    }

    void addConfigurationInstalled(URI config) {
        configurationsInstalled.add(config);
    }

    void addDependencyPresent(URI dep) {
        dependenciesPresent.add(dep);
    }

    void addDependencyInstalled(URI dep) {
        dependenciesInstalled.add(dep);
    }

    public URI[] getConfigurationsPresent() {
        return (URI[]) configurationsPresent.toArray(new URI[configurationsPresent.size()]);
    }

    public URI[] getConfigurationsInstalled() {
        return (URI[]) configurationsInstalled.toArray(new URI[configurationsInstalled.size()]);
    }

    public URI[] getDependenciesPresent() {
        return (URI[]) dependenciesPresent.toArray(new URI[dependenciesPresent.size()]);
    }

    public URI[] getDependenciesInstalled() {
        return (URI[]) dependenciesInstalled.toArray(new URI[dependenciesInstalled.size()]);
    }
}
