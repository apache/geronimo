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

package org.apache.geronimo.network.protocol;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Properties;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.control.ControlClientProtocol;
import org.apache.geronimo.network.protocol.control.ControlClientProtocolStack;
import org.apache.geronimo.network.protocol.control.ControlServerListener;
import org.apache.geronimo.network.protocol.control.ControlServerProtocol;
import org.apache.geronimo.network.protocol.control.ControlServerProtocolStack;
import org.apache.geronimo.network.protocol.control.ControlServerProtocolWaiter;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Revision: 1.8 $ $Date: 2004/07/10 16:26:36 $
 */
public class GSSAPIProtocolTest extends TestCase {

    final static private Log log = LogFactory.getLog(GSSAPIProtocolTest.class);

    private Properties properties;
    private Subject clientSubject;
    private Subject serverSubject;
    private boolean hasKerberos = false;
    private Latch startLatch;
    private Latch shutdownLatch;
    private Latch stopLatch;
    private ThreadGroup threadGroup;
    private ServerSocketAcceptor ssa;

    public void testDummy() throws Exception {
    }

    public void test() throws Exception {
        if (!hasKerberos) return;

        new Thread(threadGroup, new ServerThread(serverSubject), "Geronimo server").start();

        startLatch.acquire();

        PrivilegedExceptionAction clientAction = new ClientAction();
        Subject.doAs(clientSubject, clientAction);

        stopLatch.acquire();
    }

    class ClientAction implements PrivilegedExceptionAction {

        public Object run() throws Exception {
            ThreadPool tp = new ThreadPool();
            tp.setKeepAliveTime(1 * 1000);
            tp.setPoolSize(5);
            tp.setPoolName("Client TP");
            tp.doStart();

            ClockPool cp = new ClockPool();
            cp.setPoolName("Client CP");
            cp.doStart();

            SelectorManager sm = new SelectorManager();
            sm.setThreadPool(tp);
            sm.setThreadName("Client Selector Manager");
            sm.doStart();

            ControlClientProtocolStack clientStack = new ControlClientProtocolStack();
            clientStack.setClassLoader(Thread.currentThread().getContextClassLoader());
            clientStack.setThreadPool(tp);
            clientStack.setClockPool(cp);
            clientStack.setSelectorManager(sm);

            SocketProtocol sp = new SocketProtocol();
            sp.setTimeout(1000 * 1000); //todo reset to 10s
            sp.setInterface(new InetSocketAddress(ssa.getConnectURI().getHost(), 0));
            sp.setAddress(new InetSocketAddress(ssa.getConnectURI().getHost(), ssa.getConnectURI().getPort()));
            sp.setSelectorManager(sm);

            clientStack.push(sp);

            ControlClientProtocol ccp = new ControlClientProtocol();
            ccp.setTimeout(1000 * 1000); //todo set to 10s

            clientStack.push(ccp);

            clientStack.setup();
            Thread.sleep(5 * 1000); //todo delete

            clientStack.sendDown(getPlainPacket());
            clientStack.sendDown(getPlainPacket());
            clientStack.sendDown(getPlainPacket());

            Thread.sleep(5 * 1000); //todo back to 5s

            clientStack.drain();

            shutdownLatch.release();

            sm.doStop();

            cp.doStop();

            tp.doStop();

            stopLatch.release();

            return null;
        }
    }

    class ServerThread implements Runnable {

        private Subject subject;

        ServerThread(Subject subject) {
            this.subject = subject;
        }

