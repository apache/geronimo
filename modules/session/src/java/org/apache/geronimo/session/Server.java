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
package org.apache.geronimo.session;

/**
 * Represents a logical server which is either the local server or a remote
 * server.
 * 
 * @version $Revision: $
 */
public interface Server {

    /**
     * Returns the unique name of this server
     */
    String getName();

    /**
     * Returns the addresses on which you can communicate with the server which is
     * required for redirecting or proxying requests for remote sessions
     */
    String[] getAddresses(String protocol);

    /**
     * Configures the available addresses that can be used to connect to this server
     */
    void setAddresses(String string, String[] strings);

    /**
     * Is this the local in-JVM server or a remote server
     */
    public boolean isLocalServer();

}
