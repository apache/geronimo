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

import java.util.Enumeration;

import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;
import org.apache.geronimo.crypto.asn1.DERTaggedObject;

public class NameConstraints
    extends ASN1Encodable
{
    ASN1Sequence    permitted, excluded;

    public NameConstraints(
        ASN1Sequence    seq)
    {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1TaggedObject    o = (ASN1TaggedObject)e.nextElement();
            switch (o.getTagNo())
            {
            case 0:
                permitted = ASN1Sequence.getInstance(o, false);
                break;
            case 1:
                excluded = ASN1Sequence.getInstance(o, false);
                break;
            }
        }
    }

    public ASN1Sequence getPermittedSubtrees()
    {
        return permitted;
    }

    public ASN1Sequence getExcludedSubtrees()
    {
        return excluded;
    }

    /*
     * NameConstraints ::= SEQUENCE {
     *      permittedSubtrees       [0]     GeneralSubtrees OPTIONAL,
     *      excludedSubtrees        [1]     GeneralSubtrees OPTIONAL }
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector   v = new ASN1EncodableVector();

        if (permitted != null)
        {
            v.add(new DERTaggedObject(false, 0, permitted));
        }

        if (excluded != null)
        {
            v.add(new DERTaggedObject(false, 1, excluded));
        }

        return new DERSequence(v);
    }
}
