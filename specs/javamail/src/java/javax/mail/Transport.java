/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

/**
 * Abstract class modeling a message transport.
 *
 * @version $Rev$ $Date$
 */
public abstract class Transport extends Service {
    /**
     * Send a message to all recipient addresses it contains (as returned by {@link Message#getAllRecipients()})
     * using message transports appropriate for each address. Message addresses are checked during submission,
     * but there is no guarantee that the ultimate address is valid or that the message will ever be delivered.
     * <p/>
     * {@link Message#saveChanges()} will be called before the message is actually sent.
     *
     * @param message the message to send
     * @throws MessagingException if there was a problem sending the message
     */
    public static void send(Message message) throws MessagingException {
//        TODO: uncomment when MimeMessage imlements getAllRecipients();
//        send(message, message.getAllRecipients());
    }

    /**
     * Send a message to all addresses provided irrespective of any recipients contained in the message itself
     * using message transports appropriate for each address. Message addresses are checked during submission,
     * but there is no guarantee that the ultimate address is valid or that the message will ever be delivered.
     * <p/>
     * {@link Message#saveChanges()} will be called before the message is actually sent.
     *
     * @param message   the message to send
     * @param addresses the addesses to send to
     * @throws MessagingException if there was a problem sending the message
     */
    public static void send(Message message, Address[] addresses) throws MessagingException {
        Session session = message.session;
        Map msgsByTransport = new HashMap();
        for (int i = 0; i < addresses.length; i++) {
            Address address = addresses[i];
            Transport transport = session.getTransport(address);
            List addrs = (List) msgsByTransport.get(transport);
            if (addrs == null) {
                addrs = new ArrayList();
                msgsByTransport.put(transport, addrs);
            }
            addrs.add(address);
        }

        message.saveChanges();

        for (Iterator i = msgsByTransport.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Transport transport = (Transport) entry.getKey();
            List addrs = (List) entry.getValue();
            transport.sendMessage(message, (Address[]) addrs.toArray(new Address[addrs.size()]));
        }
    }

    /**
     * Constructor taking Session and URLName parameters required for {@link Service#Service(Session, URLName)}.
     *
     * @param session the Session this transport is for
     * @param name    the location this transport is for
     */
    public Transport(Session session, URLName name) {
        super(session, name);
    }

    /**
     * Send a message to the supplied addresses using this transport; if any of the addresses are
     * invalid then a {@link SendFailedException} is thrown. Whether the message is actually sent
     * to any of the addresses is undefined.
     * <p/>
     * Unlike the static {@link #send(Message, Address[])} method, {@link Message#saveChanges()} is
     * not called. A {@link TransportEvent} will be sent to registered listeners once the delivery
     * attempt has been made.
     *
     * @param message   the message to send
     * @param addresses list of addresses to send it to
     * @throws SendFailedException if the send failed
     * @throws MessagingException  if there was a problem sending the message
     */
    public abstract void sendMessage(Message message, Address[] addresses) throws MessagingException;

    private Vector transportListeners = new Vector();

    public void addTransportListener(TransportListener listener) {
        transportListeners.add(listener);
    }

    public void removeTransportListener(TransportListener listener) {
        transportListeners.remove(listener);
    }

    protected void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message message) {
        queueEvent(new TransportEvent(this, type, validSent, validUnsent, invalid, message), transportListeners);
    }
}
