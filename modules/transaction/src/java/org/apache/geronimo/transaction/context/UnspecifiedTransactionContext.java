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

import javax.transaction.Transaction;

import org.apache.geronimo.transaction.ConnectionReleaser;


/**
 * @version $Rev$ $Date$
 */
public class UnspecifiedTransactionContext extends TransactionContext {
    public void begin() {
    }

    public void suspend() {
    }

    public void resume() {
    }

    public boolean commit() {
        try {
            flushState();
        } catch (Error e) {
            throw e;
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable e) {
            log.error("Unable to flush state, continuing", e);
        }
        return true;
    }

    public void rollback() {
    }

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
