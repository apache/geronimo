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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicProxyManager;
import org.apache.geronimo.gbean.AbstractName;

/**
 * Pretty much the same as the BasicProxyManager, but it has a different way
 * of handling the actual invocations.
 *
 * @version $Rev$ $Date$
 */
public class JMXProxyManager extends BasicProxyManager {
    public JMXProxyManager(Kernel kernel) {
        super(kernel);
    }

    protected Callback getMethodInterceptor(Class proxyType, Kernel kernel, AbstractName target) {
        return new JMXProxyMethodInterceptor(proxyType, kernel, target);
    }

    protected void doDestroy(MethodInterceptor methodInterceptor) {
         ((JMXProxyMethodInterceptor)methodInterceptor).destroy();
    }

    protected AbstractName getAbstractName(MethodInterceptor methodInterceptor) {
        return ((JMXProxyMethodInterceptor)methodInterceptor).getAbstractName();
    }

}
