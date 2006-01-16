/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.session.local;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.session.Server;

import java.util.Map;

/**
 * The server instance which is actually in this local JVM
 * 
 * @version $Revision: $
 */
public class LocalServerImpl implements Server {
    private final String name;
    private final Map addressMap = new ConcurrentHashMap();

    public LocalServerImpl(String name) {
        this.name = name;
    }

    public boolean isLocalServer() {
        return true;
    }

    public String getName() {
        return name;
    }

    public String[] getAddresses(String protocol) {
        return (String[]) addressMap.get(protocol);
    }

    public void setAddresses(String protocol, String[] addresses) {
        addressMap.put(protocol, addresses);
    }
}
