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

package org.apache.geronimo.naming.java;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;

/**
 * An interceptor that pushes the current component's java:comp context into
 * the java: JNDI namespace
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:57 $
 */
public class ComponentContextInterceptor implements Interceptor {
    private final Interceptor next;
    private final ReadOnlyContext compContext;

    /**
     * Constructor specifying the components JNDI Context (java:comp)
     * @param compContext the component's JNDI Context
     */
    public ComponentContextInterceptor(Interceptor next, ReadOnlyContext compContext) {
        this.next = next;
        this.compContext = compContext;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        ReadOnlyContext oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(compContext);
            return next.invoke(invocation);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
