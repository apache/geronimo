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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException; 
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.apache.geronimo.corba.security.ServerPolicy;
import org.apache.geronimo.corba.security.ServerPolicyFactory;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.security.config.tss.TSSNULLTransportConfig;

import java.util.HashMap;
import java.util.Map;

 /**
  * A TSSBean represents a transport-level security profile for exported EJB objects.  An
  * exported object is attached to a TSSBean-created named POA.  The TSSBean POA
  * is created in the context of the ORB controlled by a CORBABean instance.
  * The parent CORBABean controls the transport-level security of the host connection and
  * defines the endpoint connnection for the object (host and listener port).
  * TSSBean may then define additional characteristics that
  * get encoded in the IOR of the connection.
  * @version $Revision: 497125 $ $Date: 2007-01-17 10:51:30 -0800 (Wed, 17 Jan 2007) $
  */
public class TSSBean implements GBeanLifecycle {

    private final Logger log = LoggerFactory.getLogger(TSSBean.class);

    private final ClassLoader classLoader;
    private final String POAName;
    private final CORBABean server;
    private POA localPOA;
    private NamingContextExt initialContext;
    private TSSConfig tssConfig;
    private final Map adapters = new HashMap();
    private Policy securityPolicy;

    /**
     * gbean endpoint constructor
     */
    public TSSBean() {
        classLoader = null;
        POAName = null;
        server = null;
    }

    public TSSBean(ClassLoader classLoader, String POAName, CORBABean server) {
        this.classLoader = classLoader;
        this.POAName = POAName;
        this.server = server;
    }

    public CORBABean getServer() {
        return server;
    }

    public String getPOAName() {
        return POAName;
    }

    public TSSConfig getTssConfig() {
        return tssConfig;
    }

    public void setTssConfig(TSSConfig tssConfig) {
        if (tssConfig == null) tssConfig = new TSSConfig();
        this.tssConfig = tssConfig;
    }

    /**
     * TODO: Security policy really shouldn't be inserted if there is not CSI
     * config to put into it.
     *
     * @throws Exception
     */
    public void doStart() throws Exception {
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            ORB orb = server.getORB();
            POA rootPOA = server.getRootPOA();

            Any any = orb.create_any();
            any.insert_Value(new ServerPolicy.Config(createCSIv2Config(), classLoader));

            securityPolicy = orb.create_policy(ServerPolicyFactory.POLICY_TYPE, any);
            Policy[] policies = new Policy[]{
                    securityPolicy,
                    rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
                    rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                    rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
                    rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                    rootPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
            };
            // there may be ORB-specific policy overrides required. 
            policies = server.addPolicyOverrides(policies); 
            
            localPOA = rootPOA.create_POA(POAName, rootPOA.the_POAManager(), policies);

            localPOA.the_POAManager().activate();

            org.omg.CORBA.Object obj = server.getORB().resolve_initial_references("NameService");
            // NB:  This is initial context is never used by the TSSBean, but we request it here
            // to verify that the server ORB is correctly configured and our target server is accessible.
            initialContext = NamingContextExtHelper.narrow(obj);
        } catch (NoSuchMethodError e) {
            log.error("Incorrect level of org.omg.CORBA classes found.\nLikely cause is an incorrect java.endorsed.dirs configuration"); 
            throw new InvalidConfigurationException("CORBA usage requires Yoko CORBA spec classes in java.endorsed.dirs classpath", e); 
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.debug("Started CORBA Target Security Service in POA " + POAName);
    }

    public void doStop() throws Exception {
        if (localPOA != null) {
            // make sure this POA is destroyed so the bean can be potentially restarted.
            // NOTE:  we do NOT deactivate() the poa manager, as that will take down any
            // other POAs attached to the same manager.  Just destroying this POA is sufficient.
            localPOA.destroy(true, false);
            localPOA = null;
        }
        log.debug("Stopped CORBA Target Security Service in POA " + POAName);
    }

    public void doFail() {
        log.warn("Failed CORBA Target Security Service in POA " + POAName);
    }

    private TSSConfig createCSIv2Config() {
        if (tssConfig == null) return null;
        if (tssConfig.isInherit()) return server.getTssConfig();

        TSSConfig config = new TSSConfig();

        if (server.getTssConfig() != null) {
            config.setTransport_mech(server.getTssConfig().getTransport_mech());
        } else {
            config.setTransport_mech(new TSSNULLTransportConfig());
        }

        config.getMechListConfig().setStateful(tssConfig.getMechListConfig().isStateful());
        for (int i = 0; i < tssConfig.getMechListConfig().size(); i++) {
            config.getMechListConfig().add(tssConfig.getMechListConfig().mechAt(i));
        }

        return config;
    }

    public void registerContainer(TSSLink tssLink) throws CORBAException {
        AdapterWrapper adapterWrapper = new AdapterWrapper(tssLink);

        adapterWrapper.start(server.getORB(), localPOA, securityPolicy);
        adapters.put(tssLink.getContainerId(), adapterWrapper);

        log.debug(POAName + " - Linked container " + tssLink.getContainerId());
    }

    public void unregisterContainer(TSSLink tssLink) {
        AdapterWrapper adapterWrapper = (AdapterWrapper) adapters.remove(tssLink.getContainerId());
        if (adapterWrapper != null) {
            try {
                adapterWrapper.stop();
                log.debug(POAName + " - Unlinked container " + tssLink.getContainerId());
            } catch (CORBAException e) {
                log.error(POAName + " - Error unlinking container " + tssLink.getContainerId(), e);
            }
        }
    }
    
    /**
     * Add the policy overrides (if any) to the list 
     * of policies used to create a POA instance.
     * 
     * @param policies The base set of policies.
     * 
     * @return A new Policy array with the overrides added.  Returns
     *         the same array if no overrides are required.
     */
    public Policy[] addPolicyOverrides(Policy[] policies) {
        return server.addPolicyOverrides(policies); 
    }
}
