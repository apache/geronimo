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

package org.apache.geronimo.transaction.manager;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/11 19:20:55 $
 *
 * */
public class MockLog implements TransactionLog {

    final Map prepared = new HashMap();
    final List committed = new ArrayList();
    final List rolledBack = new ArrayList();

    public void begin(Xid xid) throws LogException {
    }

    public void prepare(Xid xid, List branches) throws LogException {
        prepared.put(xid, new HashSet(branches));
    }

    public void commit(Xid xid) throws LogException {
        committed.add(xid);
    }

    public void rollback(Xid xid) throws LogException {
        rolledBack.add(xid);
    }

    public Map recover(XidFactory xidFactory) throws LogException {
        Map copy = new HashMap(prepared);
        for (Iterator iterator = committed.iterator(); iterator.hasNext();) {
            copy.remove(iterator.next());
        }
        for (Iterator iterator = rolledBack.iterator(); iterator.hasNext();) {
            copy.remove(iterator.next());
        }
        return copy;
    }

    public String getXMLStats() {
        return null;
    }

    public int getAverageForceTime() {
        return 0;
    }

    public int getAverageBytesPerForce() {
        return 0;
    }
}
