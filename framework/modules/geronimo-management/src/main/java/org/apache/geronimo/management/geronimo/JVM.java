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

package org.apache.geronimo.management.geronimo;

import java.util.Date;
import java.util.Properties;

import org.apache.geronimo.logging.SystemLog;

/**
 * Geronimo extensions to the standard JSR-77 JVM type.
 *
 * @version $Rev$ $Date$
 */
public interface JVM extends org.apache.geronimo.management.JVM {
    int getAvailableProcessors();

    /**
     * Gets the date at which the kernel was most recently started in this JVM
     */
    Date getKernelBootTime();
    /**
     * Gets the system properties for this JVM
     */
    Properties getSystemProperties();

    /**
     * Gets the system log instance
     */
    SystemLog getSystemLog();

    boolean isRedefineClassesSupported();
    
}
