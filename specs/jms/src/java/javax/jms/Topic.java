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


/** A <CODE>Topic</CODE> object encapsulates a provider-specific topic name.
 * It is the way a client specifies the identity of a topic to JMS API methods.
 * For those methods that use a <CODE>Destination</CODE> as a parameter, a
 * <CODE>Topic</CODE> object may used as an argument . For
 * example, a Topic can be used to create a <CODE>MessageConsumer</CODE>
 * and a <CODE>MessageProducer</CODE>
 * by calling:
 *<UL>
 *<LI> <CODE>Session.CreateConsumer(Destination destination)</CODE>
 *<LI> <CODE>Session.CreateProducer(Destination destination)</CODE>
 *
 *</UL>
 *
 * <P>Many publish/subscribe (pub/sub) providers group topics into hierarchies
 * and provide various options for subscribing to parts of the hierarchy. The
 * JMS API places no restriction on what a <CODE>Topic</CODE> object
 * represents. It may be a leaf in a topic hierarchy, or it may be a larger
 * part of the hierarchy.
 *
 * <P>The organization of topics and the granularity of subscriptions to
 * them is an important part of a pub/sub application's architecture. The JMS
 * API
 * does not specify a policy for how this should be done. If an application
 * takes advantage of a provider-specific topic-grouping mechanism, it
 * should document this. If the application is installed using a different
 * provider, it is the job of the administrator to construct an equivalent
 * topic architecture and create equivalent <CODE>Topic</CODE> objects.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/08/16 02:29:58 $
 * @author      Mark Hapner
 * @author      Rich Burridge
 * @author      Kate Stout
 *
 * @see        Session#createConsumer(Destination)
 * @see        Session#createProducer(Destination)
 * @see        javax.jms.TopicSession#createTopic(String)
 */

public interface Topic extends Destination {

    /** Gets the name of this topic.
     *
     * <P>Clients that depend upon the name are not portable.
     *
     * @return the topic name
     *
     * @exception JMSException if the JMS provider implementation of
     *                         <CODE>Topic</CODE> fails to return the topic
     *                         name due to some internal
     *                         error.
     */

    String getTopicName() throws JMSException;


    /** Returns a string representation of this object.
     *
     * @return the provider-specific identity values for this topic
     */

    String toString();
}
