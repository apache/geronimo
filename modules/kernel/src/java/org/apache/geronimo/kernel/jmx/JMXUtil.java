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

package org.apache.geronimo.kernel.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * Helper class for JMX Operations
 *
 * @version $Revision: 1.7 $ $Date: 2004/05/27 01:06:00 $
 */
public final class JMXUtil {
    private JMXUtil() {
    }

    /**
     * the ObjectName of the MBeanServerDelegate
     */
    public static final ObjectName DELEGATE_NAME = getObjectName("JMImplementation:type=MBeanServerDelegate");

    /**
     * Convert a String to an ObjectName
     *
     * @param name the name
     * @return the ObjectName from that String
     * @throws java.lang.IllegalArgumentException if the name is malformed
     */
    public static ObjectName getObjectName(String name) throws IllegalArgumentException {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Malformed ObjectName: " + name);
        }
    }
}
