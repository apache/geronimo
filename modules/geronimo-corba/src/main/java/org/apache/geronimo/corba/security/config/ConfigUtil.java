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
package org.apache.geronimo.corba.security.config;

import org.omg.CSIIOP.CompositeDelegation;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DelegationByClient;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.IdentityAssertion;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.NoDelegation;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.SimpleDelegation;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class ConfigUtil {

    public static String flags(int flag) {
        String result = "";

        if ((NoProtection.value & flag) != 0) {
            result += "NoProtection ";
        }
        if ((Integrity.value & flag) != 0) {
            result += "Integrity ";
        }
        if ((Confidentiality.value & flag) != 0) {
            result += "Confidentiality ";
        }
        if ((DetectReplay.value & flag) != 0) {
            result += "DetectReplay ";
        }
        if ((DetectMisordering.value & flag) != 0) {
            result += "DetectMisordering ";
        }
        if ((EstablishTrustInTarget.value & flag) != 0) {
            result += "EstablishTrustInTarget ";
        }
        if ((EstablishTrustInClient.value & flag) != 0) {
            result += "EstablishTrustInClient ";
        }
        if ((NoDelegation.value & flag) != 0) {
            result += "NoDelegation ";
        }
        if ((SimpleDelegation.value & flag) != 0) {
            result += "SimpleDelegation ";
        }
        if ((CompositeDelegation.value & flag) != 0) {
            result += "CompositeDelegation ";
        }
        if ((IdentityAssertion.value & flag) != 0) {
            result += "IdentityAssertion ";
        }
        if ((DelegationByClient.value & flag) != 0) {
            result += "DelegationByClient ";
        }

        return result;
    }
}
