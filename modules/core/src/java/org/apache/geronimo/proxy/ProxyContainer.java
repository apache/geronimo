/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;

/**
 * A local container that is a proxy for some other "real" container.
 * This container is itself fairly unintelligent; you need to add some
 * interceptors to get the desired behavior (i.e. contacting the real
 * server on every request).  For example, see
 * {@link org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory}
 *
 * @version $Revision: 1.5 $ $Date: 2003/11/11 21:53:27 $
 */
public class ProxyContainer extends SimpleRPCContainer implements InvocationHandler {

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Invocation invocation = new ProxyInvocation();
        ProxyInvocation.putMethod(invocation, method);
        ProxyInvocation.putArguments(invocation, args);
        ProxyInvocation.putProxy(invocation, proxy);
        try {
            InvocationResult result = this.invoke(invocation);
            return result.getResult();
        } catch(UndeclaredThrowableException e) {
            throw e.getCause(); // It's useless to know we got an undeclared throwable, we need the real thing!
        }
    }

    public Object createProxy(Class likeClass) {
        return createProxy(likeClass.getClassLoader(), likeClass.getInterfaces());
    }

    public Object createProxy(ClassLoader cl, Class[] interfaces) {
        return Proxy.newProxyInstance(cl, interfaces, this);
    }

    public static ProxyContainer getContainer(Object proxy) {
        if (Proxy.isProxyClass(proxy.getClass()))
            throw new IllegalArgumentException("Not a proxy.");
        return (ProxyContainer) Proxy.getInvocationHandler(proxy);
    }

}
