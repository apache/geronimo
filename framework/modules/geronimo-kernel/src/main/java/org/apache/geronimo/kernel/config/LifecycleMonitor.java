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
 * This interface can be used to monitor the progress of an operation on the
 * configuration manager.  Typically, the monitor will receive a number of
 * calls to addConfiguration as the configuration manager decides which
 * configurations will be effected by the operations.  This is followed by a
 * call to loading, starting, stopping or unloading and then a call to
 * succeeded or failed for each configuration added.
 *
 *
 * The one notable exception to this is the load operation which calls
 * addConfiguration and and immediately follows it with a reading and then
 * succeeded or failed.  This is because the load operation needs to read each
 * configuration to determine which additional configurations will need to be
 * loaded.
 *
 * When an operation fails, the failed method is called with the cause.  The
 * configuration manager normally will follow the failure with compensating
 * actions to bring the server back to the original state.  For example, if it
 * loaded a configuration, it will unload it.  Each of the compensating
 * actions will cause events to be fired.
 *
 * When the, operation is completed, the finished method will be called.  This
 *  should be called event if the operation ultimately fails and throws an
 * exception.  It is recommended that you do not rely the finished method to
 * be called in the case of an Exception as there are cases that will cause
 * the configuration manager to immediately return without notification (such
 * as an AssertionError).
 * @version $Rev$ $Date$
 */
public interface LifecycleMonitor {
    /**
     * Adds a configuration to be monitored.
     * @param configurationId the configuration identifier
     */
    void addConfiguration(Artifact configurationId);

    /**
     * The configuration manager has started resolving the dependencies of the specified configuration.
     * @param configurationId the configuration identifier
     */
    void resolving(Artifact configurationId);

    /**
     * The configuration manager has started reading the specified configuration.
     * @param configurationId the configuration identifier
     */
    void reading(Artifact configurationId);

    /**
     * The configuration manager has begun loading the specified configuration.
     * @param configurationId the configuration identifier
     */
    void loading(Artifact configurationId);

    /**
     * The configuration manager has begun starting the specified configuration.
     * @param configurationId the configuration identifier
     */
    void starting(Artifact configurationId);

    /**
     * The configuration manager has begun stopping the specified configuration.
     * @param configurationId the configuration identifier
     */
    void stopping(Artifact configurationId);

    /**
     * The configuration manager has begun unloading the specified configuration.
     * @param configurationId the configuration identifier
     */
    void unloading(Artifact configurationId);

    /**
     * The previous operation on the specified configuration has completed successfully.
     * @param configurationId the configuration identifier
     */
    void succeeded(Artifact configurationId);

    /**
     * The previous operation on the specified configuration has failed due to the specified exception.
     * @param configurationId the configuration identifier
     */
    void failed(Artifact configurationId, Throwable cause);

    /**
     * The operation on the configuration manager has finished.
     */
    void finished();
}

