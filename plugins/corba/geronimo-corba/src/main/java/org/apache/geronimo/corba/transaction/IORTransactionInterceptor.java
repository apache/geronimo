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
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CosTSInteroperation.TAG_INV_POLICY;
import org.omg.CosTransactions.ADAPTS;
import org.omg.CosTransactions.SHARED;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_OTS_POLICY;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
final class IORTransactionInterceptor extends LocalObject implements IORInterceptor {

    private final Logger log = LoggerFactory.getLogger(IORTransactionInterceptor.class);

    public void establish_components(IORInfo info) {

        try {
            Any invAny = ORB.init().create_any();
            invAny.insert_short(SHARED.value);
            byte[] invBytes = Util.getCodec().encode_value(invAny);
            TaggedComponent invocationPolicyComponent = new TaggedComponent(TAG_INV_POLICY.value, invBytes);
            info.add_ior_component_to_profile(invocationPolicyComponent, TAG_INTERNET_IOP.value);

            Any otsAny = ORB.init().create_any();
            otsAny.insert_short(ADAPTS.value);
            byte[] otsBytes = Util.getCodec().encode_value(otsAny);
            TaggedComponent otsPolicyComponent = new TaggedComponent(TAG_OTS_POLICY.value, otsBytes);
            info.add_ior_component_to_profile(otsPolicyComponent, TAG_INTERNET_IOP.value);
        } catch (INV_POLICY e) {
            // do nothing
        } catch (Exception e) {
            log.error("Generating IOR", e);
        }
    }

    public void destroy() {
    }

    public String name() {
        return "org.apache.geronimo.corba.transaction.IORTransactionInterceptor";
    }

}
