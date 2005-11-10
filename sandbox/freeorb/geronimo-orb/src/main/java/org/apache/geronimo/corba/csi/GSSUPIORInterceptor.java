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
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CSI.*;
import org.omg.CSIIOP.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.Security.DelegationDirective;
import org.omg.Security.RequiresSupports;
import org.omg.Security.SecDelegationDirectivePolicy;
import org.omg.SecurityLevel2.DelegationDirectivePolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.corba.csi.gssup.GSSUPPolicy;
import org.apache.geronimo.corba.csi.gssup.SecGSSUPPolicy;


/**
 * This interceptor adds GSSUP security information to the IOR, if the relevant
 * policy is set.
 */
public class GSSUPIORInterceptor extends CSIInterceptorBase implements
                                                            org.omg.PortableInterceptor.IORInterceptor
{

    private static final Log log = LogFactory.getLog(GSSUPIORInterceptor.class);

    GSSUPIORInterceptor(Codec codec) {
        super(codec);
    }

    public void establish_components(IORInfo info) {
        try {
            TaggedComponent mechanism_list = constructMechList(info);

            if (mechanism_list != null) {
                // add this component to all outgoing profiles!
                info.add_ior_component(mechanism_list);
            }
        }
        catch (NullPointerException ex) {
            // ex.printStackTrace ();
            throw ex;
        }
    }

    public String name() {
        return "CSI IOR Interceptor";
    }

    private TaggedComponent constructMechList(IORInfo info) {
        short as_target_requires = (short) 0;
        short as_target_supports = (short) 0;
        short sas_target_requires = (short) 0;
        short sas_target_supports = (short) 0;

        GSSUPPolicy gp = null;
        String gssup_realm = null;

        boolean has_security = false;

        try {
            gp = (GSSUPPolicy) info.get_effective_policy(SecGSSUPPolicy.value);

            if (gp.mode() == RequiresSupports.SecRequires) {
                as_target_requires |= EstablishTrustInClient.value;
            }

            as_target_supports |= EstablishTrustInClient.value;

            gssup_realm = gp.domain();
            has_security = true;

        }
        catch (org.omg.CORBA.INV_POLICY ex) {
            // ignore
        }

        try {
            DelegationDirectivePolicy delegatePolicy = (DelegationDirectivePolicy) info
                    .get_effective_policy(SecDelegationDirectivePolicy.value);

            if (delegatePolicy != null
                && delegatePolicy.delegation_directive() == DelegationDirective.Delegate)
            {
                sas_target_supports |= DelegationByClient.value
                                       | IdentityAssertion.value;
                has_security = true;
            }
        }
        catch (org.omg.CORBA.INV_POLICY ex) {
            // ignore
        }

        if (!has_security) {
            return null;
        }

        CompoundSecMech mech = new CompoundSecMech();

        AS_ContextSec as = new AS_ContextSec();
        as.target_supports = as_target_supports;
        as.target_requires = as_target_requires;

        if (as_target_supports != 0) {
            as.client_authentication_mech = GSSUP_OID;

            if (gssup_realm != null) {
                as.target_name = encodeGSSExportedName(gssup_realm);
            } else {
                as.target_name = EMPTY_BARR;
            }
        } else {
            as.target_name = EMPTY_BARR;
            as.client_authentication_mech = EMPTY_BARR;
        }

        if (log.isDebugEnabled()) {
            log.debug("AS.target_requires=" + as_target_requires);
            log.debug("AS.target_supports=" + as_target_supports);
            log.debug("SAS.target_requires=" + sas_target_requires);
            log.debug("SAS.target_supports=" + sas_target_supports);
        }

        SAS_ContextSec sas = new SAS_ContextSec();

        sas.target_supports = sas_target_supports;
        sas.target_requires = sas_target_requires;
        sas.privilege_authorities = new ServiceConfiguration[0];
        sas.supported_naming_mechanisms = new byte[][]{GSSUP_OID};

        sas.supported_identity_types = ITTAnonymous.value;

        if (as_target_supports != 0) {
            sas.supported_identity_types |= ITTAbsent.value;
        }

        if (sas_target_supports != 0) {
            sas.supported_identity_types |= ITTPrincipalName.value
                                            | ITTDistinguishedName.value | ITTX509CertChain.value;
        }

        // transport mech is null here, this field is modified by code
        // inside SSL server-side logic, adding SSL-specific information.
        mech.transport_mech = new TaggedComponent(TAG_NULL_TAG.value,
                                                  EMPTY_BARR);
        mech.target_requires = (short) (as_target_requires | sas_target_requires);
        mech.as_context_mech = as;
        mech.sas_context_mech = sas;

        CompoundSecMechList mech_list = new CompoundSecMechList(false,
                                                                new CompoundSecMech[]{mech});

        Any a = getOrb().create_any();
        CompoundSecMechListHelper.insert(a, mech_list);
        byte[] mech_data;
        try {
            mech_data = codec.encode_value(a);
        }
        catch (InvalidTypeForEncoding e) {
            MARSHAL me = new MARSHAL("cannot encode security descriptor", 0,
                                     CompletionStatus.COMPLETED_NO);
            me.initCause(e);
            throw me;
        }
        return new TaggedComponent(TAG_CSI_SEC_MECH_LIST.value, mech_data);
    }

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
