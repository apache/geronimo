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

package org.apache.geronimo.transaction.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/11 19:20:55 $
 */
public class MockResourceManager implements ResourceManager {
    private boolean willCommit;
    private Map xids = new HashMap();

    private NamedXAResource resources;
    private NamedXAResource returnedResources;

    public MockResourceManager(boolean willCommit) {
        this.willCommit = willCommit;
    }

    public MockResource getResource(String xaResourceName) {
        MockResource mockResource =  new MockResource(this, xaResourceName);
        resources = mockResource;
        return mockResource;
    }

    public void join(Xid xid, XAResource xaRes) throws XAException {
        Set resSet = (Set) xids.get(xid);
        if (resSet == null) {
            throw new XAException(XAException.XAER_NOTA);
        }
        resSet.add(xaRes);
    }

    public void newTx(Xid xid, XAResource xaRes) throws XAException {
        if (xids.containsKey(xid)) {
            throw new XAException(XAException.XAER_DUPID);
        }
        Set resSet = new HashSet();
        resSet.add(xaRes);
        xids.put(xid, resSet);
    }

    public void forget(Xid xid, XAResource xaRes) throws XAException {
        if (xids.remove(xid) == null) {
            throw new XAException(XAException.XAER_NOTA);
        }
    }

    public NamedXAResource getRecoveryXAResources() throws SystemException {
        return resources;
    }

    public void returnResource(NamedXAResource xaResource) {
        returnedResources = xaResource;
    }

    public boolean areAllResourcesReturned() {
        return returnedResources != null && returnedResources == resources;
    }
}
