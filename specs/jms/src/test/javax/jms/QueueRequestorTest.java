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

package javax.jms;

import com.mockobjects.jms.MockMessage;
import com.mockobjects.jms.MockQueue;
import com.mockobjects.jms.MockQueueReceiver;
import com.mockobjects.jms.MockQueueSender;
import com.mockobjects.jms.MockQueueSession;
import com.mockobjects.jms.MockTemporaryQueue;
import com.mockobjects.jms.MockTextMessage;

import junit.framework.TestCase;


/**
 * @version $Revision: 1.1 $ $Date: 2003/12/07 16:28:07 $
 */
public class QueueRequestorTest extends TestCase {
    public void testConstructorNullQueue() {
        MockQueue queue = new MockQueue();

        try {
            new QueueRequestor(null, queue);
            fail();
        } catch (JMSException ex) {
            fail("JMSException should not have been thrown.");
        } catch (NullPointerException ex) {
            // success.
        }

        queue.verify();
    }

    public void testConstructorSessionNull() {
        MockQueueSession session = new MockQueueSession();

        try {
            new QueueRequestor(session, null);
            fail();
        } catch (JMSException ex) {
            // success
        }

        session.verify();
    }

    public void testConstructorSessionQueue() {
        MockQueue queue = new MockQueue();
        MockQueueReceiver receiver = new MockQueueReceiver();
        MockQueueSender sender = new MockQueueSender();
        MockQueueSession session = new MockQueueSession();
        MockTemporaryQueue tempQueue = new MockTemporaryQueue();

        session.setupReceiver(receiver);
        session.setupSender(sender);
        session.setupTemporaryQueue(tempQueue);

        try {
            new QueueRequestor(session, queue);
            // success
        } catch (JMSException ex) {
            fail();
        }

        queue.verify();
        receiver.verify();
        sender.verify();
        session.verify();
        tempQueue.verify();
    }

    public void testRequestNull() {
        MockQueue queue = new MockQueue();
        MockQueueReceiver receiver = new MockQueueReceiver();
        MockQueueSender sender = new MockQueueSender();
        MockQueueSession session = new MockQueueSession();
        MockTemporaryQueue tempQueue = new MockTemporaryQueue();

        session.setupReceiver(receiver);
        session.setupSender(sender);
        session.setupTemporaryQueue(tempQueue);

        try {
            QueueRequestor requestor = new QueueRequestor(session, queue);
            requestor.request(null);
            fail();
        } catch (JMSException ex) {
            fail("JMSException should not have been thrown.");
        } catch (NullPointerException ex) {
            // success
        }

        queue.verify();
        receiver.verify();
        sender.verify();
        session.verify();
        tempQueue.verify();
    }

    public void testRequestMessage() {
        MockMessage reply = new MockTextMessage();
        MockMessage request = new MockTextMessage();
        MockQueue queue = new MockQueue();
        MockQueueReceiver receiver = new MockQueueReceiver();
        MockQueueSender sender = new MockQueueSender();
        MockQueueSession session = new MockQueueSession();
        MockTemporaryQueue tempQueue = new MockTemporaryQueue();

        request.setExpectedJMSReplyTo(tempQueue);

        receiver.setExpectedReceiveCalls(1);
        receiver.setupReceivedMessage(reply);

        sender.setExpectedSendCalls(1);

        session.setupReceiver(receiver);
        session.setupSender(sender);
        session.setupTemporaryQueue(tempQueue);

        try {
            QueueRequestor requestor = new QueueRequestor(session, queue);
            Message jmsReply = requestor.request(request);
            assertEquals(jmsReply, reply);
        } catch (JMSException ex) {
            fail("JMSException should not have been thrown.");
        }

        reply.verify();
        request.verify();
        queue.verify();
        receiver.verify();
        sender.verify();
        session.verify();
        tempQueue.verify();
    }

    public void testClose() {
        MockQueue queue = new MockQueue();
        MockQueueReceiver receiver = new MockQueueReceiver();
        MockQueueSender sender = new MockQueueSender();
        MockQueueSession session = new MockQueueSession();
        MockTemporaryQueue tempQueue = new MockTemporaryQueue();

        session.setExpectedCloseCalls(1);
        session.setupReceiver(receiver);
        session.setupSender(sender);
        session.setupTemporaryQueue(tempQueue);

        tempQueue.setExpectedDeleteCalls(1);

        try {
            QueueRequestor requestor = new QueueRequestor(session, queue);
            requestor.close();
        } catch (JMSException ex) {
            fail();
        }

        queue.verify();
        receiver.verify();
        sender.verify();
        session.verify();
        tempQueue.verify();
    }
}
