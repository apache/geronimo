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

/**
 * @version $Rev$ $Date$
 */
public interface StreamMessage extends Message {
    boolean readBoolean() throws JMSException;

    byte readByte() throws JMSException;

    short readShort() throws JMSException;

    char readChar() throws JMSException;

    int readInt() throws JMSException;

    long readLong() throws JMSException;

    float readFloat() throws JMSException;

    double readDouble() throws JMSException;

    String readString() throws JMSException;

    int readBytes(byte[] value) throws JMSException;

    Object readObject() throws JMSException;

    void writeBoolean(boolean value) throws JMSException;

    void writeByte(byte value) throws JMSException;

    void writeShort(short value) throws JMSException;

    void writeChar(char value) throws JMSException;

    void writeInt(int value) throws JMSException;

    void writeLong(long value) throws JMSException;

    void writeFloat(float value) throws JMSException;

    void writeDouble(double value) throws JMSException;

    void writeString(String value) throws JMSException;

    void writeBytes(byte[] value) throws JMSException;

    void writeBytes(byte[] value, int offset, int length) throws JMSException;

    void writeObject(Object value) throws JMSException;

    void reset() throws JMSException;
}
