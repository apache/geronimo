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

package org.apache.geronimo.connector;

import java.lang.reflect.Method;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.InvalidObjectException;

import javax.management.ObjectName;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/09 20:15:43 $
 *
 * */
public class ConnectorMethodInterceptor implements MethodInterceptor, Serializable {

    private final String kernelName;
    private final ObjectName targetName;

    private transient Object internalProxy;

    public ConnectorMethodInterceptor(String kernelName, ObjectName targetName) {
        this.kernelName = kernelName;
        this.targetName = targetName;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (internalProxy == null) {
            throw new IllegalStateException("Proxy is not connected");
        }
        return methodProxy.invoke(internalProxy, objects);
    }

    public void setInternalProxy(Object internalProxy) {
        this.internalProxy = internalProxy;
    }

    private Object readResolve() throws ObjectStreamException {
        Kernel kernel = Kernel.getKernel(kernelName);
        try {
            return kernel.invoke(targetName, "getMethodInterceptor");
        } catch (Exception e) {
            throw (InvalidObjectException)new InvalidObjectException("could not get method interceptor from ManagedConnectionFactoryWrapper").initCause(e);
        }
    }

}
