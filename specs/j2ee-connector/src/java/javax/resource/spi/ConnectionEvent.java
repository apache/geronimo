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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.resource.spi;

import java.util.EventObject;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class ConnectionEvent extends EventObject {
    public static final int CONNECTION_CLOSED = 1;
    public static final int LOCAL_TRANSACTION_STARTED = 2;
    public static final int LOCAL_TRANSACTION_COMMITTED = 3;
    public static final int LOCAL_TRANSACTION_ROLLEDBACK = 4;
    public static final int CONNECTION_ERROR_OCCURRED = 5;

    protected int id;

    private Exception exception;
    private Object connectionHandle;

    public ConnectionEvent(ManagedConnection source, int eid) {
        super(source);
        this.id = eid;
    }

    public ConnectionEvent(ManagedConnection source, int eid, Exception exception) {
        this(source, eid);
        this.exception = exception;
    }

    public Object getConnectionHandle() {
        return connectionHandle;
    }

    public void setConnectionHandle(Object connectionHandle) {
        this.connectionHandle = connectionHandle;
    }

    public Exception getException() {
        return exception;
    }

    public int getId() {
        return id;
    }
}