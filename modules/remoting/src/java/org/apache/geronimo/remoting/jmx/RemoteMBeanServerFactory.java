/*
 * ==================================================================== The
 * Apache Software License, Version 1.1
 * 
 * Copyright (c) 2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself, if and
 * wherever such third-party acknowledgments normally appear.
 *  4. The names "Apache" and "Apache Software Foundation" and "Apache
 * Geronimo" must not be used to endorse or promote products derived from this
 * software without prior written permission. For written permission, please
 * contact apache@apache.org.
 *  5. Products derived from this software may not be called "Apache", "Apache
 * Geronimo", nor may "Apache" appear in their name, without prior written
 * permission of the Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation. For more information on the
 * Apache Software Foundation, please see <http://www.apache.org/> .
 * 
 * ====================================================================
 */
package org.apache.geronimo.remoting.jmx;

import java.net.URI;
import java.net.URISyntaxException;

import javax.management.MBeanServer;

import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.remoting.MarshalingInterceptor;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;

/**
 * @version $Revision: 1.1 $ $Date: 2003/11/16 05:27:27 $
 */
public class RemoteMBeanServerFactory extends org.apache.geronimo.enterprise.deploy.server.RemoteMBeanServerFactory {

    static public MBeanServer create(String host) throws IllegalArgumentException {
        return create(host, 3434);
    }

    static public MBeanServer create(String host, int port) throws IllegalArgumentException {
        URI target;
        try {
            target = new URI("async", null, host, port, "/JMX", null, "geronimo.remoting:target=MBeanServerStub");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad host or port.");
        }
        return create(target);
    }

    static public MBeanServer create(URI target) {

        // Setup the client side container..
        ProxyContainer clientContainer = new ProxyContainer();
        clientContainer.addInterceptor(new NotificationRemoterInterceptor());
        clientContainer.addInterceptor(new MarshalingInterceptor());
        RemoteTransportInterceptor transport = new RemoteTransportInterceptor();
        transport.setRemoteURI(target);
        clientContainer.addInterceptor(transport);

        return (MBeanServer) clientContainer.createProxy(
            MBeanServer.class.getClassLoader(),
            new Class[] { MBeanServer.class });
    }

    /**
     * @see org.apache.geronimo.enterprise.deploy.server.RemoteMBeanServerFactory#factoryCreate(java.lang.String)
     */
    protected MBeanServer factoryCreate(String hostname) {
        return RemoteMBeanServerFactory.create(hostname);
    }
}
