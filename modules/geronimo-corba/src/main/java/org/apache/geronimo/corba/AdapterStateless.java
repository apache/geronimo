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
package org.apache.geronimo.corba;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.openejb.InterfaceType;
import org.apache.geronimo.corba.transaction.ServerTransactionPolicyFactory;

/**
 * @version $Revision: 497125 $ $Date: 2007-01-17 10:51:30 -0800 (Wed, 17 Jan 2007) $
 */
public final class AdapterStateless extends Adapter {
    private final POA poa;
    private final byte[] object_id;
    private final org.omg.CORBA.Object objectReference;

    public AdapterStateless(TSSLink tssLink, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        super(tssLink, orb, parentPOA, securityPolicy);
        Any any = orb.create_any();
        any.insert_Value(tssLink.getRemoteTxPolicyConfig());

        try {
            Policy[] policies = new Policy[]{
                securityPolicy,
                orb.create_policy(ServerTransactionPolicyFactory.POLICY_TYPE, any),
//                homePOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                homePOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                homePOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                homePOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                homePOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            // make sure we create this with the appropriate ORB-specific policies. 
            policies = tssLink.addPolicyOverrides(policies); 
            poa = homePOA.create_POA(tssLink.getContainerId(), homePOA.the_POAManager(), policies);

            poa.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, InterfaceType.EJB_OBJECT, tssLink.getDeployment());

            poa.activate_object_with_id(object_id = tssLink.getContainerId().getBytes(), servant);
            objectReference = poa.servant_to_reference(servant);
        } catch (Exception e) {
            throw new CORBAException("Unable to activate EJB "+ tssLink.getContainerId() +" as CORBA object", e);
        }
    }

    public void stop() throws CORBAException {
        try {
            poa.deactivate_object(object_id);
            poa.destroy(true, true);
            super.stop();
        } catch (ObjectNotActive e) {
            throw new CORBAException("Unable to activate EJB "+ tssLink.getContainerId() +" as CORBA object", e);
        } catch (WrongPolicy e) {
            throw new CORBAException("Unable to activate EJB "+ tssLink.getContainerId() +" as CORBA object", e);
        }
    }

    public org.omg.CORBA.Object genObjectReference(Object primaryKey) {
        return objectReference;
    }
}
