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

package org.apache.geronimo.transaction.log;

import java.io.IOException;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.TransactionLog;

/**
 * A log sink that doesn't actually do anything.
 * Not recommended for production use as heuristic recovery will be needed if
 * the transaction coordinator dies.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 */
public class UnrecoverableLog implements TransactionLog {
    public void begin(Xid xid) throws IOException {
    }

    public void prepare(Xid xid) throws IOException {
    }

    public void commit(Xid xid) throws IOException {
    }

    public void rollback(Xid xid) throws IOException {
    }
}
