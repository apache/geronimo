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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;

/**
 * XAResourceInsertionInterceptor.java
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:32 $
 */
public class XAResourceInsertionInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;

    public XAResourceInsertionInterceptor(final ConnectionInterceptor next) {
        this.next = next;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        next.getConnection(connectionInfo);
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        mci.setXAResource(mci.getManagedConnection().getXAResource());
    }

    public void returnConnection(ConnectionInfo connectionInfo, ConnectionReturnAction connectionReturnAction) {
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

}
