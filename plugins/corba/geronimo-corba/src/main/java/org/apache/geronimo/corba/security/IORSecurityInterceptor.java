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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
final class IORSecurityInterceptor extends LocalObject implements IORInterceptor {

    private final Logger log = LoggerFactory.getLogger(IORSecurityInterceptor.class);
    
    private final TSSConfig defaultConfig; 
    
    public IORSecurityInterceptor(TSSConfig defaultConfig) {
        this.defaultConfig = defaultConfig; 
    }

    public void establish_components(IORInfo info) {

        try {
            ServerPolicy policy = (ServerPolicy) info.get_effective_policy(ServerPolicyFactory.POLICY_TYPE);
            // try to get a config from the policy, and fall back on the default 
            TSSConfig config; 
            if (policy == null) {
                config = defaultConfig; 
            }
            else {
                config = policy.getConfig(); 
                if (config == null) {
                    config = defaultConfig; 
                }
            }
            // nothing to work with, just return 
            if (config == null) {
                return;
            }

            info.add_ior_component_to_profile(config.generateIOR(Util.getORB(), Util.getCodec()), TAG_INTERNET_IOP.value);
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
