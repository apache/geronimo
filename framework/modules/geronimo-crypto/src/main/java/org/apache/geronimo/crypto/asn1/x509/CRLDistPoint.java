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
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;

public class CRLDistPoint
    extends ASN1Encodable
{
    ASN1Sequence  seq = null;

    public static CRLDistPoint getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static CRLDistPoint getInstance(
        Object  obj)
    {
        if (obj instanceof CRLDistPoint)
        {
            return (CRLDistPoint)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new CRLDistPoint((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public CRLDistPoint(
        ASN1Sequence seq)
    {
        this.seq = seq;
    }

    public CRLDistPoint(
        DistributionPoint[] points)
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        for (int i = 0; i != points.length; i++)
        {
            v.add(points[i]);
        }

        seq = new DERSequence(v);
    }

    /**
     * Return the distribution points making up the sequence.
     *
     * @return DistributionPoint[]
     */
    public DistributionPoint[] getDistributionPoints()
    {
        DistributionPoint[]    dp = new DistributionPoint[seq.size()];

        for (int i = 0; i != seq.size(); i++)
        {
            dp[i] = DistributionPoint.getInstance(seq.getObjectAt(i));
        }

        return dp;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * CRLDistPoint ::= SEQUENCE SIZE {1..MAX} OF DistributionPoint
     * </pre>
     */
    public DERObject toASN1Object()
    {
        return seq;
    }
}
