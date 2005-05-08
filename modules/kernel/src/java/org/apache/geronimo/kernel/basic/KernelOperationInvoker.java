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

package org.apache.geronimo.kernel.basic;

import java.lang.reflect.Method;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public final class KernelOperationInvoker implements ProxyInvoker {
    private final Kernel kernel;
    private final String name;
    private final String[] argumentTypes;

    public KernelOperationInvoker(Kernel kernel, Method method) {
        this.kernel = kernel;
        name = method.getName();

        // convert the parameters to a MBeanServer friendly string array
        Class[] parameters = method.getParameterTypes();
        argumentTypes = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argumentTypes[i] = parameters[i].getName();
        }
    }

    public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
        return kernel.invoke(objectName, name, arguments, argumentTypes);
    }
}
