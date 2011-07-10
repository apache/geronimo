/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.corba;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;

import org.apache.geronimo.corba.util.Util;
import org.apache.geronimo.corba.transaction.ServerTransactionPolicyConfig;
import org.apache.geronimo.corba.transaction.OperationTxPolicy;
import org.apache.geronimo.corba.transaction.MappedServerTransactionPolicyConfig;
import org.apache.geronimo.corba.transaction.nodistributedtransactions.NoDTxServerTransactionPolicies;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.transaction.TransactionType;

import org.omg.CORBA.Policy;

/**
 * @version $Rev: 497125 $ $Date: 2007-01-17 10:51:30 -0800 (Wed, 17 Jan 2007) $
 */
public class TSSLink implements GBeanLifecycle {
    private final TSSBean tssBean;
    private final EjbDeployment ejb;
    private final String[] jndiNames;

    public TSSLink() {
        tssBean = null;
        ejb = null;
        jndiNames = null;
    }

    public TSSLink(String[] jndiNames, TSSBean tssBean, EjbDeployment ejb) {
        if (tssBean == null) {
            throw new NullPointerException("No TSSBean supplied");
        }
        if (ejb == null) {
            throw new NullPointerException("No ejb supplied");
        }
        this.jndiNames = jndiNames;
        this.tssBean = tssBean;
        this.ejb = ejb;
    }

    public void doStart() throws Exception {
        if (tssBean != null) {
            tssBean.registerContainer(this);
        }
    }

    public void doStop() throws Exception {
        destroy();
    }

    public void doFail() {
        destroy();
    }

    protected void destroy() {
        if (tssBean != null) {
            tssBean.unregisterContainer(this);
        }
    }

    public EjbDeployment getDeployment() {
        return ejb;
    }

    public String getContainerId() {
        return ejb.getDeploymentId();
    }

    public String[] getJndiNames() {
        return jndiNames;
    }

    /**
     * CORBA home transaction import policy configuration
     * @return home transaction import policy
     */
    public Serializable getHomeTxPolicyConfig() {
        if (ejb.getHomeInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(ejb.getHomeInterface(), InterfaceType.EJB_HOME);
        return policy;
    }

    /**
     * CORBA remote transaction import policy configuration
     * @return remote transaction import policy
     */
    public Serializable getRemoteTxPolicyConfig() {
        if (ejb.getRemoteInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(ejb.getRemoteInterface(), InterfaceType.BUSINESS_REMOTE);
        return policy;
    }

    private Serializable buildTransactionImportPolicy(Class intf, InterfaceType interfaceType) {

        Map policies = new HashMap();

        Map methodToOperation = Util.mapMethodToOperation(intf);
        for (Iterator iterator = methodToOperation.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method method = (Method) entry.getKey();
            String operation = (String) entry.getValue();

            if (!ejb.isBeanManagedTransaction()) {
            	TransactionType transactionType = ejb.getTransactionType(method, interfaceType);
                OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getContainerTransactionPolicy(transactionType);
                policies.put(operation, operationTxPolicy);
            } else {
                OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getBeanTransactionPolicy();
                policies.put(operation, operationTxPolicy);
            }
        }
        ServerTransactionPolicyConfig serverTransactionPolicyConfig = new MappedServerTransactionPolicyConfig(policies);

        return serverTransactionPolicyConfig;
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
        return tssBean.addPolicyOverrides(policies);
    }
}
