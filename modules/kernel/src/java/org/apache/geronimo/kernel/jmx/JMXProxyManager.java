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

import javax.management.ObjectName;
import net.sf.cglib.proxy.Callback;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicProxyManager;

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

    protected Callback getMethodInterceptor(Class proxyType, Kernel kernel, ObjectName target) {
        return new JMXProxyMethodInterceptor(proxyType, kernel, target);
    }
}
