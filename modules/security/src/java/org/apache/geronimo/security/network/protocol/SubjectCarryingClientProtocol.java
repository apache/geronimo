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

import java.security.AccessController;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.AbstractProtocol;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.security.IdentificationPrincipal;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:26 $
 */
public class SubjectCarryingClientProtocol extends AbstractProtocol {

    final static private Log log = LogFactory.getLog(SubjectCarryingClientProtocol.class);

    private Subject clientSubject;

    public void doStart() throws ProtocolException {
        log.trace("Starting");
    }

    public void doStop() throws ProtocolException {
        log.trace("Stopping");
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        getUp().sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (clientSubject == subject) {
            PassthroughDownPacket passthroughPacket = new PassthroughDownPacket();
            passthroughPacket.setBuffers(packet.getBuffers());

            getDown().sendDown(passthroughPacket);
        } else {
            clientSubject = subject;
            Collection principals = clientSubject.getPrincipals(IdentificationPrincipal.class);

            if (principals.isEmpty()) {
                PassthroughDownPacket passthroughPacket = new PassthroughDownPacket();
                passthroughPacket.setBuffers(packet.getBuffers());

                getDown().sendDown(passthroughPacket);
            } else {
                IdentificationPrincipal principal = (IdentificationPrincipal) principals.iterator().next();

                SubjectCaryingDownPacket subjectPacket = new SubjectCaryingDownPacket();
                subjectPacket.setSubjectId(principal.getId());
                subjectPacket.setBuffers(packet.getBuffers());

                getDown().sendDown(subjectPacket);
            }
        }
    }

}
