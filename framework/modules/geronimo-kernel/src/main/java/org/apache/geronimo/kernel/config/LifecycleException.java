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
public class LifecycleException extends Exception {
    private final String command;
    private final Artifact configurationId;
    private final LifecycleResults lifecycleResults;

    public LifecycleException(String command, Artifact configurationId, Throwable cause) {
        this(command, configurationId, new LifecycleResults(), cause);
        lifecycleResults.addFailed(configurationId, cause);
    }

    public LifecycleException(String command, Artifact configurationId, LifecycleResults lifecycleResults) {
        this(command, configurationId, lifecycleResults, lifecycleResults.getFailedCause(configurationId));
    }

    public LifecycleException(String command, Artifact configurationId, LifecycleResults lifecycleResults, Throwable cause) {
        super(command + " of " + configurationId + " failed", cause);
        this.command = command;
        this.configurationId = configurationId;
        this.lifecycleResults = lifecycleResults;
    }

    public String getCommand() {
        return command;
    }

    public Artifact getConfigurationId() {
        return configurationId;
    }

    public LifecycleResults getLifecycleResults() {
        return lifecycleResults;
    }
}
