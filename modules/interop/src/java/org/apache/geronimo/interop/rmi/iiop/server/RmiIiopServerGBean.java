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

import org.apache.geronimo.gbean.*;
import org.apache.geronimo.interop.adapter.AdapterManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.openejb.server.SocketService;
//import org.openejb.server.ServerService;
//import org.openejb.server.ServiceException;

import java.util.*;
import java.net.Socket;
import java.io.IOException;

//public class RmiIiopServerGBean implements ServerService {
public class RmiIiopServerGBean {

    private final Log log = LogFactory.getLog(RmiIiopServerGBean.class);

    private MessageHandler          msgHandler;
    private ArrayList               args = new ArrayList();
    private Properties              props = new Properties();
    private boolean                 simpleIDL = false;
    private boolean                 writeSystemExceptionStackTrace = false;
    private AdapterManager          adapterManager;

    public RmiIiopServerGBean(AdapterManager adapterManager, ArrayList args, Properties props,
                              boolean simpleIDL, boolean writeSystemExceptionStackTrace ) {
        this.adapterManager = adapterManager;
        this.args = args;
        this.props = props;
        this.simpleIDL = simpleIDL;
        this.writeSystemExceptionStackTrace = writeSystemExceptionStackTrace;
        this.msgHandler = new MessageHandler( this.adapterManager, this.simpleIDL,
                                              this.writeSystemExceptionStackTrace );

    }

    public RmiIiopServerGBean() throws Exception {
        this.adapterManager = null;
        this.args = null;
        this.props = null;
        this.simpleIDL = false;
        this.writeSystemExceptionStackTrace = false;
        this.msgHandler = null;
    }

    public void service(Socket socket) throws Exception {
        log.debug( "RmiIiopServerGBean.service(): socket = " + socket );
        msgHandler.service(socket);
    }

    public void init(Properties properties) throws Exception
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws Exception {
        log.debug( "RmiIiopServerGBean.start(): " );
    }

    public void stop() throws Exception {
        log.debug( "RmiIiopServerGBean.stop(): " );
    }

    public String getName() {
        return "rmiiiopd";
    }

    /*
    public void setArgs( ArrayList args )
    {
        this.args = args;
    }

    public ArrayList getArgs() {
        return args;
    }

    public void setProps( Properties props )
    {
        this.props = props;
    }

    public Properties getProps() {
        return props;
    }
    */

    /*
     * The following methods are from ServerService...
     * getPort() and getIP() ... Do we need them??
     */

    private int port;
    private String ip;

    public void setPort( int port )
    {
        log.debug( "RmiIiopServerGBean.setPort(): port = " + port );
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setIP( String ip )
    {
        log.debug( "RmiIiopServerGBean.setIP(): ip = " + ip );
        this.ip = ip;
    }

    public String getIP() {
        return "";
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(RmiIiopServerGBean.class);

        //infoFactory.addInterface(SocketService.class);

        infoFactory.addAttribute("args", ArrayList.class, true);
        infoFactory.addAttribute("props", Properties.class, true);
        infoFactory.addAttribute("simpleIDL", boolean.class, true);
        infoFactory.addAttribute("writeSystemExceptionStackTrace", boolean.class, true);
        infoFactory.addReference("adapterManager", AdapterManager.class);

        infoFactory.setConstructor(new String[]{"adapterManager", "args", "props", "simpleIDL", "writeSystemExceptionStackTrace"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

