/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Proxy;

import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;


/**
 * A client-side utility that connects to a remote login service.
 *
 * @version $Rev$ $Date$
 */
public class JaasLoginServiceRemotingClient {
    static public JaasLoginServiceMBean create(String host, int port) throws IllegalArgumentException {
        URI target;
        try {
            target = new URI("async", null, host, port, "/JMX", null, JaasLoginServiceRemotingServer.REQUIRED_OBJECT_NAME.getCanonicalName());
            return create(target);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad host or port.");
        } catch (IOException e) {
            throw new RuntimeException("IOException: "+e.getMessage(), e);
        }
    }

    static public JaasLoginServiceMBean create(URI target) throws IOException, URISyntaxException {

        ClassLoader cl = JaasLoginServiceMBean.class.getClassLoader();

        // Setup the client side container..
        RequestChannelInterceptor remoteInterceptor = new RequestChannelInterceptor(target, cl);
        Class[] interfaces = new Class[]{JaasLoginServiceMBean.class};
        return (JaasLoginServiceMBean) Proxy.newProxyInstance(cl, interfaces, remoteInterceptor);
    }

}
