/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2004/01/23 06:47:05 $
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
