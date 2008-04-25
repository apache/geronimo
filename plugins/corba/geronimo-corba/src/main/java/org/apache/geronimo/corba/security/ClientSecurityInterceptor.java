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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

import org.apache.geronimo.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.geronimo.corba.security.config.css.CSSConfig;
import org.apache.geronimo.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 502310 $ $Date: 2007-02-01 10:34:57 -0800 (Thu, 01 Feb 2007) $
 */
final class ClientSecurityInterceptor extends LocalObject implements ClientRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(ClientSecurityInterceptor.class);

    public ClientSecurityInterceptor() {
        if (log.isDebugEnabled()) log.debug("Registered");
    }

    public void receive_exception(ClientRequestInfo ri) {
    }

    public void receive_other(ClientRequestInfo ri) {
    }

    public void receive_reply(ClientRequestInfo ri) {
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public void send_request(ClientRequestInfo ri) {

        try {
            if (log.isDebugEnabled()) log.debug("Checking if target " + ri.operation() + " has a security policy");

            TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
            TSSCompoundSecMechListConfig csml = TSSCompoundSecMechListConfig.decodeIOR(Util.getCodec(), tc);

            if (log.isDebugEnabled()) log.debug("Target has a security policy");

            ClientPolicy clientPolicy = (ClientPolicy) ri.get_request_policy(ClientPolicyFactory.POLICY_TYPE);
            CSSConfig config = clientPolicy.getConfig();
            if (config == null) return;

            if (log.isDebugEnabled()) log.debug("Client has a security policy");

            List compat = config.findCompatibleSet(csml);

            if (compat.size() == 0) return;

            if (log.isDebugEnabled()) log.debug("Found compatible policy");

            ServiceContext context = ((CSSCompoundSecMechConfig) compat.get(0)).generateServiceContext();

            if (context == null) return;

            if (log.isDebugEnabled()) {
                log.debug("Msg context id: " + context.context_id);
                log.debug("Encoded msg: 0x" + Util.byteToString(context.context_data));
            }

            ri.add_request_service_context(context, true);
        } catch (BAD_PARAM bp) {
            // do nothing
        } catch (Exception ue) {
            log.error("Exception", ue);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.apache.geronimo.corba.security.ClientSecurityInterceptor";
    }
}