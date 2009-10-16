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
import java.math.BigInteger;

public class DERInteger
    extends DERObject
{
    byte[]      bytes;

    /**
     * return an integer from the passed in object
     *
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static DERInteger getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERInteger)
        {
            return (DERInteger)obj;
        }

        if (obj instanceof ASN1OctetString)
        {
            return new DERInteger(((ASN1OctetString)obj).getOctets());
        }

        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }

        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    /**
     * return an Integer from a tagged object.
     *
     * @param obj the tagged object holding the object we want
     * @param explicit true if the object is meant to be explicitly
     *              tagged false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *               be converted.
     */
    public static DERInteger getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }

    public DERInteger(
        int         value)
    {
        bytes = BigInteger.valueOf(value).toByteArray();
    }

    public DERInteger(
        BigInteger   value)
    {
        bytes = value.toByteArray();
    }

    public DERInteger(
        byte[]   bytes)
    {
        this.bytes = bytes;
    }

    public BigInteger getValue()
    {
        return new BigInteger(bytes);
    }

    /**
     * in some cases positive values get crammed into a space,
     * that's not quite big enough...
     */
    public BigInteger getPositiveValue()
    {
        return new BigInteger(1, bytes);
    }

    void encode(
        DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(INTEGER, bytes);
    }

    public int hashCode()
    {
         int     value = 0;

         for (int i = 0; i != bytes.length; i++)
         {
             value ^= (bytes[i] & 0xff) << (i % 4);
         }

         return value;
    }

    public boolean equals(
        Object  o)
    {
        if (o == null || !(o instanceof DERInteger))
        {
            return false;
        }

        DERInteger other = (DERInteger)o;

        if (bytes.length != other.bytes.length)
        {
            return false;
        }

        for (int i = 0; i != bytes.length; i++)
        {
            if (bytes[i] != other.bytes[i])
            {
                return false;
            }
        }

        return true;
    }
}
