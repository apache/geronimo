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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.activeio.AcceptListener;
import org.activeio.AsynchChannelServer;
import org.activeio.Channel;
import org.activeio.Packet;
import org.activeio.RequestChannel;
import org.activeio.SynchChannel;
import org.activeio.SynchChannelServer;
import org.activeio.adapter.AsynchChannelToServerRequestChannel;
import org.activeio.adapter.SynchToAsynchChannelAdapter;
import org.activeio.adapter.SynchToAsynchChannelServerAdapter;
import org.activeio.filter.PacketAggregatingAsynchChannel;
import org.activeio.net.SocketSynchChannelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.security.jaas.JaasLoginServiceMBean;


/**
 * A server-side utility that exposes a JaasLoginService to remote clients.
 * It prevents clients from connecting to arbitrary server-side MBeans through
 * this listener -- only the JaasLoginService is exposed.
 * 
 * @version $Rev: 56022 $ $Date: 2004-10-30 01:16:18 -0400 (Sat, 30 Oct 2004) $
 */
public class JaasLoginServiceRemotingServer implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(JaasLoginServiceRemotingServer.class);
    private AsynchChannelServer server;
    private JaasLoginServiceMBean loginService;
    private final URI bindURI;

    public JaasLoginServiceRemotingServer(URI bindURI, JaasLoginServiceMBean loginService) {
        this.bindURI = bindURI;
        this.loginService = loginService;
    }

    public URI getClientConnectURI() {
        return server.getConnectURI();
    }
    
    public void doStart() throws Exception {
        final ReflexiveInterceptor loginServiceInterceptor = new ReflexiveInterceptor(loginService);
        
        server = createAsynchChannelServer();
        server.setAcceptListener(new AcceptListener() {
            public void onAccept(Channel channel) {
                RequestChannel requestChannel=null;
                try {
                    requestChannel = createRequestChannel((SynchChannel) channel);     
                    
                    RequestChannelInterceptorInvoker invoker = new RequestChannelInterceptorInvoker(loginServiceInterceptor, loginService.getClass().getClassLoader() ); 
                    requestChannel.setRequestListener(invoker);
                    requestChannel.start();
                } catch (IOException e) {
                    log.info("Failed to accept connection.", e);
                    if( requestChannel!=null )
                        requestChannel.dispose();
                    else
                        channel.dispose();
                }                
            }
            public void onAcceptError(IOException error) {
                log.info("Accept Failed: "+error);
            }
        });
        
        server.start();
        log.info("Remote login service started on: "+server.getConnectURI()+" clients can connect to: "+server.getConnectURI());
    }
    
    private AsynchChannelServer createAsynchChannelServer() throws IOException, URISyntaxException {
        SocketSynchChannelFactory factory = new SocketSynchChannelFactory();
        SynchChannelServer server = factory.bindSynchChannel(bindURI);
        return new SynchToAsynchChannelServerAdapter(server);        
    }

    private RequestChannel createRequestChannel(SynchChannel channel) throws IOException {
        
        return new AsynchChannelToServerRequestChannel( 
                new PacketAggregatingAsynchChannel(
                        SynchToAsynchChannelAdapter.adapt(channel))) {            
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
        server=null;        
        log.info("Stopped remote login service.");
    }

    public void doFail() {
        if( server !=null ) {
            server.dispose();
	        server=null;        
        }
        log.info("Failed remote login service.");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JaasLoginServiceRemotingServer.class);
        infoFactory.addAttribute("bindURI", URI.class, true);
        infoFactory.addAttribute("clientConnectURI", URI.class, false);        
        infoFactory.addReference("loginService", JaasLoginServiceMBean.class);
        infoFactory.setConstructor(new String[]{"bindURI", "loginService"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
