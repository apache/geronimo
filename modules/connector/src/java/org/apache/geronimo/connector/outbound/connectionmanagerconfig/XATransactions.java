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
import org.apache.geronimo.connector.outbound.XAResourceInsertionInterceptor;
import org.apache.geronimo.connector.outbound.TransactionEnlistingInterceptor;
import org.apache.geronimo.connector.outbound.TransactionCachingInterceptor;
import org.apache.geronimo.connector.outbound.ThreadLocalCachingConnectionInterceptor;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/24 19:10:34 $
 *
 * */
public class XATransactions extends TransactionSupport {
    private boolean useTransactionCaching;
    private boolean useThreadCaching;

    public XATransactions(boolean useTransactionCaching, boolean useThreadCaching) {
        this.useTransactionCaching = useTransactionCaching;
        this.useThreadCaching = useThreadCaching;
    }

    public boolean isUseTransactionCaching() {
        return useTransactionCaching;
    }

    public void setUseTransactionCaching(boolean useTransactionCaching) {
        this.useTransactionCaching = useTransactionCaching;
    }

    public boolean isUseThreadCaching() {
        return useThreadCaching;
    }

    public void setUseThreadCaching(boolean useThreadCaching) {
        this.useThreadCaching = useThreadCaching;
    }

    public ConnectionInterceptor addXAResourceInsertionInterceptor(ConnectionInterceptor stack) {
        return new XAResourceInsertionInterceptor(stack);
    }

    public ConnectionInterceptor addTransactionInterceptors(ConnectionInterceptor stack) {
        //experimental thread local caching
        if (isUseThreadCaching()) {
            //useMatching should be configurable
            stack = new ThreadLocalCachingConnectionInterceptor(stack, false);
        }
        stack = new TransactionEnlistingInterceptor(stack);
        if (isUseTransactionCaching()) {
            stack = new TransactionCachingInterceptor(stack);
        }
        return stack;
    }
}
