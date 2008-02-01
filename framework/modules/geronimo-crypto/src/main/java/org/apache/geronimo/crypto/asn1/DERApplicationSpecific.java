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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Base class for an application specific object
 */
public class DERApplicationSpecific
    extends DERObject
{
    private int       tag;
    private byte[]    octets;

    public DERApplicationSpecific(
        int        tag,
        byte[]    octets)
    {
        this.tag = tag;
        this.octets = octets;
    }

    public DERApplicationSpecific(
        int                  tag,
        DEREncodable         object)
        throws IOException
    {
        this.tag = tag | DERTags.CONSTRUCTED;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DEROutputStream dos = new DEROutputStream(baos);

        dos.writeObject(object);

        this.octets = baos.toByteArray();
    }

    public boolean isConstructed()
    {
        return (tag & DERTags.CONSTRUCTED) != 0;
    }

    public byte[] getContents()
    {
        return octets;
    }

    public int getApplicationTag()
    {
        return tag & 0x1F;
    }

    public DERObject getObject()
        throws IOException
    {
        return new ASN1InputStream(new ByteArrayInputStream(getContents())).readObject();
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.crypto.asn1.DERObject#encode(org.apache.geronimo.crypto.asn1.DEROutputStream)
     */
    void encode(DEROutputStream out) throws IOException
    {
        out.writeEncoded(DERTags.APPLICATION | tag, octets);
    }

    public boolean equals(
            Object o)
    {
        if ((o == null) || !(o instanceof DERApplicationSpecific))
        {
            return false;
        }

        DERApplicationSpecific other = (DERApplicationSpecific)o;

        if (tag != other.tag)
        {
            return false;
        }

        if (octets.length != other.octets.length)
        {
            return false;
        }

        for (int i = 0; i < octets.length; i++)
        {
            if (octets[i] != other.octets[i])
            {
                return false;
            }
        }

        return true;
    }

    public int hashCode()
    {
        byte[]  b = this.getContents();
        int     value = 0;

        for (int i = 0; i != b.length; i++)
        {
            value ^= (b[i] & 0xff) << (i % 4);
        }

        return value ^ this.getApplicationTag();
    }
}
