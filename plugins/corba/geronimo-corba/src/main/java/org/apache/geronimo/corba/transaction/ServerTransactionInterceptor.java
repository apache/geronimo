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
package org.apache.geronimo.corba.transaction;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * @version $Revision: 476527 $ $Date: 2006-11-18 06:22:47 -0800 (Sat, 18 Nov 2006) $
 */
class ServerTransactionInterceptor extends LocalObject implements ServerRequestInterceptor {

    public ServerTransactionInterceptor() {
    }

    public void receive_request(ServerRequestInfo serverRequestInfo) throws ForwardRequest {
       ServerTransactionPolicy policy = (ServerTransactionPolicy) serverRequestInfo.get_server_policy(ServerTransactionPolicyFactory.POLICY_TYPE);
       // it's possible for applications to obtain the orb instance using "java:comp/ORB" and then
       // use the rootPOA to activate an object.  If that happens, then we're not going to see a
       // transaction policy on the request.  Just ignore any request that doesn't have one.
        if (policy != null) {
            ServerTransactionPolicyConfig serverTransactionPolicyConfig = policy.getServerTransactionPolicyConfig();
            serverTransactionPolicyConfig.importTransaction(serverRequestInfo);
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_reply(ServerRequestInfo ri) {
    }

    public void destroy() {
    }

    /**
     * Returns the name of the interceptor.
     * <p/>
     * Each Interceptor may have a name that may be used administratively
     * to order the lists of Interceptors. Only one Interceptor of a given
     * name can be registered with the ORB for each Interceptor type. An
     * Interceptor may be anonymous, i.e., have an empty string as the name
     * attribute. Any number of anonymous Interceptors may be registered with
     * the ORB.
     *
     * @return the name of the interceptor.
     */
    public String name() {
        return "org.apache.geronimo.corba.transaction.ServerTransactionInterceptor";
    }
}
