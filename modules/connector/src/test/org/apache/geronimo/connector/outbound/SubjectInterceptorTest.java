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

import javax.security.auth.Subject;
import javax.resource.ResourceException;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/05/24 19:10:35 $
 *
 * */
public class SubjectInterceptorTest extends ConnectionInterceptorTestUtils {

    private SubjectInterceptor subjectInterceptor;

    protected void setUp() throws Exception {
        super.setUp();
        subjectInterceptor = new SubjectInterceptor(this, this);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        subjectInterceptor = null;
    }

    public void testGetConnection() throws Exception {
        subject = new Subject();
        ConnectionInfo connectionInfo = makeConnectionInfo();
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        subjectInterceptor.getConnection(connectionInfo);
        assertTrue("Expected call to next with same connectionInfo", connectionInfo == obtainedConnectionInfo);
        assertTrue("Expected the same managedConnectionInfo", managedConnectionInfo == connectionInfo.getManagedConnectionInfo());
        assertTrue("Expected supplied subject to be inserted", subject == managedConnectionInfo.getSubject());
    }

    public void testReturnConnection() throws Exception {
        ConnectionInfo connectionInfo = makeConnectionInfo();
        subjectInterceptor.returnConnection(connectionInfo, ConnectionReturnAction.RETURN_HANDLE);
        assertTrue("Expected call to next with same connectionInfo", connectionInfo == returnedConnectionInfo);
    }

    public void testEnterWithSameSubject() throws Exception {
        makeSubject("foo");
        ConnectionInfo connectionInfo = makeConnectionInfo();
        managedConnection = new TestPlainManagedConnection();
        subjectInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        obtainedConnectionInfo = null;
        subjectInterceptor.getConnection(connectionInfo);
        assertTrue("Expected connection asked for", obtainedConnectionInfo == connectionInfo);
        assertTrue("Expected no connection returned", returnedConnectionInfo == null);
    }

    public void testEnterWithChangedSubject() throws Exception {
        makeSubject("foo");
        ConnectionInfo connectionInfo = makeConnectionInfo();
        managedConnection = new TestPlainManagedConnection();
        subjectInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        obtainedConnectionInfo = null;
        makeSubject("bar");
        subjectInterceptor.getConnection(connectionInfo);
        //expect re-association
        assertTrue("Expected connection asked for", obtainedConnectionInfo != null);
        //connection is returned by SubjectInterceptor
        assertTrue("Expected connection returned", returnedConnectionInfo != null);
    }

    public void testApplicationManagedSecurity() throws Exception {
        makeSubject("foo");
        ConnectionInfo connectionInfo = makeConnectionInfo();
        connectionInfo.setApplicationManagedSecurity(true);
        ManagedConnectionInfo managedConnectionInfo = connectionInfo.getManagedConnectionInfo();
        managedConnection = new TestPlainManagedConnection();
        subjectInterceptor.getConnection(connectionInfo);
        //expect no subject set on mci
        assertTrue("Expected call to next with same connectionInfo", connectionInfo == obtainedConnectionInfo);
        assertTrue("Expected the same managedConnectionInfo", managedConnectionInfo == connectionInfo.getManagedConnectionInfo());
        assertTrue("Expected no subject to be inserted", null == managedConnectionInfo.getSubject());
    }

    public void testUnshareablePreventsReAssociation() throws Exception {
        makeSubject("foo");
        ConnectionInfo connectionInfo = makeConnectionInfo();
        connectionInfo.setUnshareable(true);
        managedConnection = new TestPlainManagedConnection();
        subjectInterceptor.getConnection(connectionInfo);
        //reset our test indicator
        obtainedConnectionInfo = null;
        makeSubject("bar");
        try {
            subjectInterceptor.getConnection(connectionInfo);
            fail("Reassociating should fail on an unshareable connection");
        } catch (ResourceException e) {
        }
    }

}
