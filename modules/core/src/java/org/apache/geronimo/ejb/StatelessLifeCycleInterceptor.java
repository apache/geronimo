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

import java.lang.reflect.Method;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.geronimo.common.AbstractInterceptor;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.SimpleInvocationResult;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.metadata.EJBMetadata;

/**
 *
 *
 *
 * @version $Revision: 1.8 $ $Date: 2003/08/27 03:43:36 $
 */
public class StatelessLifeCycleInterceptor extends AbstractInterceptor {
    private static final Method removeRemote;
    private static final Method removeLocal;

    static {
        try {
            removeRemote = EJBObject.class.getMethod("remove", null);
            removeLocal = EJBLocalObject.class.getMethod("remove", null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Method createRemote;
    private Method createLocal;

    protected void doStart() throws Exception {
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(getContainer());
        Class homeInterface = ejbMetadata.getHomeInterface();
        if (homeInterface != null) {
            createRemote = homeInterface.getMethod("create", null);
        }

        Class localHomeInterface = ejbMetadata.getLocalHomeInterface();
        if (localHomeInterface != null) {
            createLocal = localHomeInterface.getMethod("create", null);
        }
    }

    protected void doStop() throws Exception {
        createRemote = null;
        createLocal = null;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        Method method = EJBInvocationUtil.getMethod(invocation);
        if (method == null) {
            return getNext().invoke(invocation);
        }

        if (method.equals(removeRemote) || method.equals(removeLocal)) {
            // remove for a stateless bean does nothing
            return new SimpleInvocationResult(null);
        } else if (method.equals(createRemote)) {
            EJBProxyFactoryManager ejbProxyFactoryManager = EJBPlugins.getEJBProxyFactoryManager(getContainer());
            EJBProxyFactory ejbProxyFactory = ejbProxyFactoryManager.getThreadEJBProxyFactory();
            if (ejbProxyFactory == null) {
                throw new IllegalStateException("No remote proxy factory set");
            }
            return new SimpleInvocationResult(ejbProxyFactory.getEJBObject());
        } else if (method.equals(createLocal)) {
            EJBProxyFactoryManager ejbProxyFactoryManager = EJBPlugins.getEJBProxyFactoryManager(getContainer());
            EJBProxyFactory ejbProxyFactory = ejbProxyFactoryManager.getEJBProxyFactory("local");
            if (ejbProxyFactory == null) {
                throw new IllegalStateException("No local proxy factoy set");
            }
            return new SimpleInvocationResult(ejbProxyFactory.getEJBObject());
        } else {
            return getNext().invoke(invocation);
        }
    }
}
