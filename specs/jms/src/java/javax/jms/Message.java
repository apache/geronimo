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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.jms;

import java.util.Enumeration;

/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 10:00:03 $
 */
public interface Message {

    static final int DEFAULT_DELIVERY_MODE = DeliveryMode.PERSISTENT;

    static final int DEFAULT_PRIORITY = 4;

    static final long DEFAULT_TIME_TO_LIVE = 0;

    String getJMSMessageID() throws JMSException;

    void setJMSMessageID(String id) throws JMSException;

    long getJMSTimestamp() throws JMSException;

    void setJMSTimestamp(long timestamp) throws JMSException;

    byte[] getJMSCorrelationIDAsBytes() throws JMSException;

    void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException;

    void setJMSCorrelationID(String correlationID) throws JMSException;

    String getJMSCorrelationID() throws JMSException;

    Destination getJMSReplyTo() throws JMSException;

    void setJMSReplyTo(Destination replyTo) throws JMSException;

    Destination getJMSDestination() throws JMSException;

    void setJMSDestination(Destination destination) throws JMSException;

    int getJMSDeliveryMode() throws JMSException;

    void setJMSDeliveryMode(int deliveryMode) throws JMSException;

    boolean getJMSRedelivered() throws JMSException;

    void setJMSRedelivered(boolean redelivered) throws JMSException;

    String getJMSType() throws JMSException;

    void setJMSType(String type) throws JMSException;

    long getJMSExpiration() throws JMSException;

    void setJMSExpiration(long expiration) throws JMSException;

    int getJMSPriority() throws JMSException;

    void setJMSPriority(int priority) throws JMSException;

    void clearProperties() throws JMSException;

    boolean propertyExists(String name) throws JMSException;

    boolean getBooleanProperty(String name) throws JMSException;

    byte getByteProperty(String name) throws JMSException;

    short getShortProperty(String name) throws JMSException;

    int getIntProperty(String name) throws JMSException;

    long getLongProperty(String name) throws JMSException;

    float getFloatProperty(String name) throws JMSException;

    double getDoubleProperty(String name) throws JMSException;

    String getStringProperty(String name) throws JMSException;

    Object getObjectProperty(String name) throws JMSException;

    Enumeration getPropertyNames() throws JMSException;

    void setBooleanProperty(String name, boolean value) throws JMSException;

    void setByteProperty(String name, byte value) throws JMSException;

    void setShortProperty(String name, short value) throws JMSException;

    void setIntProperty(String name, int value) throws JMSException;

    void setLongProperty(String name, long value) throws JMSException;

    void setFloatProperty(String name, float value) throws JMSException;

    void setDoubleProperty(String name, double value) throws JMSException;

    void setStringProperty(String name, String value) throws JMSException;

    void setObjectProperty(String name, Object value) throws JMSException;

    void acknowledge() throws JMSException;

    void clearBody() throws JMSException;
}
