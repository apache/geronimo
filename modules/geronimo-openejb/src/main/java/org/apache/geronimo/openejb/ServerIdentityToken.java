/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb;

import java.net.URI;

import org.apache.geronimo.security.SubjectId;

/**
 * @version $Rev$ $Date$
 */
public class ServerIdentityToken {
    private final URI server;
    private final SubjectId id;


    public ServerIdentityToken(URI server, SubjectId id) {
        this.server = server;
        this.id = id;
    }


    public URI getServer() {
        return server;
    }

    public SubjectId getId() {
        return id;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerIdentityToken that = (ServerIdentityToken) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (server != null ? !server.equals(that.server) : that.server != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (server != null ? server.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
