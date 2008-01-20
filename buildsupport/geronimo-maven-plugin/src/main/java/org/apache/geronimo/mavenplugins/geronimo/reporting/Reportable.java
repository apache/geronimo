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

import java.io.File;
import java.util.Date;

/**
 * Provides the details of the report to a {@link Reporter}.
 *
 * @version $Rev$ $Date$
 */
public interface Reportable
{
    /**
     * Returns the date at which the goal was started.
     *
     * @return  The date when the goal was started.
     */
    Date getStartTime();

    /**
     * Returns the name of the goal.
     *
     * @return  Goal name.
     */
    String getName();

    /**
     * Returns the log file which the goal may or may not output logs to.
     *
     * @return  The log file; or null of the goal does not log to a file.
     */
    File getLogFile();
}
