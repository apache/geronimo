/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.naming;

import javax.naming.Reference;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.naming.deployment.RefAdapter;

/**
 * @version $Revision$ $Date$
 */
public interface ReferenceFactory {
    Reference buildConnectionFactoryReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException;

    Reference buildAdminObjectReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException;

    //TODO warning: this only works if there is only one kernel!
    Reference buildMessageDestinationReference(String linkName, Class iface) throws MalformedObjectNameException;

    Reference buildEjbReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException;

    Reference buildEjbLocalReference(RefAdapter refAdapter, Class iface) throws MalformedObjectNameException;
}
