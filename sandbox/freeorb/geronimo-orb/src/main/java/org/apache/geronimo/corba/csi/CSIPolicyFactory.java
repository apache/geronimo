/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.corba.csi;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.Security.*;
import org.omg.SecurityLevel2.Credentials;
import org.omg.SecurityLevel2.CredentialsListHelper;


class CSIPolicyFactory extends LocalObject
        implements org.omg.PortableInterceptor.PolicyFactory
{

    public Policy create_policy(int type, Any value)
            throws org.omg.CORBA.PolicyError
    {
        switch (type) {
            case SecMechanismsPolicy.value:
                return new MechanismPolicy(value);
            case SecInvocationCredentialsPolicy.value:
                return new InvocationCredentialsPolicy(value);
            case SecQOPPolicy.value:
                return new QOPPolicy(value);
            case SecEstablishTrustPolicy.value:
                return new EstablishTrustPolicy(value);
            case SecDelegationDirectivePolicy.value:
                return new DelegationDirectivePolicy(value);
            case SecGSSUPPolicy.value:
                return new GSSUPPolicy(value);
        }

        throw new org.omg.CORBA.PolicyError
                (org.omg.CORBA.BAD_POLICY.value);
    }

    static abstract class SecurityPolicy
            extends LocalObject
            implements Policy, Cloneable
    {

        public Policy copy() {
            try {
                return (Policy) super.clone();
            }
            catch (CloneNotSupportedException ex) {
                return null;
            }
        }

        public void destroy() {
            // do nothing //
        }
    }

    static class MechanismPolicy extends SecurityPolicy
            implements org.omg.SecurityLevel2.MechanismPolicy
    {

        String[] mechanisms;

        MechanismPolicy(Any value) {
            mechanisms = MechanismTypeListHelper.extract(value);
        }

        public int policy_type() {
            return SecMechanismsPolicy.value;
        }

        public String[] mechanisms() {
            return mechanisms;
        }

    }

    static class InvocationCredentialsPolicy extends SecurityPolicy
            implements org.omg.SecurityLevel2.InvocationCredentialsPolicy
    {

        Credentials[] creds;

        InvocationCredentialsPolicy(Any value) {
            creds = CredentialsListHelper.extract(value);
        }

        public int policy_type() {
            return SecInvocationCredentialsPolicy.value;
        }

        public Credentials[] creds() {
            return creds;
        }

    }

    static class QOPPolicy extends SecurityPolicy
            implements org.omg.SecurityLevel2.QOPPolicy
    {

        QOP qop;

        QOPPolicy(Any value) {
            qop = QOPHelper.extract(value);
        }

        public int policy_type() {
            return SecQOPPolicy.value;
        }

        public QOP qop() {
            return qop;
        }

    }

    static class EstablishTrustPolicy extends SecurityPolicy
            implements org.omg.SecurityLevel2.EstablishTrustPolicy
    {

        EstablishTrust trust;

        EstablishTrustPolicy(Any value) {
            trust = EstablishTrustHelper.extract(value);
        }

        public int policy_type() {
            return SecEstablishTrustPolicy.value;
        }

        public EstablishTrust trust() {
            return trust;
        }

    }

    static class DelegationDirectivePolicy extends SecurityPolicy
            implements org.omg.SecurityLevel2.DelegationDirectivePolicy
    {

        DelegationDirective directive;

        DelegationDirectivePolicy(Any value) {
            directive = DelegationDirectiveHelper.extract(value);
        }

        public int policy_type() {
            return SecDelegationDirectivePolicy.value;
        }

        public DelegationDirective delegation_directive() {
            return directive;
        }

    }

    static class GSSUPPolicy extends SecurityPolicy
            implements org.apache.geronimo.corba.csi.gssup.GSSUPPolicy
    {

        RequiresSupports mode;
        String domain;

        GSSUPPolicy(Any value) {
            GSSUPPolicyValue val = GSSUPPolicyValueHelper.extract(value);
            mode = val.mode;
            domain = val.domain;
        }

        public int policy_type() {
            return SecGSSUPPolicy.value;
        }

        public RequiresSupports mode() {
            return mode;
        }

        public String domain() {
            return domain;
        }

    }


}
