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

package org.apache.geronimo.connector.outbound;

/**
 * ConnectionInfo.java
 *
 *
 * Created: Thu Sep 25 14:29:07 2003
 *
 * @version 1.0
 */
public class ConnectionInfo {

    private ManagedConnectionInfo mci;
    private Object connection;

    public ConnectionInfo() {
    } // ConnectionInfo constructor

    public ConnectionInfo(ManagedConnectionInfo mci) {
        this.mci = mci;
    }

    /**
     * Get the Mci value.
     * @return the Mci value.
     */
    public ManagedConnectionInfo getManagedConnectionInfo() {
        return mci;
    }

    /**
     * Set the Mci value.
     * @param mci The new Mci value.
     */
    public void setManagedConnectionInfo(ManagedConnectionInfo mci) {
        this.mci = mci;
    }

    /**
     * Get the Connection value.
     * @return the Connection value.
     */
    public Object getConnectionHandle() {
        return connection;
    }

    /**
     * Set the Connection value.
     * @param newConnection The new Connection value.
     */
    public void setConnectionHandle(Object connection) {
        this.connection = connection;
    }

} // ConnectionInfo
