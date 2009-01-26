/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.kernel.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

public class GeronimoRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {

    private static final long serialVersionUID = 8238444722121747980L;
    
    private int connectionTimeout = -1;
    private int readTimeout = -1;

    public GeronimoRMIClientSocketFactory(int connectionTimeout, int readTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }
    
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.bind(null);
        socket.connect(new InetSocketAddress(host, port), (this.connectionTimeout > 0) ? this.connectionTimeout : 0);        
        if (this.readTimeout >= 0) {
            socket.setSoTimeout(this.readTimeout);
        }
        return socket;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + connectionTimeout;
        result = prime * result + readTimeout;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GeronimoRMIClientSocketFactory other = (GeronimoRMIClientSocketFactory) obj;
        if (connectionTimeout != other.connectionTimeout) {
            return false;
        }
        if (readTimeout != other.readTimeout) {
            return false;
        }
        return true;
    }

}
