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

package org.apache.geronimo.connector.outbound.connectionmanagerconfig;

import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.TransactionCachingInterceptor;
import org.apache.geronimo.connector.outbound.TransactionEnlistingInterceptor;
import org.apache.geronimo.connector.outbound.transactionlog.LogXAResourceInsertionInterceptor;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/30 19:03:36 $
 *
 * */
public class TransactionLog extends TransactionSupport
{
    public static final TransactionSupport INSTANCE = new TransactionLog();

    private TransactionLog() {
    }

    public ConnectionInterceptor addXAResourceInsertionInterceptor(ConnectionInterceptor stack) {
        return new LogXAResourceInsertionInterceptor(stack);
    }

    public ConnectionInterceptor addTransactionInterceptors(ConnectionInterceptor stack) {
        stack = new TransactionEnlistingInterceptor(stack);
        return new TransactionCachingInterceptor(stack);
    }
}
