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
import org.apache.geronimo.security.jaas.JaasLoginServiceMBean;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasLoginServiceRemotingClient {
    static public JaasLoginServiceMBean create(String host, int port) throws IllegalArgumentException {
        URI target;
        try {
            target = new URI("async", null, host, port, "/JMX", null, "geronimo.remoting:target=JaasLoginServiceRemotingServer");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad host or port.");
        }
        return create(target);
    }

    static public JaasLoginServiceMBean create(URI target) {
        // Setup the client side container..
        RemoteTransportInterceptor remoteInterceptor = new RemoteTransportInterceptor(target);
        remoteInterceptor.setRemoteURI(target);

        Interceptor firstInterceptor = new MarshalingInterceptor(remoteInterceptor);
        firstInterceptor = new NotificationRemoterInterceptor(firstInterceptor);

        ProxyContainer clientContainer = new ProxyContainer(firstInterceptor);
        return (JaasLoginServiceMBean) clientContainer.createProxy(JaasLoginServiceMBean.class.getClassLoader(), new Class[]{JaasLoginServiceMBean.class});
    }
}
