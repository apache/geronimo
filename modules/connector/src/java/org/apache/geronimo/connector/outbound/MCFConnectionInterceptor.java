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
import javax.resource.spi.ManagedConnection;

/**
 * MCFConnectionInterceptor.java
 *
 *
 * @version $Rev$ $Date$
 */
public class MCFConnectionInterceptor implements ConnectionInterceptor {

    private ConnectionInterceptor stack;

    public MCFConnectionInterceptor() {
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (mci.getManagedConnection() != null) {
            return;
        }
        ManagedConnection mc =
                mci.getManagedConnectionFactory().createManagedConnection(
                        mci.getSubject(),
                        mci.getConnectionRequestInfo());
        mci.setManagedConnection(mc);
        GeronimoConnectionEventListener listener = new GeronimoConnectionEventListener(stack, mci);
        mci.setConnectionEventListener(listener);
        mc.addConnectionEventListener(listener);
    }

    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        ManagedConnection mc = mci.getManagedConnection();
        try {
            mc.destroy();
        } catch (ResourceException e) {
            //log and forget
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            //log and forget
        }
    }

    public void setStack(ConnectionInterceptor stack) {
        this.stack = stack;
    }

}
