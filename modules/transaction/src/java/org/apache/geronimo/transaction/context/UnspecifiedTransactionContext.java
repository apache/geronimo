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

import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

/**
 * @version $Rev$ $Date$
 */
class UnspecifiedTransactionContext extends AbstractTransactionContext {
    private boolean active = true;
    private boolean failed = false;

    public UnspecifiedTransactionContext() {
    }

    public boolean isInheritable() {
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean enlistResource(XAResource xaResource){
        throw new IllegalStateException("There is no transaction in progress.");
    }

    public boolean delistResource(XAResource xaResource, int flag) {
        throw new IllegalStateException("There is no transaction in progress.");
    }

    public void registerSynchronization(Synchronization synchronization) {
        throw new IllegalStateException("There is no transaction in progress.");
    }

    public boolean getRollbackOnly() {
        return failed;
    }

    public void setRollbackOnly() {
        this.failed = true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public boolean commit() {
        complete();
        return true;
    }

    public void rollback() {
        setRollbackOnly();
        complete();
    }

    private void complete() {
        try {
            if (!failed) {
                flushState();
            }
        } catch (Error e) {
            throw e;
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable e) {
            log.error("Unable to flush state, continuing", e);
        } finally {
            active = false;
            unassociateAll();
        }
    }
}
