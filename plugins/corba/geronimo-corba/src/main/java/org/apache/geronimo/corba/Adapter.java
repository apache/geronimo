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
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.geronimo.corba.transaction.ServerTransactionPolicyFactory;
import org.apache.openejb.InterfaceType;
import org.apache.geronimo.openejb.EjbDeployment;

/**
 * @version $Revision: 497125 $ $Date: 2007-01-17 10:51:30 -0800 (Wed, 17 Jan 2007) $
 */
public abstract class Adapter implements RefGenerator {
    protected final EjbDeployment deployment;
    protected final POA homePOA;
    protected final ORB orb;
    protected final NamingContextExt initialContext;
    protected final byte[] home_id;
    protected final org.omg.CORBA.Object homeReference;
    protected final TSSLink tssLink; 

    protected Adapter(TSSLink tssLink, ORB orb, POA parentPOA, Policy securityPolicy) throws CORBAException {
        this.tssLink = tssLink; 
        this.deployment = tssLink.getDeployment();
        this.home_id = tssLink.getContainerId().getBytes();
        this.orb = orb;

        Any any = orb.create_any();
        any.insert_Value(tssLink.getHomeTxPolicyConfig());

        try {
            Policy[] policies = new Policy[]{
                securityPolicy,
                orb.create_policy(ServerTransactionPolicyFactory.POLICY_TYPE, any),
//                parentPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                parentPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                parentPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                parentPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                parentPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            // make sure we create this with the appropriate ORB-specific policies. 
            policies = tssLink.addPolicyOverrides(policies);
            homePOA = parentPOA.create_POA(tssLink.getContainerId(), parentPOA.the_POAManager(), policies);

            homePOA.the_POAManager().activate();

            StandardServant servant = new StandardServant(orb, InterfaceType.EJB_HOME, deployment);

            homePOA.activate_object_with_id(home_id, servant);
            homeReference = homePOA.servant_to_reference(servant);

            org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
            initialContext = NamingContextExtHelper.narrow(obj);
            String[] names = tssLink.getJndiNames();
            for (int i = 0; i < names.length; i++) {
                NameComponent[] nameComponent = initialContext.to_name(names[i]);
                NamingContext currentContext = initialContext;
                NameComponent[] nc = new NameComponent[1];
                int lastComponent = nameComponent.length - 1;
                for (int j = 0; j < lastComponent; ++j) {
                    nc[0] = nameComponent[j];
                    try {
                        currentContext = NamingContextHelper.narrow(currentContext.resolve(nc));
                    } catch (NotFound nf) {
                        currentContext = currentContext.bind_new_context(nc);
                    }
                }
                nc[0] = nameComponent[lastComponent];
                currentContext.rebind(nc, homeReference);
            }
        } catch (Exception e) {
            throw new CORBAException("Unable to activate EJB as CORBA object.", e);
        }

    }

    public EjbDeployment getDeployment() {
        return deployment;
    }

    public NamingContextExt getInitialContext() {
        return initialContext;
    }

    public org.omg.CORBA.Object getHomeReference() {
        return homeReference;
    }

    public ORB getOrb() {
        return orb;
    }

    public void stop() throws CORBAException {
        try {
            String[] names = tssLink.getJndiNames();
            for (int i = 0; i < names.length; i++) {
                NameComponent[] nameComponent = initialContext.to_name(names[i]);
                initialContext.unbind(nameComponent);

                for (int j = nameComponent.length - 1; 0 < j; --j) {
                    NameComponent[] nc = new NameComponent[j];
                    System.arraycopy(nameComponent, 0, nc, 0, j);
                    NamingContext currentContext = NamingContextHelper.narrow(initialContext.resolve(nc));
                    try {
                        currentContext.destroy();
                    } catch (NotEmpty ne) {
                        break;
                    }
                }
            }

            homePOA.deactivate_object(home_id);
            homePOA.destroy(true, true);
        } catch (Exception e) {
            throw new CORBAException("Error deactivating EJB as CORBA object", e);
        }
    }

    public org.omg.CORBA.Object genHomeReference() throws CORBAException {
        return this.getHomeReference();
    }
}
