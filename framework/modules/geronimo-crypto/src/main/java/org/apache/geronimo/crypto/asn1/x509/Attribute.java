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

import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1Set;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DERSequence;

public class Attribute
    extends ASN1Encodable
{
    private DERObjectIdentifier attrType;
    private ASN1Set             attrValues;

    /**
     * return an Attribute object from the given object.
     *
     * @param o the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static Attribute getInstance(
        Object o)
    {
        if (o == null || o instanceof Attribute)
        {
            return (Attribute)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new Attribute((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public Attribute(
        ASN1Sequence seq)
    {
        attrType = (DERObjectIdentifier)seq.getObjectAt(0);
        attrValues = (ASN1Set)seq.getObjectAt(1);
    }

    public Attribute(
        DERObjectIdentifier attrType,
        ASN1Set             attrValues)
    {
        this.attrType = attrType;
        this.attrValues = attrValues;
    }

    public DERObjectIdentifier getAttrType()
    {
        return attrType;
    }

    public ASN1Set getAttrValues()
    {
        return attrValues;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * Attribute ::= SEQUENCE {
     *     attrType OBJECT IDENTIFIER,
     *     attrValues SET OF AttributeValue
     * }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(attrType);
        v.add(attrValues);

        return new DERSequence(v);
    }
}
