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

package org.apache.geronimo.gbean.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * @version $Rev$ $Date$
 */
public interface ProxyMethodInterceptor {

    void connect(MBeanServerConnection server, ObjectName objectName);

    void disconnect();

    static final class HashCodeInvoke implements GBeanInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(objectName.hashCode());
        }
    }

    static final class EqualsInvoke implements GBeanInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            // todo this is broken.. we need a way to extract the object name from the other proxy
            return Boolean.valueOf(objectName.equals(arguments[0]));
        }
    }

    static final class ToStringInvoke implements GBeanInvoker {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return interfaceName + objectName + "]";
        }
    }
}
