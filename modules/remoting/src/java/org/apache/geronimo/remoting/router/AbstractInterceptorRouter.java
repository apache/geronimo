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

package org.apache.geronimo.remoting.router;

import java.net.URI;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.TimeoutSync;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocation;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.remoting.InvocationSupport;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.transport.Msg;
import org.apache.geronimo.remoting.transport.TransportException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractInterceptorRouter implements GBeanLifecycle, Router {
    private long stoppedRoutingTimeout = 1000 * 60; // 1 min.

    /**
     * Allows us to pause invocations when in the stopped state.
     */
    private Sync routerLock = createNewRouterLock();

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
        infoFactory.addOperation("sendRequest", new Class[]{URI.class, Msg.class});
        infoFactory.addOperation("sendDatagram", new Class[]{URI.class, Msg.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
