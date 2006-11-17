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

package org.apache.geronimo.j2ee.management.impl;

import org.apache.geronimo.management.J2EEManagedObject;

/**
 * @version $Rev$ $Date$
 */
public class Util {
    private Util() {
    }

    public static String[] getObjectNames(J2EEManagedObject[] objects) {
        String[] objectNames = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            J2EEManagedObject object = objects[i];
            objectNames[i] = object.getObjectName();
        }
        return objectNames;
    }
}
