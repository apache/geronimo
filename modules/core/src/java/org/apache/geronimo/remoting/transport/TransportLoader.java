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
package org.apache.geronimo.remoting.transport;

import java.net.URI;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.jmx.MBeanProxyFactory;
import org.apache.geronimo.management.AbstractManagedObject;
import org.apache.geronimo.remoting.router.*;

/**
 *
 * @jmx:mbean
 *      extends="org.apache.geronimo.management.ManagedObject,org.apache.geronimo.management.StateManageable"
 *
 * @version $Revision: 1.4 $ $Date: 2003/09/01 20:38:49 $
 */
public class TransportLoader
    extends AbstractManagedObject
    implements TransportLoaderMBean
{
    URI bindURI;
    TransportServer transportServer;
    Router dispatchingRouter;
    private ObjectName routerTarget;

    /**
     * @see org.apache.geronimo.common.AbstractComponent#doStart()
     */
    protected void doStart() throws Exception {

        if (dispatchingRouter == null) {
            if (routerTarget == null)
                throw new IllegalStateException("Target router was not set.");
            RouterTargetMBean target =
                (RouterTargetMBean) MBeanProxyFactory.getProxy(RouterTargetMBean.class, server, routerTarget);
            dispatchingRouter = target.getRouter();
        }

        TransportFactory tf = TransportFactory.getTransportFactory(bindURI);
        transportServer = tf.createSever();
        transportServer.bind(bindURI, dispatchingRouter);
        transportServer.start();
    }

    /**
     * @see org.apache.geronimo.common.AbstractComponent#doStop()
     */
    protected void doStop() throws Exception {
        if (transportServer == null) {
            log.error("Cannot STOP. This component was never started.");
            return;
        }
        transportServer.stop();
        transportServer.dispose();
        transportServer = null;

    }
    /**
     *
     * @jmx:managed-attribute
     *
     * @return
     */
    public URI getBindURI() {
        return bindURI;
    }

    /**
     *
     * @jmx:managed-attribute
     *
     * @param bindURI
     */
    public void setBindURI(URI bindURI) {
        this.bindURI = bindURI;
    }

    /**
     *
     * @jmx:managed-attribute
     *
     * @return
     */
    public Router getDispatchingRouter() {
        return dispatchingRouter;
    }

    /**
     *
     * @jmx:managed-attribute
     *
     * @param dispatchingRouter
     */
    public void setDispatchingRouter(Router dispatchingRouter) {
        this.dispatchingRouter = dispatchingRouter;
    }

    /**
     *
     * @jmx:managed-attribute
     *
     * @param dispatchingRouter
     */
    public void setRouterTarget(String ob) throws MalformedObjectNameException {
        this.routerTarget = new ObjectName(ob);
    }

    /**
     *
     * @jmx:managed-attribute
     *
     * @return
     */
    public URI getClientConnectURI() {
        return transportServer.getClientConnectURI();
    }
}
