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

package org.apache.geronimo.timer;

import javax.transaction.TransactionManager;

import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.timer.TransactionalExecutorTaskFactory;
import org.apache.geronimo.timer.AbstractThreadPooledTimerTest;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:57 $
 *
 * */
public class TransactionalThreadPooledTimerTest extends AbstractThreadPooledTimerTest {

    protected void setUp() throws Exception {
        TransactionManager transactionManager = new TransactionManagerImpl();
        transactionContextManager = new TransactionContextManager(transactionManager);
        executableWorkFactory = new TransactionalExecutorTaskFactory(transactionContextManager, 1);
        super.setUp();
    }
}
