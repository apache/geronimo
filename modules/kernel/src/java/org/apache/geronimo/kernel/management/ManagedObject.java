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

package org.apache.geronimo.kernel.management;

/**
 * This is a representation of the J2EEManagedObject type defined in the J2EE Management specification
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:02 $
 */
public interface ManagedObject {
    /**
     * Gets the unique name of this object.  The object name must comply with the ObjectName specification
     * in the JMX specification and the restrictions in the J2EEManagementInterface.
     *
     * @return the unique name of this object within the server
     */
    String getObjectName();

    /**
     * Determines if this object implements the state manageable type defined in the J2EE Management specification.
     *
     * @return true if this class also implements the state manageable type; false otherwise
     */
    boolean isStateManageable();

    /**
     * Determines if this object implements the statistics provider type defined in the J2EE Management specification.
     *
     * @return true if this class also implements the state manageable type; false otherwise
     */
    boolean isStatisticsProvider();

    /**
     * Determines if this object implemnts the event provider type defined in the J2EE Management specification.
     *
     * @return true if this class also implements the event manageable type; false otherwise
     */
    boolean isEventProvider();
}
