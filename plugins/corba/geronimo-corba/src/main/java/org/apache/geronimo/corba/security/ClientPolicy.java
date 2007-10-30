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

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

import org.apache.geronimo.corba.security.config.css.CSSConfig;


/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class ClientPolicy extends LocalObject implements Policy {

    private final CSSConfig config;

    public ClientPolicy(CSSConfig ORBConfig) {
        this.config = ORBConfig;
    }

    public CSSConfig getConfig() {
        return config;
    }

    public int policy_type() {
        return ClientPolicyFactory.POLICY_TYPE;
    }

    public void destroy() {
    }

    public Policy copy() {
        return new ClientPolicy(config);
    }
}
