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
package org.apache.geronimo.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBException;

import org.apache.geronimo.common.AbstractInterceptor;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.SimpleInvocationResult;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.metadata.EJBMetadata;
import org.apache.geronimo.ejb.metadata.MethodMetadata;

/**
 * This interceptor calls the callback method or throws an
 * IllegalArgumentException if there is no callback method in the invocation
 * object.  This should be the last interceptor in the chain.
 *
 *
 * @version $Revision: 1.7 $ $Date: 2003/08/27 03:43:36 $
 */
public final class CallbackInterceptor extends AbstractInterceptor {
    public InvocationResult invoke(Invocation invocation) throws Throwable {
        // Instance
        EnterpriseContext ctx = EJBInvocationUtil.getEnterpriseContext(invocation);
        if (ctx == null) {
            throw new IllegalArgumentException("Invocation does not contain an enterprise context");
        }
        Object instance = ctx.getInstance();
        if (instance == null) {
            throw new IllegalArgumentException("Context does not have an instance assigned");
        }

        // Method metadata
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(getContainer());
        Method interfaceMethod = EJBInvocationUtil.getMethod(invocation);
        MethodMetadata methodMetadata = ejbMetadata.getMethodMetadata(interfaceMethod);

        // Callback Method
        Method callbackMethod = methodMetadata.getCallbackMethod();
        if (callbackMethod == null) {
            throw new IllegalArgumentException("Invocation does not contain a callback method");
        }

        // Callback Arguments
        Object[] callbackArgs = EJBInvocationUtil.getArguments(invocation);

        // Invoke it
        try {
            return new SimpleInvocationResult(callbackMethod.invoke(instance, callbackArgs));
        } catch (IllegalAccessException e) {
            // This method is using the Java language access control and the
            // underlying method is inaccessible.
            throw new EJBException(e);
        } catch (InvocationTargetException e) {
            // unwrap the exception
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error("Unexpected Throwable", t);
            }
        }
    }
}
