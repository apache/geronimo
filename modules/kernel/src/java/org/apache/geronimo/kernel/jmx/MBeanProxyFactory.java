/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.ProxyFactory;
import org.apache.geronimo.gbean.jmx.ProxyMethodInterceptor;

/**
 * MBeanProxyFactory creates a dynamic proxy to an MBean by ObjectName.
 * The interface type and object existance are enforced during construction.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/25 09:57:49 $
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
    public static Object getProxy(Class type, MBeanServer server, ObjectName objectName) throws Exception {
        assert type != null;
        assert type.isInterface();
        assert server != null;

        if (objectName.isPattern()) {
            Set names = server.queryNames(objectName, null);
            if (names.isEmpty()) {
                throw new IllegalArgumentException("No names mbeans registered that match object name pattern: " + objectName);
            }
            objectName = (ObjectName) names.iterator().next();
        }

        ProxyFactory factory = new ProxyFactory(type);
        ProxyMethodInterceptor methodInterceptor = new ProxyMethodInterceptor(factory.getType());
        methodInterceptor.connect(server, objectName);
        return factory.create(methodInterceptor);
    }
}
