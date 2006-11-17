/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo.reporting;

/**
 * Provides an asbtraction to allow pluggable report handling.
 *
 * @version $Rev$ $Date$
 */
public interface Reporter
{
    /**
     * Signals the start of a reporting run.  This method should collect the required information.
     *
     * @param source    The source of the report; must not be null.
     */
    void reportBegin(Reportable source);

    /**
     * Called when a failure has occured while executing a goal.
     *
     * @param cause     The cause of the error; must not be null.
     */
    void reportError(Throwable cause);

    /**
     * Called after the goal has run (with or with-out errors).
     */
    void reportEnd();
}
