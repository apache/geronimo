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

import javax.security.auth.Subject;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/10 07:48:12 $
 *
 * */
public class SubjectInterceptorTest extends ConnectionManagerTestUtils {

    private SubjectInterceptor subjectInterceptor;

    protected void setUp() throws Exception {
        super.setUp();
        subjectInterceptor = new SubjectInterceptor(this, this);
    }

    protected  void tearDown() throws Exception {
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
        assertTrue("Expected connection asked for", obtainedConnectionInfo == null);
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

}
