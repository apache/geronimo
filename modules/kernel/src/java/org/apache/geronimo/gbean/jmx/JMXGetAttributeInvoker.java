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

import java.lang.reflect.Method;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 * @version $Revision: 1.1 $ $Date: 2004/06/02 06:49:23 $
 */
public final class JMXGetAttributeInvoker implements GBeanInvoker {
    private final MBeanServerConnection server;
    private final String name;
    private final Class[] declaredExceptions;

    public JMXGetAttributeInvoker(MBeanServerConnection server, Method method) {
        this.server = server;

        if (method.getName().startsWith("is")) {
            name = method.getName().substring(2);
        } else {
            name = method.getName().substring(3);
        }

        declaredExceptions = method.getExceptionTypes();
    }

    public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
        try {
            return server.getAttribute(objectName, name);
        } catch (Throwable t) {
            // todo do we need all this?
            Throwable throwable = t;
            while (true) {
                for (int i = 0; i < declaredExceptions.length; i++) {
                    Class declaredException = declaredExceptions[i];
                    if (declaredException.isInstance(throwable)) {
                        throw throwable;
                    }
                }

                // Unwrap the exceptions we understand
                if (throwable instanceof MBeanException) {
                    throwable = (((MBeanException) throwable).getTargetException());
                } else if (throwable instanceof ReflectionException) {
                    throwable = (((ReflectionException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeOperationsException) {
                    throwable = (((RuntimeOperationsException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeMBeanException) {
                    throwable = (((RuntimeMBeanException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeErrorException) {
                    throwable = (((RuntimeErrorException) throwable).getTargetError());
                } else {
                    // don't know how to unwrap this, just throw it
                    throw throwable;
                }
            }
        }
    }
}
