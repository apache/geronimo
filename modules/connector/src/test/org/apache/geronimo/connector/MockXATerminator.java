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

package org.apache.geronimo.connector;

import javax.resource.spi.XATerminator;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;

import org.apache.geronimo.transaction.XAServices;

/**
 * Dummy implementation of XATerminator interface for use in
 * {@link BootstrapContextTest}
 * @version $Rev$ $Date$
 */
public class MockXATerminator implements XAServices {

    private String id = null;

    /** Creates a new instance of MockWorkManager */
    public MockXATerminator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean equals(MockXATerminator xat) {
        if (!(xat instanceof MockXATerminator)) {
            return false;
        }

        return ((MockXATerminator) xat).getId() != null &&
                ((MockXATerminator) xat).getId().equals(getId());
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int prepare(Xid xid) throws XAException {
        return -1;
    }

    public Xid[] recover(int flag) throws XAException {
        return null;
    }

    public void rollback(Xid xid) throws XAException {
    }

    public void begin(Xid xid, long txTimeout) throws XAException, InvalidTransactionException, SystemException {
    }

    public void end(Xid xid) throws XAException, SystemException {
    }

}
