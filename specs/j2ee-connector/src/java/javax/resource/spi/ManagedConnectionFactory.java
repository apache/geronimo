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
import java.io.Serializable;
import java.util.Set;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

/**
 *
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/04/07 06:54:57 $
 */
public interface ManagedConnectionFactory extends Serializable {

    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException;

    public Object createConnectionFactory() throws ResourceException;

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException;

    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException;

    public void setLogWriter(PrintWriter out) throws ResourceException;

    public PrintWriter getLogWriter() throws ResourceException;

    public int hashCode();

    public boolean equals(Object other);
}