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

import java.util.Enumeration;

/**
 * @version $Revision: 1.2 $ $Date: 2003/08/24 06:26:46 $
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
