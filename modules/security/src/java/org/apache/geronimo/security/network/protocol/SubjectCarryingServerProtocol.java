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

import javax.security.auth.Subject;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.AbstractProtocol;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.network.protocol.MetadataSupport;
import org.apache.geronimo.network.protocol.control.BootstrapCook;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.ContextManager;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:26 $
 */
public class SubjectCarryingServerProtocol extends AbstractProtocol implements BootstrapCook {

    final static private Log log = LogFactory.getLog(SubjectCarryingServerProtocol.class);

    private Subject clientSubject;

    public void doStart() throws ProtocolException {
        log.trace("Starting");
    }

    public void doStop() throws ProtocolException {
        log.trace("Stopping");
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        log.trace("sendUp");
        UpPacket p = SubjectCarryingPacketReader.getInstance().read(packet.getBuffer());
        if (p instanceof PassthroughUpPacket) {
            MetadataSupport.setSubject(packet, clientSubject);
            getUp().sendUp(packet);
        } else if (p instanceof SubjectCarryingUpPacket) {
            SubjectCarryingUpPacket subjectPacket = (SubjectCarryingUpPacket)p;
            clientSubject = ContextManager.getRegisteredSubject(subjectPacket.getSubjectId());

            MetadataSupport.setSubject(packet, clientSubject);
            getUp().sendUp(packet);
        }
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        log.trace("sendDown");
        getDown().sendDown(packet);
    }

    public Collection cook(ControlContext context) {
        ArrayList list = new ArrayList(1);

        CreateInstanceMenuItem create = new CreateInstanceMenuItem();
        create.setClassName("org.apache.geronimo.security.network.protocol.SubjectCarryingClientProtocol");
        create.setInstanceId(context.assignId(this));
        list.add(create);

        return list;
    }

}
