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
public interface MapMessage extends Message {
    boolean getBoolean(String name) throws JMSException;

    byte getByte(String name) throws JMSException;

    short getShort(String name) throws JMSException;

    char getChar(String name) throws JMSException;

    int getInt(String name) throws JMSException;

    long getLong(String name) throws JMSException;

    float getFloat(String name) throws JMSException;

    double getDouble(String name) throws JMSException;

    String getString(String name) throws JMSException;

    byte[] getBytes(String name) throws JMSException;

    Object getObject(String name) throws JMSException;

    Enumeration getMapNames() throws JMSException;

    void setBoolean(String name, boolean value) throws JMSException;

    void setByte(String name, byte value) throws JMSException;

    void setShort(String name, short value) throws JMSException;

    void setChar(String name, char value) throws JMSException;

    void setInt(String name, int value) throws JMSException;

    void setLong(String name, long value) throws JMSException;

    void setFloat(String name, float value) throws JMSException;

    void setDouble(String name, double value) throws JMSException;

    void setString(String name, String value) throws JMSException;

    void setBytes(String name, byte[] value) throws JMSException;

    void setBytes(String name, byte[] value, int offset, int length)
        throws JMSException;

    void setObject(String name, Object value) throws JMSException;

    boolean itemExists(String name) throws JMSException;
}
