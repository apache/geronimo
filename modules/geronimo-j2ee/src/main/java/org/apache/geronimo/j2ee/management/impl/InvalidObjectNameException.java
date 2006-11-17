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

import javax.management.ObjectName;

/**
 * @version $Rev$ $Date$
 */
public class InvalidObjectNameException extends RuntimeException {
    private final ObjectName objectName;

    public InvalidObjectNameException(ObjectName objectName) {
        super(objectName.getCanonicalName());
        this.objectName = objectName;
    }

    public InvalidObjectNameException(String message, ObjectName objectName) {
        super(message + ": " + objectName);
        this.objectName = objectName;
    }

    public InvalidObjectNameException(String message, Throwable cause, ObjectName objectName) {
        super(message + ": " + objectName, cause);
        this.objectName = objectName;
    }

    public ObjectName getObjectName() {
        return objectName;
    }
}
