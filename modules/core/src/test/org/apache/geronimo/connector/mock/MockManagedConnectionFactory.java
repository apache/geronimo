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

package org.apache.geronimo.connector.mock;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintWriter;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/23 17:34:34 $
 *
 * */
public class MockManagedConnectionFactory implements ManagedConnectionFactory {

    private MockResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    private final Set managedConnections = new HashSet();

    private boolean reauthentication;

    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        assert this.resourceAdapter == null: "Setting ResourceAdapter twice";
        assert resourceAdapter != null: "trying to set resourceAdapter to null";
        this.resourceAdapter = (MockResourceAdapter)resourceAdapter;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return new MockConnectionFactory(this, connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return null;
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        MockManagedConnection managedConnection = new MockManagedConnection(this, subject, (MockConnectionRequestInfo)connectionRequestInfo);
        managedConnections.add(managedConnection);
        return managedConnection;
    }

    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (reauthentication) {
            for (Iterator iterator = connectionSet.iterator(); iterator.hasNext();) {
                ManagedConnection managedConnection = (ManagedConnection) iterator.next();
                if (managedConnections.contains(managedConnection)) {
                    return managedConnection;
                }
            }
        } else {
            for (Iterator iterator = connectionSet.iterator(); iterator.hasNext();) {
                ManagedConnection managedConnection = (ManagedConnection) iterator.next();
                if (managedConnections.contains(managedConnection)) {
                    MockManagedConnection mockManagedConnection = (MockManagedConnection)managedConnection;
                    if ((subject == null? mockManagedConnection.getSubject() == null: subject.equals(mockManagedConnection.getSubject())
                    && (cxRequestInfo == null? mockManagedConnection.getConnectionRequestInfo() == null: cxRequestInfo.equals(mockManagedConnection.getConnectionRequestInfo())))) {
                        return mockManagedConnection;
                    }
                }
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter logWriter) throws ResourceException {
        this.logWriter = logWriter;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    public boolean isReauthentication() {
        return reauthentication;
    }

    public void setReauthentication(boolean reauthentication) {
        this.reauthentication = reauthentication;
    }

    public Set getManagedConnections() {
        return managedConnections;
    }
}
