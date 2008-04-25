/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.yoko;

import java.net.Socket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.yoko.orb.PortableInterceptor.ServerRequestInfoExt;
import org.apache.yoko.orb.OCI.IIOP.TransportInfo_impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import org.apache.geronimo.corba.security.SSLSessionManager;

/**
 * A service context interceptor to help manage
 * SSL security information for incoming connections.
 * @version $Revision: 452600 $ $Date: 2006-10-03 12:29:42 -0700 (Tue, 03 Oct 2006) $
 */
final class ServiceContextInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(ServiceContextInterceptor.class);

    public ServiceContextInterceptor() {
        if (log.isDebugEnabled()) log.debug("<init>");
    }

    public void receive_request(ServerRequestInfo ri) {
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {

        if (log.isDebugEnabled()) log.debug("Looking for SSL Session");

        // for an incoming request, we need to see if the request is coming in on
        // an SSLSocket.  If this is using a secure connection, then we register the
        // request and SSLSession with the session manager.
        ServerRequestInfoExt riExt = (ServerRequestInfoExt) ri;
        TransportInfo_impl connection = (TransportInfo_impl)riExt.getTransportInfo();
        if (connection != null) {
            Socket socket = connection.socket();
            if (socket != null && socket instanceof SSLSocket) {
                if (log.isDebugEnabled()) log.debug("Found SSL Session");
                SSLSocket sslSocket = (SSLSocket) socket;

                SSLSessionManager.setSSLSession(ri.request_id(), sslSocket.getSession());
            }
        }
    }

    public void send_exception(ServerRequestInfo ri) {
        // clean any SSL session information if we registered.
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_exception");
    }

    public void send_other(ServerRequestInfo ri) {
        // clean any SSL session information if we registered.
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_reply");
    }

    public void send_reply(ServerRequestInfo ri) {
        // clean any SSL session information if we registered.
        SSLSession old = SSLSessionManager.clearSSLSession(ri.request_id());
        if (log.isDebugEnabled() && old != null) log.debug("Removing SSL Session for send_reply");
    }

    public void destroy() {
        if (log.isDebugEnabled()) log.debug("Destroy");
    }

    public String name() {
        return "org.apache.geronimo.yoko.ServiceContextInterceptor";
    }
}
