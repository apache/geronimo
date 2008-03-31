/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jms.test.mdb;

import java.text.MessageFormat;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

import org.apache.geronimo.jms.test.mdb.exception.ProcessingException;
import org.apache.geronimo.jms.test.mdb.to.SimpleTransferObject;
import org.apache.geronimo.jms.test.mdb.to.TransferObject;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
@MessageDriven(name = "SimpleMessageReceiver",
        activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "MRRequests"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "InitialRedeliveryDelay", propertyValue = "30"),
        @ActivationConfigProperty(propertyName = "MaximumRedeliveries", propertyValue = "9999"),
                //prefetch size >=10 works for 100 total messages
        @ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "1")
                })
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
//No tx works...
//@TransactionManagement(value = TransactionManagementType.BEAN)
public class SimpleMessageReceiver extends MessageReceiver implements MessageListener {

    /**
     * Context injected by Container
     */
    @Resource
    private MessageDrivenContext mdc = null;

    /**
     * Injected connection factory
     */
    @Resource(name = "MRConnectionFactory")
    private ConnectionFactory jmsConnFactory = null;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.geronimo.jms.test.mdb.MessageReceiver#getMessageDrivenContext()
     */
    @Override
    public MessageDrivenContext getMessageDrivenContext() {
        return mdc;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.geronimo.jms.test.mdb.MessageReceiver#getConnectionFactory()
     */
    @Override
    public ConnectionFactory getConnectionFactory() {
        return jmsConnFactory;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.geronimo.jms.test.mdb.MessageReceiver#processMessage(TransferObject)
     */
    @Override
    protected TransferObject processMessage(TransferObject obj) throws ProcessingException {
        if (obj == null) {
            throw new ProcessingException("Object received in message is null");
        } else if (!SimpleTransferObject.class.isInstance(obj)) {
            throw new ProcessingException(MessageFormat.format("Invalid class of object {0} included in received message", obj.getClass()));
        } else {
            SimpleTransferObject to = (SimpleTransferObject) obj;
            to.markProcessed();
            return to;
        }
    }
}
