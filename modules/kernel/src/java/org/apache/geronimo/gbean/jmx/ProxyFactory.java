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

import java.lang.reflect.InvocationTargetException;

/**
 * @version $Rev$ $Date$
 */
public abstract class ProxyFactory {
    private static final boolean useCGLib;

    static {
        boolean flag = false;
        try {
            Class.forName("net.sf.cglib.proxy.Enhancer");
            flag = true;
        } catch (Exception e) {
        }
        useCGLib = flag;
    }

    public static ProxyFactory newProxyFactory(Class type) {
        if (useCGLib) {
            return new CGLibProxyFactory(type);
        } else {
            return new VMProxyFactory(type);
        }
    }

    public abstract ProxyMethodInterceptor getMethodInterceptor();

    public abstract Object create(ProxyMethodInterceptor methodInterceptor);

    public abstract Object create(ProxyMethodInterceptor methodInterceptor, Class[] types, Object[] arguments);
}
