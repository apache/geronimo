/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.farm.service;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev$ $Date$
 */
public class NodeService implements Serializable {

    private static final long serialVersionUID = 8329271824511964537L;
    private final URI uri;
    private final String uriString;

    public URI getUri() {
        return uri;
    }

    public String getUriString() {
        return uriString;
    }

    public NodeService(URI uri) {
        this.uri = uri;
        this.uriString = uri.toString();
    }

    public NodeService(String uriString) throws URISyntaxException {
        this(new URI(uriString));
    }
}
