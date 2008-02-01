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

package org.apache.geronimo.crypto.asn1.x509;

import java.math.BigInteger;

import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DERInteger;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;
import org.apache.geronimo.crypto.asn1.DERTaggedObject;

public class GeneralSubtree
    extends ASN1Encodable
{
    private GeneralName  base;
    private DERInteger minimum;
    private DERInteger maximum;

    public GeneralSubtree(
        ASN1Sequence seq)
    {
        base = GeneralName.getInstance(seq.getObjectAt(0));

        switch (seq.size())
        {
        case 1:
            break;
        case 2:
            ASN1TaggedObject o = (ASN1TaggedObject)seq.getObjectAt(1);
            switch (o.getTagNo())
            {
            case 0 :
                minimum = DERInteger.getInstance(o, false);
                break;
            case 1 :
                maximum = DERInteger.getInstance(o, false);
                break;
            default:
                throw new IllegalArgumentException("Bad tag number: " + o.getTagNo());
            }
            break;
        case 3 :
            minimum = DERInteger.getInstance((ASN1TaggedObject)seq.getObjectAt(1), false);
            maximum = DERInteger.getInstance((ASN1TaggedObject)seq.getObjectAt(2), false);
            break;
        default:
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
    }

    public static GeneralSubtree getInstance(
        ASN1TaggedObject    o,
        boolean             explicit)
    {
        return new GeneralSubtree(ASN1Sequence.getInstance(o, explicit));
    }

    public static GeneralSubtree getInstance(
        Object obj)
    {
        if (obj == null)
        {
            return null;
        }

        if (obj instanceof GeneralSubtree)
        {
            return (GeneralSubtree)obj;
        }

        return new GeneralSubtree(ASN1Sequence.getInstance(obj));
    }

    public GeneralName getBase()
    {
        return base;
    }

    public BigInteger getMinimum()
    {
        if (minimum == null)
        {
            return BigInteger.valueOf(0);
        }

        return minimum.getValue();
    }

    public BigInteger getMaximum()
    {
        if (maximum == null)
        {
            return null;
        }

        return maximum.getValue();
    }

    /*
     * GeneralSubtree ::= SEQUENCE {
     *      base                    GeneralName,
     *      minimum         [0]     BaseDistance DEFAULT 0,
     *      maximum         [1]     BaseDistance OPTIONAL }
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(base);

        if (minimum != null)
        {
            v.add(new DERTaggedObject(false, 0, minimum));
        }

        if (maximum != null)
        {
            v.add(new DERTaggedObject(false, 1, maximum));
        }

        return new DERSequence(v);
    }
}
