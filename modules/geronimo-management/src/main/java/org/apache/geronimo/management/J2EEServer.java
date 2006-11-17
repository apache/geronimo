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
 * 
 * 
 * @version $Rev$ $Date$
 */
public interface J2EEServer extends J2EEManagedObject {
    /**
     * A list of all of the J2EEApplication and J2EEModule types deployed on this J2EEServer.
     * @see "JSR77.3.3.1.1"
     * @return the deployed objects on this server
     */
    String[] getDeployedObjects();

    /**
     * A list of resources available to this server.
     * @see "JSR77.3.3.1.2"
     * @return the resources available to this server
     */
    String[] getResources();

    /**
     * A list of all Java virtual machines on which this J2EEServer has running threads.
     * @see "JSR77.3.3.1.3"
     * @return the JVMs for this server
     */
    String[] getJavaVMs();

    /**
     * Identifies the J2EE platform vendor of this J2EEServer.
     * @see "JSR77.3.3.1.4"
     * @return the server vendor
     */
    String getServerVendor();

    /**
     * Identifies the J2EE implemetation version of this J2EEServer.
     * @see "JSR77.3.3.1.5"
     * @return the server version
     */
    String getServerVersion();
}
