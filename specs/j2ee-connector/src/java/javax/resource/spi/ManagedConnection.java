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

import java.io.PrintWriter;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public interface ManagedConnection {
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException;

    public void destroy() throws ResourceException;

    public void cleanup() throws ResourceException;

    public void associateConnection(Object connection) throws ResourceException;

    public void addConnectionEventListener(ConnectionEventListener listener);

    public void removeConnectionEventListener(ConnectionEventListener listener);

    public XAResource getXAResource() throws ResourceException;

    public LocalTransaction getLocalTransaction() throws ResourceException;

    public ManagedConnectionMetaData getMetaData() throws ResourceException;

    public void setLogWriter(PrintWriter out) throws ResourceException;

    public PrintWriter getLogWriter() throws ResourceException;
}