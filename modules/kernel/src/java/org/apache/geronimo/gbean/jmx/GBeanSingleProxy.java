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

import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class GBeanSingleProxy {
    /**
     * The GBean we are proxying.
     */
    private ObjectName target;

    /**
     * Proxy implementation held by the component
     */
    private Object proxy;

    /**
     * The interceptor for the proxy instance
     */
    private ProxyMethodInterceptor methodInterceptor;

    public GBeanSingleProxy(Kernel kernel, Class type, ObjectName target) throws Exception {
        assert kernel != null: "kernel is null";
        assert type != null: "type is null";
        assert target != null: "target is null";

        this.target = target;
        ProxyFactory factory = ProxyFactory.newProxyFactory(type);
        methodInterceptor = factory.getMethodInterceptor();
        proxy = factory.create(methodInterceptor);

        methodInterceptor.connect(kernel.getMBeanServer(), target);
    }

    public synchronized void destroy() {
        methodInterceptor.disconnect();

        proxy = null;
        methodInterceptor = null;
    }

    public synchronized Object getProxy() {
        return proxy;
    }

    public ObjectName getTarget() {
        return target;
    }
}
