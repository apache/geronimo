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
package org.apache.geronimo.remoting.router;

import java.net.URI;
import java.util.Set;
import java.util.HashSet;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocation;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
import org.apache.geronimo.remoting.InvocationSupport;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.transport.Msg;
import org.apache.geronimo.remoting.transport.TransportException;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.TimeoutSync;

/**
 * @version $Revision: 1.6 $ $Date: 2004/02/20 17:25:12 $
 */
public abstract class AbstractInterceptorRouter implements GBean, Router {
    private long stoppedRoutingTimeout = 1000 * 60; // 1 min.

    /**
     * Allows us to pause invocations when in the stopped state.
     */
    private Sync routerLock = createNewRouterLock();

    protected GBeanMBeanContext context;

    public long getStoppedRoutingTimeout() {
        return stoppedRoutingTimeout;
    }

    public void setStoppedRoutingTimeout(long stoppedRoutingTimeout) {
        this.stoppedRoutingTimeout = stoppedRoutingTimeout;
    }

    private Sync createNewRouterLock() {
        Latch lock = new Latch();
        return new TimeoutSync(lock, stoppedRoutingTimeout);
    }

    public Msg sendRequest(URI to, Msg msg) throws TransportException {
        try {
            routerLock.acquire();

            Interceptor interceptor = lookupInterceptorFrom(to);

            SimpleInvocation invocation = new SimpleInvocation();
            InvocationSupport.putMarshaledValue(invocation, msg.popMarshaledObject());
            InvocationSupport.putRemoteURI(invocation, to);

            InvocationResult result = interceptor.invoke(invocation);

            msg = msg.createMsg();
            Object rc = result.getResult();
            msg.pushMarshaledObject((MarshalledObject) rc);
            return msg;

        } catch (Throwable e) {
            e.printStackTrace();
            throw new TransportException(e.getMessage());
        }
    }

    public void sendDatagram(URI to, Msg msg) throws TransportException {
        try {
            routerLock.acquire();
            Interceptor interceptor = lookupInterceptorFrom(to);

            SimpleInvocation invocation = new SimpleInvocation();
            InvocationSupport.putMarshaledValue(invocation, msg.popMarshaledObject());
            InvocationSupport.putRemoteURI(invocation, to);

            interceptor.invoke(invocation);
        } catch (Throwable e) {
            throw new TransportException(e.getMessage());
        }
    }

    abstract protected Interceptor lookupInterceptorFrom(URI to) throws Throwable;

    public void setGBeanContext(GBeanContext context) {
        this.context = (GBeanMBeanContext) context;
    }

    public void doStart() {
        routerLock.release();
    }

    public void doStop() {
        routerLock = createNewRouterLock();
    }

    public void doFail() {
        // @todo do your best to clean up after a failure
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractInterceptorRouter.class);
        infoFactory.addOperation("sendRequest", new Class[] {URI.class, Msg.class});
        infoFactory.addOperation("sendDatagram", new Class[] {URI.class, Msg.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
