/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.management;

/**
 * Represents the JSR-77 type with the same name
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public interface J2EEModule extends J2EEDeployedObject {
    /**
     * A list of JVMs this module is running on.  Each JVM listed here must
     * be present in the owning J2EEServer's list of JVMs.
     * @see "JSR77.3.7.1.1"
     * @return the ObjectNames of the JVMs the module is running on
     */
    String[] getJavaVMs();
}
