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

import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.ProxyFactory;
import org.apache.geronimo.gbean.jmx.ProxyMethodInterceptor;

/**
 * MBeanProxyFactory creates a dynamic proxy to an MBean by ObjectName.
 * The interface type and object existance are enforced during construction.
 *
 * @version $Revision: 1.10 $ $Date: 2004/06/02 06:49:23 $
 */
public final class MBeanProxyFactory {

    /**
     * Creates an MBean proxy using the specified interface to the objectName.
     *
     * @param type the interface to implement for this proxy
     * @param server the MBeanServer in which the object is registered
     * @param objectName the objectName of the MBean to proxy
     * @return the new MBean proxy, which implemnts the specified interface
     */
    public static Object getProxy(Class type, MBeanServerConnection server, ObjectName objectName) {
        assert type != null;
        assert server != null;

        if (objectName.isPattern()) {
            throw new UnsupportedOperationException();
/*
            Set names = server.queryNames(objectName, null);
            if (names.isEmpty()) {
                throw new IllegalArgumentException("No names mbeans registered that match object name pattern: " + objectName);
            }
            objectName = (ObjectName) names.iterator().next();
*/
        }

        ProxyFactory factory = ProxyFactory.newProxyFactory(type);
        ProxyMethodInterceptor methodInterceptor = factory.getMethodInterceptor();
        methodInterceptor.connect(server, objectName);
        return factory.create(methodInterceptor);
    }
}
