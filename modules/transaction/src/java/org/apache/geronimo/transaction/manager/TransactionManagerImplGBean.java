/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction.manager;

import org.apache.geronimo.gbean.*;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.ExtendedTransactionManager;

import javax.transaction.xa.XAException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of a transaction manager.
 *
 * @version $Rev$ $Date$
 */
public class TransactionManagerImplGBean extends TransactionManagerImpl {

    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public TransactionManagerImplGBean(int defaultTransactionTimeoutSeconds, XidFactory xidFactory, TransactionLog transactionLog, Collection resourceManagers) throws XAException {
        super(defaultTransactionTimeoutSeconds, xidFactory, transactionLog, resourceManagers);
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
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TransactionManagerImplGBean.class, NameFactory.TRANSACTION_MANAGER);

        infoBuilder.addAttribute("defaultTransactionTimeoutSeconds", int.class, true);
        infoBuilder.addReference("XidFactory", XidFactory.class, NameFactory.XID_FACTORY);
        infoBuilder.addReference("TransactionLog", TransactionLog.class, NameFactory.TRANSACTION_LOG);
        infoBuilder.addReference("ResourceManagers", ResourceManager.class);//two kinds of things, so specify the type in each pattern.

        infoBuilder.addInterface(ExtendedTransactionManager.class);
        infoBuilder.addInterface(XidImporter.class);

        infoBuilder.setConstructor(new String[]{
                "defaultTransactionTimeoutSeconds",
                "XidFactory",
                "TransactionLog",
                "ResourceManagers"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
