/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.server;

import java.net.InetAddress;
import java.net.Socket;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.properties.SystemProperties;
import org.apache.geronimo.interop.rmi.iiop.ListenerInfo;
import org.apache.geronimo.interop.rmi.iiop.Protocol;


public class SocketListener extends Thread {
    public static SocketListener getInstance() {
        return new SocketListener();
    }

    // private data

    private String _name;

    private String _host;

    private int _port;

    private int _listenBacklog;

    private java.net.ServerSocket _listener;

    // internal methods

    protected void init() {
        _host = "localhost";
        _port = 2000;
        _listenBacklog = 10;
        setDaemon(true);
    }

    // public methods

    public void setHost(String host) {
        _host = host;
    }

    public void setPort(int port) {
        _port = port;
    }

    public void setListenBacklog(int backlog) {
        _listenBacklog = backlog;
    }

    public void run() {
        String iiopURL = "iiop://" + _host + ":" + _port;
        ListenerInfo listenerInfo = new ListenerInfo();
        listenerInfo.protocol = Protocol.IIOP; // TODO: other protocols (IIOPS etc.)
        listenerInfo.host = _host;
        listenerInfo.port = _port;
        try {
            InetAddress addr = InetAddress.getByName(_host);
            _listener = new java.net.ServerSocket(_port, _listenBacklog, addr);
        } catch (Exception ex) {
            System.out.println("SocketListener: Error creating server socket.");
            ex.printStackTrace();
            try {
                Socket socket = new Socket(_host, _port);
                socket.close();
                System.out.println("SocketListener: Error server already running: " + iiopURL);
                ex.printStackTrace();
            } catch (Exception ignore) {
            }
            return;
        }
        new CheckConnect().start();
        for (; ;) {
            java.net.Socket socket;
            try {
                socket = _listener.accept();
            } catch (Exception ex) {
                throw new SystemException(ex); // TODO: log error message
            }
            MessageHandler.getInstance(listenerInfo, socket).start();
        }
    }

    private class CheckConnect extends Thread {
        public void run() {
            try {
                Socket socket = new Socket(_host, _port);
                socket.close();
                if (!SystemProperties.quiet()) {
                    System.out.println(formatAcceptingIiopConnections());
                }
            } catch (Exception ex) {
                warnConnectFailed(_host, _port);
            }
        }
    }

    // format methods

    protected String formatAcceptingIiopConnections() {
        String msg = "SocketListener.formatAcceptingIiopConnection(): ";
        return msg;
    }

    // log methods

    protected void warnConnectFailed(String host, int port) {
        System.out.println("SocketListener.warnConnectFailed(): host = " + host + ", port = " + port);
    }
}
