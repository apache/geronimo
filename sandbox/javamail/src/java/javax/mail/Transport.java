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

package javax.mail;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:07 $
 */
public abstract class Transport extends Service {
    public static void send(Message message) throws MessagingException {
        send(message, message.getAllRecipients());
    }
    public static void send(Message message, Address[] addresses)
        throws MessagingException {
        // TODO Implement
        Transport transport = null; // Lookup based on protocol?
        transport.sendMessage(message, addresses);
    }
    private List _listeners = new LinkedList();
    public Transport(Session session, URLName name) {
        super(session,name);
    }
    public void addTransportListener(TransportListener listener) {
        _listeners.add(listener);
    }
    protected void notifyTransportListeners(
        int type,
        Address[] validSent,
        Address[] validUnsent,
        Address[] invalid,
        Message message) {
        TransportEvent event =
            new TransportEvent(
                this,
                type,
                validSent,
                validUnsent,
                invalid,
                message);
        Iterator it = _listeners.iterator();
        while (it.hasNext()) {
            TransportListener listener = (TransportListener) it.next();
            event.dispatch(listener);
        }
    }
    public void removeTransportListener(TransportListener listener) {
        _listeners.remove(listener);
    }
    public abstract void sendMessage(Message message, Address[] addresses)
        throws MessagingException;
}
