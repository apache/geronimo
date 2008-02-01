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
import java.util.Enumeration;

/**
 * BER TaggedObject - in ASN.1 nottation this is any object proceeded by
 * a [n] where n is some number - these are assume to follow the construction
 * rules (as with sequences).
 */
public class BERTaggedObject
    extends DERTaggedObject
{
    /**
     * @param tagNo the tag number for this object.
     * @param obj the tagged object.
     */
    public BERTaggedObject(
        int             tagNo,
        DEREncodable    obj)
    {
        super(tagNo, obj);
    }

    /**
     * @param explicit true if an explicitly tagged object.
     * @param tagNo the tag number for this object.
     * @param obj the tagged object.
     */
    public BERTaggedObject(
        boolean         explicit,
        int             tagNo,
        DEREncodable    obj)
    {
        super(explicit, tagNo, obj);
    }

    /**
     * create an implicitly tagged object that contains a zero
     * length sequence.
     */
    public BERTaggedObject(
        int             tagNo)
    {
        super(false, tagNo, new BERSequence());
    }

    void encode(
        DEROutputStream  out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(CONSTRUCTED | TAGGED | tagNo);
            out.write(0x80);

            if (!empty)
            {
                if (!explicit)
                {
                    if (obj instanceof ASN1OctetString)
                    {
                        Enumeration  e;

                        if (obj instanceof BERConstructedOctetString)
                        {
                            e = ((BERConstructedOctetString)obj).getObjects();
                        }
                        else
                        {
                            ASN1OctetString             octs = (ASN1OctetString)obj;
                            BERConstructedOctetString   berO = new BERConstructedOctetString(octs.getOctets());

                            e = berO.getObjects();
                        }

                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else if (obj instanceof ASN1Sequence)
                    {
                        Enumeration  e = ((ASN1Sequence)obj).getObjects();

                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else if (obj instanceof ASN1Set)
                    {
                        Enumeration  e = ((ASN1Set)obj).getObjects();

                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else
                    {
                        throw new RuntimeException("not implemented: " + obj.getClass().getName());
                    }
                }
                else
                {
                    out.writeObject(obj);
                }
            }

            out.write(0x00);
            out.write(0x00);
        }
        else
        {
            super.encode(out);
        }
    }
}