        public void run() {
            try {
                PrivilegedExceptionAction serverAction = new ServerAction();
                Subject.doAs(subject, serverAction);
            } catch (PrivilegedActionException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerAction implements PrivilegedExceptionAction {

        public Object run() throws Exception {
            ThreadPool tp = new ThreadPool();
            tp.setKeepAliveTime(1 * 1000);
            tp.setPoolSize(5);
            tp.setPoolName("Server TP");
            tp.doStart();

            ClockPool cp = new ClockPool();
            cp.setPoolName("Server CP");
            cp.doStart();

            SelectorManager sm = new SelectorManager();
            sm.setThreadPool(tp);
            sm.setThreadName("Server Selector Manager");
            sm.doStart();

            ControlServerProtocolStack templateStack = new ControlServerProtocolStack();

            SocketProtocol spt = new SocketProtocol();
            spt.setTimeout(10 * 1000);
            spt.setSelectorManager(sm);

            templateStack.push(spt);

            ControlServerProtocol csp = new ControlServerProtocol();
            csp.setTimeout(1 * 1000);
            csp.setThreadPool(tp);
            csp.setClockPool(cp);
            csp.setSelectorManager(sm);
            csp.setControlServerListener(new ControlServerListener() {
                public void shutdown() {
                    log.trace("SERVER SIDE SHUTDOWN");
                }
            });

            templateStack.push(csp);

            ControlServerProtocolWaiter waiter = new ControlServerProtocolWaiter();

            GSSAPIServerProtocol gsp = new GSSAPIServerProtocol();
            gsp.setThreadPool(tp);
            gsp.setMutualAuth(true);
            gsp.setConfidential(true);
            gsp.setIntegrity(true);
            gsp.setServerNameString(properties.getProperty("SERVER"));

            waiter.push(gsp);

            waiter.push(new CountingProtocol());

            TestProtocol test = new TestProtocol();
            test.setValue("SimpleTest");
            test.setThreadPool(tp);
            test.setClockPool(cp);
            test.setSelectorManager(sm);

            waiter.push(test);

            templateStack.push(waiter);

            ProtocolFactory pf = new ProtocolFactory();
            pf.setClockPool(cp);
            pf.setMaxAge(Long.MAX_VALUE);
            pf.setMaxInactivity(1 * 60 * 60 * 1000);
            pf.setReclaimPeriod(10 * 1000);
            pf.setTemplate(templateStack);

            ssa = new ServerSocketAcceptor();
            ssa.setSelectorManager(sm);
            ssa.setTimeOut(5 * 1000);
            ssa.setUri(new URI("async://localhost:0/?tcp.nodelay=true&tcp.backlog=5#"));
            ssa.setAcceptorListener(pf);
            ssa.startup();

            startLatch.release();

            shutdownLatch.acquire();

            ssa.drain();

            pf.drain();

            sm.doStop();

            cp.doStop();

            tp.doStop();

            return null;
        }
    }

    public void setUp() throws Exception {
        startLatch = new Latch();
        shutdownLatch = new Latch();
        stopLatch = new Latch();
        try {
            properties = new Properties();
            try {
                properties.load(new File(System.getProperty("user.home") + "/login.properties").toURI().toURL().openStream());
            } catch (IOException e) {
                properties.load(new File("./src/test-data/data/login.properties").toURI().toURL().openStream());
            }
            LoginContext lc = new LoginContext("SampleClient", new UsernamePasswordCallback(properties.getProperty("USER"), properties.getProperty("USER_PASSWORD")));
            lc.login();
            clientSubject = lc.getSubject();

            lc = new LoginContext("SampleServer", new UsernamePasswordCallback(properties.getProperty("SERVER"), properties.getProperty("SERVER_PASSWORD")));
            lc.login();
            serverSubject = lc.getSubject();

            threadGroup = new ThreadGroup("Geronimo GSSAPI Server");

            hasKerberos = true;
        } catch (LoginException e) {
            hasKerberos = false;
        }
    }

    static volatile long id = 0;

    protected PlainDownPacket getPlainPacket() {
        PlainDownPacket packet = new PlainDownPacket();
        ArrayList list = new ArrayList();

        final int COUNT = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(COUNT);
        for (int i = 0; i < COUNT; i++) {
            buffer.put((byte) 0x0b);
        }
        buffer.flip();

        list.add(buffer);
        packet.setBuffers(list);

        return packet;
    }

}
