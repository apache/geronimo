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
package org.apache.geronimo.interop.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.geronimo.interop.adapter.Adapter;
import org.apache.geronimo.interop.adapter.AdapterManager;
import org.apache.geronimo.interop.naming.NameService;
import org.apache.geronimo.interop.rmi.iiop.ListenerInfo;
import org.apache.geronimo.interop.rmi.iiop.server.MessageHandler;


public class IIOPDaemon implements Runnable {
    protected String _host = "localhost";
    protected int _port = 9000;
    protected String _name = "IIOP";
    protected ServerSocket _ss = null;
    protected boolean _ready = false;

    public void setHost(String host) {
        _host = host;
    }

    public String getHost() {
        return _host;
    }

    public void setPort(int port) {
        _port = port;
    }

    public int getPort() {
        return _port;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setReady() {
        _ready = true;
    }

    public boolean isReady() {
        return _ready;
    }

    public ServerSocket getServerSocket() {
        if (_ss == null) {
            synchronized (this) {
                try {
                    InetSocketAddress isa = new InetSocketAddress(_host, _port);
                    _ss = new ServerSocket();
                    _ss.bind(isa);
                    setReady();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return _ss;
    }

    public void run() {
        ListenerInfo li = new ListenerInfo();
        li.protocol = 1;
        li.host = getHost();
        li.port = getPort();

        ServerSocket ss = getServerSocket();
        Socket s = null;
        System.out.println("[" + getName() + "-" + getHost() + ":" + getPort() + "] Accepting Connections...");
        while (isReady()) {
            try {
                s = ss.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }

            MessageHandler mh;
            mh = MessageHandler.getInstance(li, s);
            mh.start();
        }
    }

    public static void main(String args[]) {
        IIOPDaemon id = new IIOPDaemon();
        id.setHost("localhost");
        id.setPort(9000);

        Thread t = new Thread(id);
        t.setName(id.getName() + " Daemon");
        t.start();

        NameService ns = NameService.getInstance();
        AdapterManager am = AdapterManager.getInstance();

        //
        // NameService
        //
        Adapter a = new Adapter();
        a.setBindName("NameService");
        a.setRemoteClassName("org.apache.geronimo.interop.rmi.iiop.server.ServerNamingContext");
        a.setRemoteInterfaceName("org.apache.geronimo.interop.rmi.iiop.NameServiceOperations");
        a.setShared(true);
        a.addId("IDL:org.apache.geronimo.interop/rmi/iiop/NameService:1.0");
        a.addId("IDL:omg.org/CosNaming/NamingContext:1.0");
        a.addId("IDL:omg.org/CosNaming/NamingContextExt:1.0");
        a.addId("NameService"); // this gets passed in by the J2SE 1.4 ORB
        a.setClassLoader(id.getClass().getClassLoader());
        //a.generateSkels();
        //a.compileSkels();

        am.registerAdapter(a);
        ns.bindAdapter(a);

        //
        // Component
        //
        a = new Adapter();
        a.setBindName("mark.comps.Add");
        a.setRemoteClassName("mark.comps.AddImpl");
        a.setRemoteInterfaceName("mark.comps.Add");
        a.addId("RMI:mark.comps.Add:0000000000000000");
        a.setClassLoader(id.getClass().getClassLoader());

        am.registerAdapter(a);
        ns.bindAdapter(a);

        //
        // Component
        //
        a = new Adapter();
        a.setBindName("mark.comps.Add2");
        a.setRemoteClassName("mark.comps.Add2Impl");
        a.setRemoteInterfaceName("mark.comps.Add2");
        a.addId("RMI:mark.comps.Add2:0000000000000000");
        a.setClassLoader(id.getClass().getClassLoader());

        am.registerAdapter(a);
        ns.bindAdapter(a);
    }
}
