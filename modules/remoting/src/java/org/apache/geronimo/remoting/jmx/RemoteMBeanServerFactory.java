/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.remoting.jmx;

import java.net.URI;
import java.net.URISyntaxException;

import javax.management.MBeanServer;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.remoting.MarshalingInterceptor;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:58:03 $
 */
public class RemoteMBeanServerFactory {

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
        Interceptor firstInterceptor = new RemoteTransportInterceptor(target);
        firstInterceptor = new MarshalingInterceptor(firstInterceptor);
        firstInterceptor = new NotificationRemoterInterceptor(firstInterceptor);

        ProxyContainer clientContainer = new ProxyContainer(firstInterceptor);
        return (MBeanServer) clientContainer.createProxy(
            MBeanServer.class.getClassLoader(),
            new Class[] { MBeanServer.class });
    }

    //TODO figure out if and how to use this method with only this one class.
    private static RemoteMBeanServerFactory createRemoteMBeanServerFactory() {
        try {
            // Get the factory name via sys prop...
            // In case we EVER used a different factory than: org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory
            String factoryClass = System.getProperty("org.apache.geronimo.enterprise.deploy.server.RemoteMBeanServerFactory", "org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory");
            return  (RemoteMBeanServerFactory) RemoteMBeanServerFactory.class.getClassLoader().loadClass(factoryClass).newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("The RemoteMBeanServerFactory instance could not be loaded:", e);
        }
    }
    /**
     * @see org.apache.geronimo.enterprise.deploy.server.RemoteMBeanServerFactory#factoryCreate(java.lang.String)
     */
    protected MBeanServer factoryCreate(String hostname) {
        return RemoteMBeanServerFactory.create(hostname);
    }


}
