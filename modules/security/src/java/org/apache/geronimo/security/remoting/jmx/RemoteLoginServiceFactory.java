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

package org.apache.geronimo.security.remoting.jmx;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.remoting.MarshalingInterceptor;
import org.apache.geronimo.remoting.jmx.NotificationRemoterInterceptor;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;
import org.apache.geronimo.security.jaas.LoginServiceMBean;


/**
 * @version $Rev$ $Date$
 */
public class RemoteLoginServiceFactory extends org.apache.geronimo.security.remoting.RemoteLoginServiceFactory {

    static public LoginServiceMBean create(String host) throws IllegalArgumentException {
        return create(host, 3434);
    }

    static public LoginServiceMBean create(String host, int port) throws IllegalArgumentException {
        URI target;
        try {
            target = new URI("async", null, host, port, "/JMX", null, "geronimo.remoting:target=LoginServiceStub");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad host or port.");
        }
        return create(target);
    }

    static public LoginServiceMBean create(URI target) {

        // Setup the client side container..
        RemoteTransportInterceptor remoteInterceptor = new RemoteTransportInterceptor(target);
        remoteInterceptor.setRemoteURI(target);

        Interceptor firstInterceptor = new MarshalingInterceptor(remoteInterceptor);
        firstInterceptor = new NotificationRemoterInterceptor(firstInterceptor);

        ProxyContainer clientContainer = new ProxyContainer(firstInterceptor);
        return (LoginServiceMBean) clientContainer.createProxy(LoginServiceMBean.class.getClassLoader(), new Class[]{LoginServiceMBean.class});
    }

    protected LoginServiceMBean factoryCreate(String hostname) {
        return RemoteLoginServiceFactory.create(hostname);
    }


}
