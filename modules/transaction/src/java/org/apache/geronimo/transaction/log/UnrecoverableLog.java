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

package org.apache.geronimo.transaction.log;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.LogException;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;

/**
 * A log sink that doesn't actually do anything.
 * Not recommended for production use as heuristic recovery will be needed if
 * the transaction coordinator dies.
 *
 * @version $Revision: 1.6 $ $Date: 2004/06/11 19:20:55 $
 */
public class UnrecoverableLog implements TransactionLog {
    public void begin(Xid xid) throws LogException {
    }

    public void prepare(Xid xid, List branches) throws LogException {
    }

    public void commit(Xid xid) throws LogException {
    }

    public void rollback(Xid xid) throws LogException {
    }

    public Map recover(XidFactory xidFactory) throws LogException {
        return new HashMap();
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
