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
import javax.security.auth.kerberos.KerberosPrincipal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import com.sun.security.jgss.GSSUtil;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.MessageProp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.control.BootstrapCook;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;
import org.apache.geronimo.network.protocol.control.commands.SetAttributeMenuItem;
import org.apache.geronimo.network.protocol.control.commands.SetReferenceMenuItem;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Rev$ $Date$
 */
public class GSSAPIServerProtocol extends AbstractProtocol implements BootstrapCook {

    final static private Log log = LogFactory.getLog(GSSAPIServerProtocol.class);

    private ThreadPool threadPool;
    private String serverNameString;
    private boolean mutualAuth;
    private boolean confidential;
    private boolean integrity;
    private GSSContext context;
    private Subject clientSubject;
    Latch startupLatch;

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

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public boolean isIntegrity() {
        return integrity;
    }

    public void setIntegrity(boolean integrity) {
        this.integrity = integrity;
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        GSSAPIServerProtocol result = (GSSAPIServerProtocol) super.clone();

        result.startupLatch = new Latch();
        try {
            GSSManager manager = GSSManager.getInstance();
            result.context = manager.createContext((GSSCredential) null);
            result.context.requestMutualAuth(mutualAuth);
            result.context.requestConf(confidential);
            result.context.requestInteg(integrity);
            result.context.requestCredDeleg(true);
        } catch (GSSException e) {
            throw new CloneNotSupportedException(e.toString());
        }
        return result;
    }
    public void setup() throws ProtocolException {
        log.trace("Starting");
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
                byte[] token = context.acceptSecContext(buffer.array(), buffer.position(), buffer.remaining());

                PlainDownPacket reply = new PlainDownPacket();
                reply.setBuffers(Collections.singletonList(ByteBuffer.allocate(token.length).put(token).flip()));
                getDownProtocol().sendDown(reply);

                if (context.isEstablished()) {
                    log.trace("SECURE CONTEXT ESTABLISHED");
                    log.trace("Client is " + context.getSrcName());
                    log.trace("Server is " + context.getTargName());
                    if (context.getMutualAuthState()) log.trace("MUTUAL AUTHENTICATION IN PLACE");
                    if (context.getConfState()) log.trace("CONFIDENTIALITY IN PLACE");
                    if (context.getIntegState()) log.trace("INTEGRITY IN PLACE");
                    if (context.getCredDelegState()) {
                        log.trace("DELEGATE IN PLACE");
                        clientSubject = GSSUtil.createSubject(context.getSrcName(), context.getDelegCred());
                    } else {
                        clientSubject = new Subject();
                        KerberosPrincipal principal = new KerberosPrincipal(context.getSrcName().toString());
                        clientSubject.getPrincipals().add(principal);
                    }
                    startupLatch.release();
                }
            } else {
                ByteBuffer buffer = packet.getBuffer();

                byte[] token = context.unwrap(buffer.array(), buffer.position(), buffer.remaining(), new MessageProp(0, true));
                UpPacket message = new UpPacket();
                message.setBuffer((ByteBuffer) ByteBuffer.allocate(token.length).put(token).flip());

                MetadataSupport.setSubject(message, clientSubject);

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

    public Collection cook(ControlContext context) {
        ArrayList list = new ArrayList(6);

        CreateInstanceMenuItem create = new CreateInstanceMenuItem();
        create.setClassName("org.apache.geronimo.network.protocol.GSSAPIClientProtocol");
        create.setInstanceId(context.assignId(this));
        list.add(create);

        SetAttributeMenuItem attribute = new SetAttributeMenuItem();
        attribute.setInstanceId(context.assignId(this));
        attribute.setAttributeName("ServerNameString");
        attribute.setAttributeValue(serverNameString);
        list.add(attribute);

        attribute = new SetAttributeMenuItem();
        attribute.setInstanceId(context.assignId(this));
        attribute.setAttributeName("MutualAuth");
        attribute.setAttributeValue(new Boolean(mutualAuth));
        list.add(attribute);

        attribute = new SetAttributeMenuItem();
        attribute.setInstanceId(context.assignId(this));
        attribute.setAttributeName("Confidential");
        attribute.setAttributeValue(new Boolean(confidential));
        list.add(attribute);

        attribute = new SetAttributeMenuItem();
        attribute.setInstanceId(context.assignId(this));
        attribute.setAttributeName("Integrity");
        attribute.setAttributeValue(new Boolean(integrity));
        list.add(attribute);

        SetReferenceMenuItem reference = new SetReferenceMenuItem();
        reference.setInstanceId(context.assignId(this));
        reference.setReferenceName("ThreadPool");
        reference.setReferenceId(context.assignId(threadPool));
        list.add(reference);

        return list;
    }
}
