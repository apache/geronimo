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

package javax.jms;

/** The <CODE>TopicRequestor</CODE> helper class simplifies
 * making service requests.
 *
 * <P>The <CODE>TopicRequestor</CODE> constructor is given a non-transacted
 * <CODE>TopicSession</CODE> and a destination <CODE>Topic</CODE>. It creates a
 * <CODE>TemporaryTopic</CODE> for the responses and provides a
 * <CODE>request</CODE> method that sends the request message and waits
 * for its reply.
 *
 * <P>This is a basic request/reply abstraction that should be sufficient
 * for most uses. JMS providers and clients are free to create more
 * sophisticated versions.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 *
 * @see         javax.jms.QueueRequestor
 */

public class TopicRequestor {

    TopicSession session;    // The topic session the topic belongs to.
    Topic topic;      // The topic to perform the request/reply on.
    TemporaryTopic tempTopic;
    TopicPublisher publisher;
    TopicSubscriber subscriber;


    /** Constructor for the <CODE>TopicRequestor</CODE> class.
     *
     * <P>This implementation assumes the session parameter to be non-transacted,
     * with a delivery mode of either <CODE>AUTO_ACKNOWLEDGE</CODE> or
     * <CODE>DUPS_OK_ACKNOWLEDGE</CODE>.
     *
     * @param session the <CODE>TopicSession</CODE> the topic belongs to
     * @param topic the topic to perform the request/reply call on
     *
     * @exception JMSException if the JMS provider fails to create the
     *                         <CODE>TopicRequestor</CODE> due to some internal
     *                         error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     */

    public
            TopicRequestor(TopicSession session, Topic topic) throws JMSException {
        this.session = session;
        this.topic = topic;
        tempTopic = session.createTemporaryTopic();
        publisher = session.createPublisher(topic);
        subscriber = session.createSubscriber(tempTopic);
    }


    /** Sends a request and waits for a reply. The temporary topic is used for
     * the <CODE>JMSReplyTo</CODE> destination; the first reply is returned,
     * and any following replies are discarded.
     *
     * @param message the message to send
     *
     * @return the reply message
     *
     * @exception JMSException if the JMS provider fails to complete the
     *                         request due to some internal error.
     */

    public Message
            request(Message message) throws JMSException {
        message.setJMSReplyTo(tempTopic);
        publisher.publish(message);
        return (subscriber.receive());
    }


    /** Closes the <CODE>TopicRequestor</CODE> and its session.
     *
     * <P>Since a provider may allocate some resources on behalf of a
     * <CODE>TopicRequestor</CODE> outside the Java virtual machine, clients
     * should close them when they
     * are not needed. Relying on garbage collection to eventually reclaim
     * these resources may not be timely enough.
     *
     * <P>Note that this method closes the <CODE>TopicSession</CODE> object
     * passed to the <CODE>TopicRequestor</CODE> constructor.
     *
     * @exception JMSException if the JMS provider fails to close the
     *                         <CODE>TopicRequestor</CODE> due to some internal
     *                         error.
     */

    public void
            close() throws JMSException {

        // publisher and consumer created by constructor are implicitly closed.
        session.close();
        tempTopic.delete();
    }
}
