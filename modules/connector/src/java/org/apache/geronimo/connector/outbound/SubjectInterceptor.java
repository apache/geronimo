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
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.security.ContextManager;

/**
 * SubjectInterceptor.java
 *
 *
 * Created: Mon Oct  6 14:31:56 2003
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:32 $
 */
public class SubjectInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final RealmBridge realmBridge;

    public SubjectInterceptor(
            final ConnectionInterceptor next,
            final RealmBridge realmBridge) {
        this.next = next;
        this.realmBridge = realmBridge;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        Subject currentSubject = null;
        try {
            currentSubject = realmBridge.mapSubject(ContextManager.getCurrentCaller());
        } catch (SecurityException e) {
            throw new ResourceException("Can not obtain Subject for login", e);
        } catch (LoginException e) {
            throw new ResourceException("Can not obtain Subject for login", e);
        }
        assert currentSubject != null;
        ManagedConnectionInfo originalManagedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        //No existing managed connection, get an appropriate one and return.
        if (originalManagedConnectionInfo.getManagedConnection() == null) {
            originalManagedConnectionInfo.setSubject(currentSubject);
            next.getConnection(connectionInfo);
        } else if (!currentSubject.equals(originalManagedConnectionInfo.getSubject())) {
            //existing managed connection, wrong subject: must re-associate.
            //make a ConnectionInfo to process removing the handle from the old mc
            ConnectionInfo returningConnectionInfo = new ConnectionInfo();
            returningConnectionInfo.setManagedConnectionInfo(originalManagedConnectionInfo);
            //This should decrement handle count, but not close the handle, when returnConnection is called
            //I'm not sure how to test/assure this.
            returningConnectionInfo.setConnectionHandle(connectionInfo.getConnectionHandle());

            //make a new ManagedConnectionInfo for the mc we will ask for
            ManagedConnectionInfo newManagedConnectionInfo =
                    new ManagedConnectionInfo(
                            originalManagedConnectionInfo.getManagedConnectionFactory(),
                            originalManagedConnectionInfo.getConnectionRequestInfo());
            newManagedConnectionInfo.setSubject(currentSubject);
            connectionInfo.setManagedConnectionInfo(newManagedConnectionInfo);
            next.getConnection(connectionInfo);
            //process the removal of the handle from the previous mc
            returnConnection(returningConnectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        }
        //otherwise, the current ManagedConnection matches the security info, we keep it.
    }

    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        next.returnConnection(connectionInfo, connectionReturnAction);
    }

}
