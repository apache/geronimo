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
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DERBitString;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;
import org.apache.geronimo.crypto.asn1.DERTaggedObject;

/**
 * The DistributionPoint object.
 * <pre>
 * DistributionPoint ::= SEQUENCE {
 *      distributionPoint [0] DistributionPointName OPTIONAL,
 *      reasons           [1] ReasonFlags OPTIONAL,
 *      cRLIssuer         [2] GeneralNames OPTIONAL
 * }
 * </pre>
 */
public class DistributionPoint
    extends ASN1Encodable
{
    DistributionPointName       distributionPoint;
    ReasonFlags                 reasons;
    GeneralNames                cRLIssuer;

    public static DistributionPoint getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DistributionPoint getInstance(
        Object obj)
    {
        if(obj == null || obj instanceof DistributionPoint)
        {
            return (DistributionPoint)obj;
        }

        if(obj instanceof ASN1Sequence)
        {
            return new DistributionPoint((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("Invalid DistributionPoint: " + obj.getClass().getName());
    }

    public DistributionPoint(
        ASN1Sequence seq)
    {
        for (int i = 0; i != seq.size(); i++)
        {
            ASN1TaggedObject    t = (ASN1TaggedObject)seq.getObjectAt(i);
            switch (t.getTagNo())
            {
            case 0:
                distributionPoint = DistributionPointName.getInstance(t, true);
                break;
            case 1:
                reasons = new ReasonFlags(DERBitString.getInstance(t, false));
                break;
            case 2:
                cRLIssuer = GeneralNames.getInstance(t, false);
            }
        }
    }

    public DistributionPoint(
        DistributionPointName distributionPoint,
        ReasonFlags                 reasons,
        GeneralNames            cRLIssuer)
    {
        this.distributionPoint = distributionPoint;
        this.reasons = reasons;
        this.cRLIssuer = cRLIssuer;
    }

    public DistributionPointName getDistributionPoint()
    {
        return distributionPoint;
    }

    public ReasonFlags getReasons()
    {
        return reasons;
    }

    public GeneralNames getCRLIssuer()
    {
        return cRLIssuer;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        if (distributionPoint != null)
        {
            //
            // as this is a CHOICE it must be explicitly tagged
            //
            v.add(new DERTaggedObject(0, distributionPoint));
        }

        if (reasons != null)
        {
            v.add(new DERTaggedObject(false, 1, reasons));
        }

        if (cRLIssuer != null)
        {
            v.add(new DERTaggedObject(false, 2, cRLIssuer));
        }

        return new DERSequence(v);
    }
}
