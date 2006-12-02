/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.transaction.jta11;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import javax.transaction.xa.XAException;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;
import org.apache.geronimo.transaction.jta11.GeronimoTransactionManagerJTA11;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoTransactionManagerJTA11GBean extends GeronimoTransactionManagerJTA11 {
    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public GeronimoTransactionManagerJTA11GBean(int defaultTransactionTimeoutSeconds, XidFactory xidFactory, TransactionLog transactionLog, Collection resourceManagers) throws XAException {
        super(defaultTransactionTimeoutSeconds == 0 ? DEFAULT_TIMEOUT : defaultTransactionTimeoutSeconds,
                xidFactory,
                transactionLog,
                resourceManagers);
    }


    /**
     * We can track as resources are added into the geronimo kernel.
     *
     * @param resourceManagers
     * @return the original list of resources.
     */
    protected List watchResourceManagers(Collection resourceManagers) {
        if( resourceManagers instanceof ReferenceCollection ) {
            List copy;
            synchronized (resourceManagers) {
                copy = new ArrayList(resourceManagers);
                    ((ReferenceCollection)resourceManagers).addReferenceCollectionListener(new ReferenceCollectionListener() {
                    public void memberAdded(ReferenceCollectionEvent event) {
                        ResourceManager resourceManager = (ResourceManager) event.getMember();
                        recoverResourceManager(resourceManager);
                    }

                    public void memberRemoved(ReferenceCollectionEvent event) {
                    }

                });
            }
            return copy;
        } else {
            return super.watchResourceManagers(resourceManagers);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(GeronimoTransactionManagerJTA11GBean.class,
                TransactionManagerImplGBean.GBEAN_INFO,
                NameFactory.TRANSACTION_MANAGER);
        infoBuilder.addInterface(TransactionSynchronizationRegistry.class);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
