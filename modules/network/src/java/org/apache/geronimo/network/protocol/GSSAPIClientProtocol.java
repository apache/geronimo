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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.pool.ThreadPool;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;


import EDU.oswego.cs.dl.util.concurrent.Latch;


/**
 * @version $Revision: 1.8 $ $Date: 2004/08/01 13:03:43 $
 */
public class GSSAPIClientProtocol extends AbstractProtocol {

    final static private Log log = LogFactory.getLog(GSSAPIClientProtocol.class);

    private ThreadPool threadPool;
    private String serverNameString;
    private GSSContext context;
    private boolean mutualAuth;
    private boolean confidential;
    private boolean integrity;
    Latch startupLatch = new Latch();


    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public String getServerNameString() {
        return serverNameString;
    }

    public void setServerNameString(String serverNameString) {
        this.serverNameString = serverNameString;
    }

    public boolean isMutualAuth() {
        return mutualAuth;
    }

    public void setMutualAuth(boolean mutualAuth) {
        this.mutualAuth = mutualAuth;
    }

    public void setMutualAuth(Boolean mutualAuth) {
        this.mutualAuth = mutualAuth.booleanValue();
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential.booleanValue();
    }

    public boolean isIntegrity() {
        return integrity;
    }

    public void setIntegrity(boolean integrity) {
        this.integrity = integrity;
    }

    public void setIntegrity(Boolean integrity) {
        this.integrity = integrity.booleanValue();
    }


    public void setup() throws ProtocolException {
        log.trace("Starting");
        try {
            Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
            GSSManager manager = GSSManager.getInstance();
            GSSName serverName = manager.createName(serverNameString, null);
            context = manager.createContext(serverName,
                                            krb5Oid,
                                            null,
                                            GSSContext.DEFAULT_LIFETIME);
            context.requestMutualAuth(mutualAuth);
            context.requestConf(confidential);
            context.requestInteg(integrity);
            context.requestCredDeleg(true);

            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        byte[] token = new byte[0];
                        token = context.initSecContext(token, 0, token.length);
                        PlainDownPacket packet = new PlainDownPacket();
                        packet.setBuffers(Collections.singletonList(ByteBuffer.allocate(token.length).put(token).flip()));
                        getDownProtocol().sendDown(packet);
                    } catch (ProtocolException e) {
                    } catch (GSSException e) {
                    }
                }
            });
        } catch (GSSException e) {
            throw new ProtocolException(e);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public void drain() throws ProtocolException {
        log.trace("Stoping");
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        try {
            log.trace("sendUp");
            if (!context.isEstablished()) {
                ByteBuffer buffer = packet.getBuffer();
                byte[] token = context.initSecContext(buffer.array(), buffer.position(), buffer.remaining());

                if (!context.isEstablished()) {
                    PlainDownPacket reply = new PlainDownPacket();
                    reply.setBuffers(Collections.singletonList(ByteBuffer.allocate(token.length).put(token).flip()));
                    getDownProtocol().sendDown(reply);
                } else {
                    log.trace("SECURE CONTEXT ESTABLISHED");
                    log.trace("Client is " + context.getSrcName());
                    log.trace("Server is " + context.getTargName());
                    if (context.getMutualAuthState()) log.trace("MUTUAL AUTHENTICATION IN PLACE");
                    if (context.getConfState()) log.trace("CONFIDENTIALITY IN PLACE");
                    if (context.getIntegState()) log.trace("INTEGRITY IN PLACE");

                    log.trace("RELEASING " + startupLatch);
                    startupLatch.release();
                    log.trace("RELEASED " + startupLatch);
                }
            } else {
                ByteBuffer buffer = packet.getBuffer();

                byte[] token = context.unwrap(buffer.array(), buffer.position(), buffer.remaining(), new MessageProp(0, true));
                UpPacket message = new UpPacket();
                message.setBuffer((ByteBuffer) ByteBuffer.allocate(token.length).put(token).flip());
                getUpProtocol().sendUp(message);
            }
        } catch (GSSException e) {
            throw new ProtocolException(e);
        }
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        try {
            log.trace("sendDown");

            log.trace("AQUIRING " + startupLatch);
            if (!startupLatch.attempt(1000 * 1000)) throw new ProtocolException("Send timeout");
            log.trace("AQUIRED " + startupLatch);

            int size = 0;
            for (Iterator iter = packet.getBuffers().iterator(); iter.hasNext();) {
                size += ((ByteBuffer) iter.next()).remaining();
            }
            ByteBuffer buffer = ByteBuffer.allocate(size);
            for (Iterator iter = packet.getBuffers().iterator(); iter.hasNext();) {
                buffer.put((ByteBuffer) iter.next());
            }
            buffer.flip();

            byte[] token = context.wrap(buffer.array(), buffer.position(), buffer.remaining(), new MessageProp(0, true));
            PlainDownPacket reply = new PlainDownPacket();
            reply.setBuffers(Collections.singletonList(ByteBuffer.allocate(token.length).put(token).flip()));
            getDownProtocol().sendDown(reply);
        } catch (GSSException e) {
            throw new ProtocolException(e);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public void flush() throws ProtocolException {
        getDownProtocol().flush();
    }
}
