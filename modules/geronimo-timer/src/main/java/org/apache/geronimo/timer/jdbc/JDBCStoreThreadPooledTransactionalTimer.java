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

package org.apache.geronimo.timer.jdbc;

import java.sql.SQLException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.TransactionalExecutorTaskFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 */
public class JDBCStoreThreadPooledTransactionalTimer extends ThreadPooledTimer {

    public JDBCStoreThreadPooledTransactionalTimer(int repeatCount,
            TransactionManager transactionManager,
            ConnectionFactorySource managedConnectionFactoryWrapper,
            Executor threadPool,
            Kernel kernel) throws SQLException {
        super(new TransactionalExecutorTaskFactory(transactionManager, repeatCount),
                new JDBCWorkerPersistence(kernel.getKernelName(), (DataSource)managedConnectionFactoryWrapper.$getResource(), false), threadPool, transactionManager);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(JDBCStoreThreadPooledTransactionalTimer.class);
        infoFactory.addInterface(PersistentTimer.class);

        infoFactory.addAttribute("repeatCount", int.class, true);
        infoFactory.addReference("TransactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoFactory.addReference("ManagedConnectionFactoryWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoFactory.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[] {"repeatCount", "TransactionManager", "ManagedConnectionFactoryWrapper", "ThreadPool", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
