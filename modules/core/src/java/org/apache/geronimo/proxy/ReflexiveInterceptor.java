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

package org.apache.geronimo.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;

/**
 * @version $Revision: 1.6 $ $Date: 2004/02/25 09:57:29 $
 */
public class ReflexiveInterceptor implements Interceptor {

    Object target;

    public ReflexiveInterceptor(Object target) {
        this.target = target;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        try {

            Method m = ProxyInvocation.getMethod(invocation);
            Object args[] = ProxyInvocation.getArguments(invocation);
            Object rc = m.invoke(target, args);
            return new SimpleInvocationResult(true, rc);

        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                return new SimpleInvocationResult(false, (Exception)t);
            } else {
                throw t;
            }
        }
    }

}