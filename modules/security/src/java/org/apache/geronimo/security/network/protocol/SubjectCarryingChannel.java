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
package org.apache.geronimo.security.network.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.util.Collection;

import javax.security.auth.Subject;

import org.activeio.AsynchChannel;
import org.activeio.FilterAsynchChannel;
import org.activeio.Packet;
import org.activeio.adapter.PacketInputStream;
import org.activeio.adapter.PacketOutputStream;
import org.activeio.packet.AppendedPacket;
import org.activeio.packet.ByteArrayPacket;
import org.activeio.packet.FilterPacket;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;

/**
 * SubjectCarryingChannel is a FilterAsynchChannel that allows you to send 
 * the subject associated with the current write operation down to the remote
 * end of the channel.
 * 
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class SubjectCarryingChannel extends FilterAsynchChannel {

    static final byte PASSTHROUGH = (byte)0x00;
    static final byte SET_SUBJECT = (byte)0x01;
    static final byte CLEAR_SUBJECT = (byte)0x2;

    final private ByteArrayPacket header = new ByteArrayPacket(new byte[1+8+4]);

    private Subject remoteSubject;
    private Subject localSubject;
    
    private final boolean enableLocalSubjectPublishing;
    private final boolean enableRemoteSubjectConsumption;

    public SubjectCarryingChannel(AsynchChannel next) {
        this(next, true, true);
    }
    
    public SubjectCarryingChannel(AsynchChannel next, boolean enableLocalSubjectPublishing, boolean enableRemoteSubjectConsumption) {
        super(next);
        this.enableLocalSubjectPublishing = enableLocalSubjectPublishing;
        this.enableRemoteSubjectConsumption = enableRemoteSubjectConsumption;
    }
    
    public void write(Packet packet) throws IOException {
        
        // Don't add anything to the packet stream if subject writing is not enabled.
        if( !enableLocalSubjectPublishing ) {
            super.write(packet);
            return;
        }
        
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (remoteSubject != subject) {
            remoteSubject = subject;
            Collection principals = remoteSubject.getPrincipals(IdentificationPrincipal.class);
            
            if (principals.isEmpty()) {
                super.write(createClearSubjectPackt());
            } else {                
                IdentificationPrincipal principal = (IdentificationPrincipal) principals.iterator().next();
                SubjectId subjectId = principal.getId();                
                super.write(createSubjectPacket(subjectId.getSubjectId(), subjectId.getHash()));
            }
            
        }
        super.write(createPassthroughPacket(packet));
    }

    public void onPacket(Packet packet) {
        
        // Don't take anything to the packet stream if subject reading is not enabled.
        if( !enableRemoteSubjectConsumption ) {
            super.onPacket(packet);
            return;
        }
        
        try {
            switch( packet.read() ) {
            	case CLEAR_SUBJECT:
            	    localSubject = null;
                    return;
            	case SET_SUBJECT:        	    
            	    SubjectId subjectId = extractSubjectId(packet);
            	    localSubject = ContextManager.getRegisteredSubject(subjectId);
                    return;
            	case PASSTHROUGH:
                    super.onPacket(new FilterPacket(packet){
                        public Object narrow(Class target) {
                            if( target == SubjectContext.class ) {
                                return new SubjectContext() {
                                    public Subject getSubject() {
                                        return remoteSubject;
                                    }
                                };
                            }
                            return super.narrow(target);
                        }
                        public Packet filter(Packet packet) {
                            return packet;
                        }
                    });
            }
        } catch (IOException e) {
            super.onPacketError(e);
        }

        super.onPacket(packet);
    }

    /**
     */
    private SubjectId extractSubjectId(Packet packet) throws IOException {
        DataInputStream is = new DataInputStream(new PacketInputStream(packet));
        Long id = new Long(is.readLong());
        byte hash[]=  new byte[ is.readInt() ];
	    return new SubjectId(id, hash);
    }

    private Packet createClearSubjectPackt() {
        header.clear().write(CLEAR_SUBJECT);
        return header.flip();
    }

    private Packet createSubjectPacket(Long subjectId, byte[] hash) throws IOException {
        header.clear();
        DataOutputStream os = new DataOutputStream(new PacketOutputStream(header));
        os.writeByte(SET_SUBJECT);
        os.writeLong(subjectId.longValue());
        os.writeInt(hash.length);
        os.close();
        return AppendedPacket.join(header.flip(), new ByteArrayPacket(hash));
    }

    private Packet createPassthroughPacket(Packet packet) {
        header.clear().write(PASSTHROUGH);        
        return AppendedPacket.join(header.flip(),packet);
    }

    public Subject getLocalSubject() {
        return localSubject;
    }

    public Subject getRemoteSubject() {
        return remoteSubject;
    }
    
}
