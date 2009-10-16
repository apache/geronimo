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

package org.apache.geronimo.management;

/**
 * Identifies a J2EE application EAR that has been deployed.
 * @see "JSR77.3.6"
 *
 * @version $Rev$ $Date$
 */
public interface J2EEApplication extends J2EEDeployedObject {
    /**
     * A list of J2EEModules that comprise this application.
     * @see "JSR77.3.6.1.1"
     * @return the modules in this EAR
     */
    String[] getModules();
}
