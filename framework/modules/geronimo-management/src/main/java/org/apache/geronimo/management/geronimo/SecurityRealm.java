/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.management.geronimo;

/**
 * A security realm in the server.
 *
 * todo: I'm not yet sure what should reasonably go in here -- it's not clear what's necessary
 * for a portable security layer and what's Geronimo-specific.  Currently the meaty extension
 * interface is in o.a.g.security
 *
 * @version $Rev$ $Date$
 */
public interface SecurityRealm {
    /**
     * The name of the realm, which must be unique across all realms in the
     * server.
     */
    public String getRealmName();

}
