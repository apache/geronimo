/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction;

import java.util.Collection;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.RecoveryImpl;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.apache.geronimo.transaction.manager.XidImporter;

/**
 * Class to set up the standard objects for a geronimo transaction manager.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/15 03:25:36 $
 *
 * */
public class GeronimoTransactionManager extends TransactionManagerProxy {

    public GeronimoTransactionManager(TransactionLog transactionLog, Collection resourceManagers) {
        super(getConstructorParams(transactionLog, (ReferenceCollection)resourceManagers));
    }

    private static TransactionManagerProxy.ConstructorParams getConstructorParams(TransactionLog transactionLog, ReferenceCollection resourceManagers) {
        TransactionManagerProxy.ConstructorParams params = new TransactionManagerProxy.ConstructorParams();
        XidFactory xidFactory = new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes());
        if (transactionLog == null) {
            transactionLog = new UnrecoverableLog();
        }
        TransactionManager delegate = new TransactionManagerImpl(transactionLog, xidFactory);
        Recovery recovery = new RecoveryImpl(transactionLog, xidFactory);
        params.delegate = delegate;
        params.xidImporter = (XidImporter) delegate;
        params.recovery = recovery;
        params.resourceManagers = resourceManagers;
        return params;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GeronimoTransactionManager.class);

        infoFactory.addReference("TransactionLog", TransactionLog.class);
        infoFactory.addReference("ResourceManagers", ResourceManager.class);

        infoFactory.addOperation("setTransactionTimeout", new Class[]{int.class});
        infoFactory.addOperation("begin");
        infoFactory.addOperation("getStatus");
        infoFactory.addOperation("getTransaction");
        infoFactory.addOperation("suspend");
        infoFactory.addOperation("resume", new Class[]{Transaction.class});
        infoFactory.addOperation("commit");
        infoFactory.addOperation("rollback");
        infoFactory.addOperation("setRollbackOnly");

        infoFactory.setConstructor(new String[]{"TransactionLog", "ResourceManagers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}