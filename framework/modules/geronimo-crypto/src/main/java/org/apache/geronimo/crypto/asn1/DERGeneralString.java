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

public class DERGeneralString
    extends DERObject implements DERString
{
    private String string;

    public static DERGeneralString getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof DERGeneralString)
        {
            return (DERGeneralString) obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERGeneralString(((ASN1OctetString) obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject) obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: "
                + obj.getClass().getName());
    }

    public static DERGeneralString getInstance(
        ASN1TaggedObject obj,
        boolean explicit)
    {
        return getInstance(obj.getObject());
    }

    public DERGeneralString(byte[] string)
    {
        char[] cs = new char[string.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }
        this.string = new String(cs);
    }

    public DERGeneralString(String string)
    {
        this.string = string;
    }

    public String getString()
    {
        return string;
    }

    public byte[] getOctets()
    {
        char[] cs = string.toCharArray();
        byte[] bs = new byte[cs.length];
        for (int i = 0; i != cs.length; i++)
        {
            bs[i] = (byte) cs[i];
        }
        return bs;
    }

    void encode(DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(GENERAL_STRING, this.getOctets());
    }

    public int hashCode()
    {
        return this.getString().hashCode();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof DERGeneralString))
        {
            return false;
        }
        DERGeneralString s = (DERGeneralString) o;
        return this.getString().equals(s.getString());
    }
}
