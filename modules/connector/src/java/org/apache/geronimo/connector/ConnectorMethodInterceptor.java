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

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.management.ObjectName;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.DeadProxyException;

/**
 * MethodInterceptor used by various Proxies.  The important part of this class is the
 * deserialization in the readResolve method.
 *
 * @version $Rev$ $Date$
 *
 * */
public class ConnectorMethodInterceptor implements MethodInterceptor, Serializable {
    private final String kernelName;
    private final ObjectName targetName;

    private transient Object internalProxy;

    public ConnectorMethodInterceptor(final String kernelName, final ObjectName targetName) {
        this.kernelName = kernelName;
        this.targetName = targetName;
    }

    public Object intercept(final Object o, final Method method, Object[] objects, final MethodProxy methodProxy) throws Throwable {
        if (internalProxy == null) {
            connectInternalProxy();
        }
        try {
            return methodProxy.invoke(internalProxy, objects);
        } catch (DeadProxyException e) {
            connectInternalProxy();
            return methodProxy.invoke(internalProxy, objects);            
        }
    }

    public void setInternalProxy(final Object internalProxy) {
        this.internalProxy = internalProxy;
    }

    private void connectInternalProxy() throws Throwable {
        Kernel kernel = KernelRegistry.getKernel(kernelName);
        try {
            internalProxy = kernel.invoke(targetName, "$getConnectionFactory");
        } catch (Exception e) {
            throw new IllegalStateException("Could not connect proxy to ManagedConnectionFactoryWrapper").initCause(e);
        }
    }
}
