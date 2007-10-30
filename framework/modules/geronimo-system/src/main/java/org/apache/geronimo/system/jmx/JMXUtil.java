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

package org.apache.geronimo.system.jmx;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.kernel.management.NotificationType;


/**
 * Helper class for JMX Operations
 *
 * @version $Rev$ $Date$
 */
public final class JMXUtil {
    private JMXUtil() {
    }

    public static MBeanInfo toMBeanInfo(GBeanInfo gBeanInfo) {
        String className = gBeanInfo.getClassName();
        String description = "No description available";

        // attributes
        Set gbeanAttributes = gBeanInfo.getAttributes();
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[gbeanAttributes.size()];
        int a = 0;
        for (Iterator iterator = gbeanAttributes.iterator(); iterator.hasNext();) {
            GAttributeInfo gAttributeInfo = (GAttributeInfo) iterator.next();
            attributes[a] = new MBeanAttributeInfo(gAttributeInfo.getName(), gAttributeInfo.getType(), "no description available", gAttributeInfo.isReadable(), gAttributeInfo.isWritable(), isIs(gAttributeInfo));
            a++;
        }

        //we don't expose managed constructors
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[0];

        // operations
        Set gbeanOperations = gBeanInfo.getOperations();
        MBeanOperationInfo[] operations = new MBeanOperationInfo[gbeanOperations.size()];
        int o = 0;
        for (Iterator iterator = gbeanOperations.iterator(); iterator.hasNext();) {
            GOperationInfo gOperationInfo = (GOperationInfo) iterator.next();
            //list of class names
            List gparameters = gOperationInfo.getParameterList();
            MBeanParameterInfo[] parameters = new MBeanParameterInfo[gparameters.size()];
            int p = 0;
            for (Iterator piterator = gparameters.iterator(); piterator.hasNext();) {
                String type = (String) piterator.next();
                parameters[p] = new MBeanParameterInfo("parameter" + p, type, "no description available");
                p++;
            }
            operations[o] = new MBeanOperationInfo(gOperationInfo.getName(), "no description available", parameters, gOperationInfo.getReturnType() , MBeanOperationInfo.UNKNOWN);
            o++;
        }

        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[1];
        notifications[0] = new MBeanNotificationInfo(NotificationType.TYPES, "javax.management.Notification", "J2EE Notifications");

        MBeanInfo mbeanInfo = new MBeanInfo(className, description, attributes, constructors, operations, notifications);
        return mbeanInfo;
    }

    private static boolean isIs(GAttributeInfo gAttributeInfo) {
        String getterName = gAttributeInfo.getGetterName();
        if (getterName == null) {
            return false;
        }
        return getterName.startsWith("is");
    }
}
