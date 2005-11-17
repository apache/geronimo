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

package org.apache.geronimo.security.remoting.jmx;

import java.io.IOException;
import java.io.Serializable;

import org.activeio.Packet;
import org.activeio.RequestListener;
import org.activeio.packet.EmptyPacket;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;

/**
 * @version $Rev$ $Date$
 */
public class RequestChannelInterceptorInvoker implements RequestListener {

    private ClassLoader classloader;
    private Interceptor next;

    public RequestChannelInterceptorInvoker(Interceptor next, ClassLoader classloader) {
        this.next = next;
        this.classloader = classloader;
    }

    public static class ThrowableWrapper implements Serializable {
        private static final long serialVersionUID = 3905243428970182455L;
        ThrowableWrapper(Throwable exception) {
            this.exception = exception;
        }
        public Throwable exception;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public Packet onRequest(Packet request) {
        Thread currentThread = Thread.currentThread();
        ClassLoader orig = currentThread.getContextClassLoader();
        try {
            
            Invocation marshalledInvocation;
            
            try {
                currentThread.setContextClassLoader(classloader);
                marshalledInvocation = (Invocation) RequestChannelInterceptor.deserialize(request,classloader);
            } catch (Throwable e) {
                // Could not deserialize the invocation...
                e.printStackTrace();
                return RequestChannelInterceptor.serialize(new ThrowableWrapper(e));                
            }

            try {
                InvocationResult rc = next.invoke(marshalledInvocation);
                return RequestChannelInterceptor.serialize(rc);                
            } catch (Throwable e) {
                return RequestChannelInterceptor.serialize(new ThrowableWrapper(e));                
            }

            
        } catch (IOException e) {
            // TODO: handle this.
            return EmptyPacket.EMPTY_PACKET;            
        } finally {
            currentThread.setContextClassLoader(orig);
        }
    }

    public void onRquestError(IOException error) {
        System.out.println("Request Error:"+error);
    }

}
