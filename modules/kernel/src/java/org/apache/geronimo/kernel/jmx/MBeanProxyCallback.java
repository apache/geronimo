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

import java.lang.reflect.Method;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This class handles invocations for MBean proxies.  Normally only the getObjectName method is necessary.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:49 $
 */
public final class MBeanProxyCallback implements MethodInterceptor {
    private final InvokeMBean[] methodTable;
    private final MBeanServer server;
    private final ObjectName objectName;

    public MBeanProxyCallback(InvokeMBean[] methodTable, MBeanServer server, ObjectName objectName) {
        this.methodTable = methodTable;
        this.server = server;
        this.objectName = objectName;
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return methodTable[proxy.getSuperIndex()].invoke(server, objectName, args);
    }
}
