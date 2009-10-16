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

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Describes a notification of a GBean.
 *
 * @version $Rev$ $Date$
 */
public final class GNotificationInfo implements Serializable {
    private static final long serialVersionUID = 2241808021653786721L;

    private final String name;
    private final Set notificationTypes;

    public GNotificationInfo(String name, Set notificationTypes) {
        this.name = name;
        this.notificationTypes = Collections.unmodifiableSet(notificationTypes);
    }

    public String getName() {
        return name;
    }

    public Set getNotificationTypes() {
        return notificationTypes;
    }

    public String toString() {
        return "[GNotificationInfo:" +
                 " name=" + name +
                 " notificationTypes=" + notificationTypes +
                 "]";
    }

    public String toXML() {
        String xml = "";

        xml += "<gNotificationInfo ";
        xml += "name='" + name + "' ";
        xml += ">";

        for (Iterator loop = notificationTypes.iterator(); loop.hasNext(); ) {
            xml += "<notificationType>" + loop.next().toString() + "</notificationType>";
        }

        xml += "</gNotificationInfo>";

        return xml;
    }
}
