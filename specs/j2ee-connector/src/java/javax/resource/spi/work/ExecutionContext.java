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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.resource.spi.work;

import javax.resource.NotSupportedException;
import javax.transaction.xa.Xid;

/**
 *
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:29 $
 */
public class ExecutionContext {
    private Xid xid;
    private long timeout = -1;

    public ExecutionContext() {
    }

    public void setXid(Xid xid) {
        this.xid = xid;
    }

    public Xid getXid() {
        return xid;
    }

    public void setTransactionTimeout(long timeout) throws NotSupportedException {
        if (timeout<=0) {
            throw new NotSupportedException("Illegal timeout value");
        }
        this.timeout = timeout;
    }

    public long getTransactionTimeout() {
        return timeout;
    }
}