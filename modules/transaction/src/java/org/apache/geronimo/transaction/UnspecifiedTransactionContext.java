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

import javax.transaction.Transaction;

import org.apache.geronimo.transaction.ConnectionReleaser;
import org.apache.geronimo.transaction.TransactionContext;


/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 */
public class UnspecifiedTransactionContext extends TransactionContext {
    public void begin() {
    }

    public void suspend() {
    }

    public void resume() {
    }

    public void commit() {
        try {
            flushState();
        } catch (Exception e) {
            log.error("Unable to flush state, continuing", e);
        }
    }

    public void rollback() {
    }

    //Geronimo connector framework support
    public void setManagedConnectionInfo(ConnectionReleaser key, Object info) {
    }

    public Object getManagedConnectionInfo(ConnectionReleaser key) {
        return null;
    }

    public boolean isActive() {
        return false;
    }

    public Transaction getTransaction() {
        return null;
    }
}
