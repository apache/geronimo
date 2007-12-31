/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.config;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class NullLifecycleMonitor implements LifecycleMonitor {
    public static final NullLifecycleMonitor INSTANCE = new NullLifecycleMonitor();

    public void addConfiguration(Artifact configurationId) {
    }

    public void resolving(Artifact configurationId) {
    }

    public void reading(Artifact configurationId) {
    }

    public void loading(Artifact configurationId) {
    }

    public void starting(Artifact configurationId) {
    }

    public void stopping(Artifact configurationId) {
    }

    public void unloading(Artifact configurationId) {
    }

    public void succeeded(Artifact configurationId) {
    }

    public void failed(Artifact configurationId, Throwable cause) {
    }

    public void finished() {
    }
}
