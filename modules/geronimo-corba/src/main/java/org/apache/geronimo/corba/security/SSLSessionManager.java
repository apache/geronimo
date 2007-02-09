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
package org.apache.geronimo.corba.security;

import java.util.Hashtable;
import java.util.Map;
import javax.net.ssl.SSLSession;


/**
 * Stores requests' SSL sessions so that they may be shared amongst portable
 * interceptors.  We use this singleton instead of using a ThreadLocal
 * because we cannot guarantee that interceptors will be called under
 * the same thread for a single request.
 * <p/>
 * TODO: There may be an error where the interceptor does not remove the
 * registered session.  We should have a daemon that cleans up old requests.
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class SSLSessionManager {
    private final static Map requestSSLSessions = new Hashtable();

    public static SSLSession getSSLSession(int requestId) {
        return (SSLSession) requestSSLSessions.get(new Integer(requestId));
    }

    public static void setSSLSession(int requestId, SSLSession session) {
        requestSSLSessions.put(new Integer(requestId), session);
    }

    public static SSLSession clearSSLSession(int requestId) {
        return (SSLSession) requestSSLSessions.remove(new Integer(requestId));
    }
}
