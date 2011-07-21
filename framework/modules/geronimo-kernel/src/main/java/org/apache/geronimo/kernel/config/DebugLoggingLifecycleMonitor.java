/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.kernel.config;

import org.apache.geronimo.kernel.repository.Artifact;
import org.slf4j.Logger;

/**
 * @version $Rev$ $Date$
 */
public class DebugLoggingLifecycleMonitor implements LifecycleMonitor {
    private final Logger log;


    public DebugLoggingLifecycleMonitor(Logger log) {
        this.log = log;
    }

    public void addConfiguration(Artifact configurationId) {
        log.debug("added module: {}", configurationId);
    }

    public void resolving(Artifact configurationId) {
        log.debug("resolving dependencies for module: {}", configurationId);
    }

    public void reading(Artifact configurationId) {
        log.debug("reading module: {}", configurationId);
    }

    public void loading(Artifact configurationId) {
        log.debug("loading module: {}", configurationId);
    }

    public void starting(Artifact configurationId) {
        log.debug("starting module: {}", configurationId);
    }

    public void stopping(Artifact configurationId) {
        log.debug("stopping module: {}", configurationId);
    }

    public void unloading(Artifact configurationId) {
        log.debug("unloading module: {}", configurationId);
    }

    public void succeeded(Artifact configurationId) {
        log.debug("succeeded module: {}", configurationId);
    }

    public void failed(Artifact configurationId, Throwable cause) {
        log.debug("failed module: {}", configurationId, cause);
    }

    public void finished() {
        log.debug("Lifecycle finished");
    }
}
