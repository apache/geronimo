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

package org.apache.geronimo.remoting;

import java.io.Serializable;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;

/**
 * @version $Rev$ $Date$
 */
public class DeMarshalingInterceptor implements Interceptor {

    private ClassLoader classloader;
    private Interceptor next;

    public DeMarshalingInterceptor(Interceptor next) {
        this.next = next;
    }

    public DeMarshalingInterceptor(Interceptor next, ClassLoader classloader) {
        this.next = next;
        this.classloader = classloader;
    }

    public static class ThrowableWrapper implements Serializable {
        ThrowableWrapper(Throwable exception) {
            this.exception = exception;
        }
        public Throwable exception;
    }

    /**
     * @return
     */
    public ClassLoader getClassloader() {
        return classloader;
    }

    /**
     * @param classloader
     */
    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        Thread currentThread = Thread.currentThread();
        ClassLoader orig = currentThread.getContextClassLoader();
        try {
            Invocation marshalledInvocation;
            
            MarshalledObject mo = InvocationSupport.getMarshaledValue(invocation);;
            try {
                currentThread.setContextClassLoader(classloader);
                marshalledInvocation = (Invocation) mo.get();
            } catch (Throwable e) {
                // Could not deserialize the invocation...
                mo.set(new ThrowableWrapper(e));
                return new SimpleInvocationResult(false, mo);                
            }

            try {
                InvocationResult rc = next.invoke(marshalledInvocation);
                mo.set(new SimpleInvocationResult(rc.isNormal(), rc.getResult()));
                return new SimpleInvocationResult(true, mo);
            } catch (Throwable e) {
                mo.set(new ThrowableWrapper(e));
                return new SimpleInvocationResult(true, mo);
            }

        } finally {
            currentThread.setContextClassLoader(orig);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.core.service.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

}
