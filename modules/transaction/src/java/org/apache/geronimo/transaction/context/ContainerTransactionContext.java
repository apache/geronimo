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

package org.apache.geronimo.transaction.context;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.geronimo.transaction.ExtendedTransactionManager;

/**
 * @version $Rev$ $Date$
 */
class ContainerTransactionContext extends InheritableTransactionContext {
    ContainerTransactionContext(ExtendedTransactionManager txnManager) throws SystemException, NotSupportedException {
        super(txnManager);
        begin(0);
    }

    ContainerTransactionContext(ExtendedTransactionManager txnManager, Transaction transaction) {
        super(txnManager, transaction);
    }
}
