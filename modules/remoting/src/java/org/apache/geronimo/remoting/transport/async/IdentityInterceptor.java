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

package org.apache.geronimo.remoting.transport.async;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.geronimo.common.Classes;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.proxy.ProxyInvocation;

/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:20 $
 */
public class IdentityInterceptor implements Interceptor, Serializable {

    private static final Method EQUALS_METHOD = Classes.getMethod(Object.class, "equals");
    private static final Method HASHCODE_METHOD = Classes.getMethod(Object.class, "hashCode");
    
    private RemoteRef ref;
    private Interceptor next;

    /**
     * @param ref
     */
    public IdentityInterceptor(Interceptor next, RemoteRef ref) {
        this.next = next;
        this.ref = ref;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        Method method = ProxyInvocation.getMethod(invocation);
        if( method.equals(EQUALS_METHOD) ) {
            Object proxy = ProxyInvocation.getProxy(invocation);
            Object[] args = ProxyInvocation.getArguments(invocation);
            return new SimpleInvocationResult( true, proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);  
        } else if( method.equals(HASHCODE_METHOD) ) {
            return new SimpleInvocationResult( true, new Integer(ref.hashCode()) );
        }        
        return next.invoke(invocation);
    }

}
