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

package org.apache.geronimo.webdav;

import java.util.Collection;

/**
 * A DAVServer is an HTTP server providing WebDAV capabilities. It allows to
 * expose, edit and manage a set of repositories remotely via a WebDAV client.
 * <BR>
 * The WebDAV protocol could "potentially" be the preferred transport to
 * distribute a component as its base protocol, HTTP, can usually
 * traverse firewalls.
 * <BR>
 * It is a composition of connectors and repositories.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:20 $
 */
public interface DAVServer {
    /**
     * Gets the connectors of this server.
     *
     * @return Collection of Connector instances associated to this server.
     */
    public Collection getConnectors();

    /**
     * Gets the repositories of this server.
     *
     * @return Collection of DAVRepository instances associated to this server.
     */
    public Collection getRepositories();

}
