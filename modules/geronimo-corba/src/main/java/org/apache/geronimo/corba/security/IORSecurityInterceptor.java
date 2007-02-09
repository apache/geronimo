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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
final class IORSecurityInterceptor extends LocalObject implements IORInterceptor {

    private final Log log = LogFactory.getLog(IORSecurityInterceptor.class);

    public void establish_components(IORInfo info) {

        try {
            ServerPolicy policy = (ServerPolicy) info.get_effective_policy(ServerPolicyFactory.POLICY_TYPE);

            if (policy == null || policy.getConfig() == null) return;

            info.add_ior_component_to_profile(policy.getConfig().generateIOR(Util.getORB(), Util.getCodec()), TAG_INTERNET_IOP.value);
        } catch (INV_POLICY e) {
            // do nothing
        } catch (Exception e) {
            log.error("Generating IOR", e);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.apache.geronimo.corba.security.IORSecurityInterceptor";
    }

}
