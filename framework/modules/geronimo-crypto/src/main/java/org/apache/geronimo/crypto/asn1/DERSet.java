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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 * A DER encoded set object
 */
public class DERSet
    extends ASN1Set
{
    /**
     * create an empty set
     */
    public DERSet()
    {
    }

    /**
     * @param obj - a single object that makes up the set.
     */
    public DERSet(
        DEREncodable   obj)
    {
        this.addObject(obj);
    }

    /**
     * @param v - a vector of objects making up the set.
     */
    public DERSet(
        DEREncodableVector   v)
    {
        this(v, true);
    }

    /**
     * create a set from an array of objects.
     */
    public DERSet(
        ASN1Encodable[]   a)
    {
        for (int i = 0; i != a.length; i++)
        {
            this.addObject(a[i]);
        }

        this.sort();
    }

    /**
     * @param v - a vector of objects making up the set.
     */
    DERSet(
        DEREncodableVector   v,
        boolean              needsSorting)
    {
        for (int i = 0; i != v.size(); i++)
        {
            this.addObject(v.get(i));
        }

        if (needsSorting)
        {
            this.sort();
        }
    }

    /*
     * A note on the implementation:
     * <p>
     * As DER requires the constructed, definite-length model to
     * be used for structured types, this varies slightly from the
     * ASN.1 descriptions given. Rather than just outputing SET,
     * we also have to specify CONSTRUCTED, and the objects length.
     */
    void encode(
        DEROutputStream out)
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        Enumeration             e = this.getObjects();

        while (e.hasMoreElements())
        {
            Object    obj = e.nextElement();

            dOut.writeObject(obj);
        }

        dOut.close();

        byte[]  bytes = bOut.toByteArray();

        out.writeEncoded(SET | CONSTRUCTED, bytes);
    }
}
