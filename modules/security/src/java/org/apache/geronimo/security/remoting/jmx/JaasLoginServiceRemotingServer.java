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

package org.apache.geronimo.security.remoting.jmx;

import org.activeio.AcceptListener;
import org.activeio.AsyncChannelServer;
import org.activeio.Channel;
import org.activeio.Packet;
import org.activeio.RequestChannel;
import org.activeio.SyncChannel;
import org.activeio.SyncChannelServer;
import org.activeio.adapter.AsyncChannelToServerRequestChannel;
import org.activeio.adapter.AsyncToSyncChannel;
import org.activeio.adapter.SyncToAsyncChannel;
import org.activeio.adapter.SyncToAsyncChannelServer;
import org.activeio.filter.PacketAggregatingAsyncChannel;
import org.activeio.net.SocketMetadata;
import org.activeio.net.SocketSyncChannelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.security.jaas.JaasLoginServiceMBean;

import javax.management.ObjectName;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * A server-side utility that exposes a JaasLoginService to remote clients.
 * It prevents clients from connecting to arbitrary server-side MBeans through
 * this listener -- only the JaasLoginService is exposed.
 *
 * @version $Rev: 56022 $ $Date: 2004-10-30 01:16:18 -0400 (Sat, 30 Oct 2004) $
 */
public class JaasLoginServiceRemotingServer implements GBeanLifecycle, NetworkConnector {

    public static final ObjectName REQUIRED_OBJECT_NAME = JMXUtil.getObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");

    private static final Log log = LogFactory.getLog(JaasLoginServiceRemotingServer.class);
    private AsyncChannelServer server;
    private JaasLoginServiceMBean loginService;
    private String protocol;
    private String host;
    private int port;

    public JaasLoginServiceRemotingServer(String protocol, String host, int port, JaasLoginServiceMBean loginService) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.loginService = loginService;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public URI getClientConnectURI() {
        return server.getConnectURI();
    }

    public InetSocketAddress getListenAddress() {
        if (server != null) {
            URI uri = server.getBindURI();
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        } else {
            return new InetSocketAddress(host, port);
        }
    }

    public void doStart() throws Exception {
        final ReflexiveInterceptor loginServiceInterceptor = new ReflexiveInterceptor(loginService);

        server = createAsyncChannelServer();
        server.setAcceptListener(new AcceptListener() {
            public void onAccept(Channel channel) {
                RequestChannel requestChannel = null;
                try {
                    SyncChannel syncChannel = AsyncToSyncChannel.adapt(channel);
                    SocketMetadata socket = (SocketMetadata) syncChannel.narrow(SocketMetadata.class);
                    socket.setTcpNoDelay(true);

                    requestChannel = createRequestChannel(syncChannel);

                    RequestChannelInterceptorInvoker invoker = new RequestChannelInterceptorInvoker(loginServiceInterceptor, loginService.getClass().getClassLoader());
                    requestChannel.setRequestListener(invoker);
                    requestChannel.start();
                } catch (IOException e) {
                    log.info("Failed to accept connection.", e);
                    if (requestChannel != null)
                        requestChannel.dispose();
                    else
                        channel.dispose();
                }
            }

            public void onAcceptError(IOException error) {
                log.info("Accept Failed: " + error);
            }
        });

        server.start();
        log.info("Remote login service started on: " + server.getConnectURI() + " clients can connect to: " + server.getConnectURI());
    }

    private AsyncChannelServer createAsyncChannelServer() throws IOException, URISyntaxException {
        SocketSyncChannelFactory factory = new SocketSyncChannelFactory();
        SyncChannelServer server = factory.bindSyncChannel(new URI(protocol, null, host, port, null, null, null));
        return new SyncToAsyncChannelServer(server);
    }

    private RequestChannel createRequestChannel(SyncChannel channel) throws IOException {

        return new AsyncChannelToServerRequestChannel(new PacketAggregatingAsyncChannel(SyncToAsyncChannel.adapt(channel))) {
            /**
             * close out the channel once one request has been serviced.
             */
            public void onPacket(Packet packet) {
                super.onPacket(packet);
                dispose();
            }
        };
    }

    public void doStop() {
        server.dispose();
        server = null;
        log.info("Stopped remote login service.");
    }

    public void doFail() {
        if (server != null) {
            server.dispose();
            server = null;
        }
        log.info("Failed remote login service.");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Remote Login Listener", JaasLoginServiceRemotingServer.class); //has fixed name, j2eeType is irrelevant
        infoFactory.addAttribute("clientConnectURI", URI.class, false);
        infoFactory.addReference("LoginService", JaasLoginServiceMBean.class, "JaasLoginService");
        infoFactory.addInterface(NetworkConnector.class, new String[]{"host", "port", "protocol"}, new String[]{"host", "port"});
        infoFactory.setConstructor(new String[]{"protocol", "host", "port", "LoginService"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
