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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.CosTSInteroperation.TAG_OTS_POLICY;
import org.omg.CosTransactions.OTSPolicyValueHelper;
import org.omg.CosTransactions.ADAPTS;

import org.apache.geronimo.corba.util.Util;
import org.apache.geronimo.corba.util.TypeCode;


/**
 * @version $Revision: 502396 $ $Date: 2007-02-01 15:06:06 -0800 (Thu, 01 Feb 2007) $
 */
class ClientTransactionInterceptor extends LocalObject implements ClientRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(ClientTransactionInterceptor.class);

    public ClientTransactionInterceptor() {
        if (log.isDebugEnabled()) log.debug("Registered");
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest {
    }

    public void receive_reply(ClientRequestInfo ri) {
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public void send_request(ClientRequestInfo ri) throws ForwardRequest {
        TaggedComponent taggedComponent = null;
        try {
            if (log.isDebugEnabled()) log.debug("Checking if target " + ri.operation() + " has a transaction policy");

            taggedComponent = ri.get_effective_component(TAG_OTS_POLICY.value);
        } catch (BAD_PARAM e) {
            return;
        }

        if (log.isDebugEnabled()) log.debug("Target has a transaction policy");

        byte[] data = taggedComponent.component_data;
        Any any = null;
        try {
            any = Util.getCodec().decode_value(data, TypeCode.SHORT);
        } catch (FormatMismatch formatMismatch) {
            log.error("Mismatched format", formatMismatch);
            throw (INTERNAL) new INTERNAL("Mismatched format").initCause(formatMismatch);
        } catch (TypeMismatch typeMismatch) {
            log.error("Type mismatch", typeMismatch);
            throw (INTERNAL) new INTERNAL("Type mismatch").initCause(typeMismatch);
        }

        short value = OTSPolicyValueHelper.extract(any);
        if (value == ADAPTS.value) {
            ClientTransactionPolicy clientTransactionPolicy = (ClientTransactionPolicy) ri.get_request_policy(ClientTransactionPolicyFactory.POLICY_TYPE);
            if (clientTransactionPolicy == null) {
                return; 
            }
            ClientTransactionPolicyConfig clientTransactionPolicyConfig = clientTransactionPolicy.getClientTransactionPolicyConfig();
            if (clientTransactionPolicyConfig == null) return;

            if (log.isDebugEnabled()) log.debug("Client has a transaction policy");

            clientTransactionPolicyConfig.exportTransaction(ri);
        }
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
        return "org.apache.geronimo.corba.transaction.ClientTransactionInterceptor";
    }
}