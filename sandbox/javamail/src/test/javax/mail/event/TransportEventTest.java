/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail.event;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.TestData;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import junit.framework.TestCase;
/**
 * @version $Revision: 1.1 $ $Date: 2004/01/29 04:20:06 $
 */
public class TransportEventTest extends TestCase {
    public TransportEventTest(String name) {
        super(name);
    }
    public void testEvent() throws AddressException {
        doEventTests(TransportEvent.MESSAGE_DELIVERED);
        doEventTests(TransportEvent.MESSAGE_PARTIALLY_DELIVERED);
        doEventTests(TransportEvent.MESSAGE_NOT_DELIVERED);
        // TODO Should really instantiate some messages to test this
        try {
            TransportEvent event =
                new TransportEvent(
                    TestData.getTestTransport(),
                    -12345,
                    null,
                    null,
                    null,
                    null);
            fail("Expected exception due to invalid type " + event.getType());
        } catch (IllegalArgumentException e) {
        }
    }
    private void doEventTests(int type) throws AddressException {
        Folder folder = TestData.getTestFolder();
        Message message = TestData.getMessage();
        Transport transport = TestData.getTestTransport();
        Address[] sent = new Address[] { new InternetAddress("alex@here.com")};
        Address[] empty = new Address[0];
        TransportEvent event =
            new TransportEvent(transport, type, sent, empty, empty, message);
        assertEquals(transport, event.getSource());
        assertEquals(type, event.getType());
        TransportListenerTest listener = new TransportListenerTest();
        event.dispatch(listener);
        assertEquals("Unexpcted method dispatched", type, listener.getState());
    }
    public static class TransportListenerTest implements TransportListener {
        private int state = 0;
        public void messageDelivered(TransportEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = TransportEvent.MESSAGE_DELIVERED;
        }
        public void messagePartiallyDelivered(TransportEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = TransportEvent.MESSAGE_PARTIALLY_DELIVERED;
        }
        public void messageNotDelivered(TransportEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = TransportEvent.MESSAGE_NOT_DELIVERED;
        }
        public int getState() {
            return state;
        }
    }
}
