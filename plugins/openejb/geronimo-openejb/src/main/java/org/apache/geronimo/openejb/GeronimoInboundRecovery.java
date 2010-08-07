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


package org.apache.geronimo.openejb;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.xa.XAResource;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.mdb.InboundRecovery;

/**
 * @version $Rev:$ $Date:$
 */
public class GeronimoInboundRecovery implements InboundRecovery {

    private final RecoverableTransactionManager transactionManager;

    public GeronimoInboundRecovery(RecoverableTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void recover(ResourceAdapter resourceAdapter, ActivationSpec activationSpec, String containerId) throws OpenEJBException {
        try {
            XAResource[] xaress = resourceAdapter.getXAResources(new ActivationSpec[] {activationSpec});
            if (xaress == null || xaress.length == 0) {
                return;
            }
            NamedXAResource xares = new WrapperNamedXAResource(xaress[0], containerId);
            transactionManager.recoverResourceManager(xares);
        } catch (ResourceException e) {
            throw new OpenEJBException("Could not recover resource manager", e);
        }
    }
}
