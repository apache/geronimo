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

package org.apache.geronimo.crypto.asn1;

import java.io.IOException;

/**
 * DER T61String (also the teletex string)
 */
public class DERT61String
    extends DERObject
    implements DERString
{
    String  string;

    /**
     * return a T61 string from the passed in object.
     *
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static DERT61String getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERT61String)
        {
            return (DERT61String)obj;
        }

        if (obj instanceof ASN1OctetString)
        {
            return new DERT61String(((ASN1OctetString)obj).getOctets());
        }

        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }

        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    /**
     * return an T61 String from a tagged object.
     *
     * @param obj the tagged object holding the object we want
     * @param explicit true if the object is meant to be explicitly
     *              tagged false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *               be converted.
     */
    public static DERT61String getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }

    /**
     * basic constructor - with bytes.
     */
    public DERT61String(
        byte[]   string)
    {
        char[]  cs = new char[string.length];

        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }

        this.string = new String(cs);
    }

    /**
     * basic constructor - with string.
     */
    public DERT61String(
        String   string)
    {
        this.string = string;
    }

    public String getString()
    {
        return string;
    }

    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(T61_STRING, this.getOctets());
    }

    public byte[] getOctets()
    {
        char[]  cs = string.toCharArray();
        byte[]  bs = new byte[cs.length];

        for (int i = 0; i != cs.length; i++)
        {
            bs[i] = (byte)cs[i];
        }

        return bs;
    }

    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERT61String))
        {
            return false;
        }

        return this.getString().equals(((DERT61String)o).getString());
    }

    public int hashCode()
    {
        return this.getString().hashCode();
    }
}
