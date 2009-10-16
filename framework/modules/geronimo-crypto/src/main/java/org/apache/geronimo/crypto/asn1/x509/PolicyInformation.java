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

import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DERSequence;

public class PolicyInformation
    extends ASN1Encodable
{
    private DERObjectIdentifier   policyIdentifier;
    private ASN1Sequence          policyQualifiers;

    public PolicyInformation(
        ASN1Sequence seq)
    {
        policyIdentifier = (DERObjectIdentifier)seq.getObjectAt(0);

        if (seq.size() > 1)
        {
            policyQualifiers = (ASN1Sequence)seq.getObjectAt(1);
        }
    }

    public PolicyInformation(
        DERObjectIdentifier policyIdentifier)
    {
        this.policyIdentifier = policyIdentifier;
    }

    public PolicyInformation(
        DERObjectIdentifier policyIdentifier,
        ASN1Sequence        policyQualifiers)
    {
        this.policyIdentifier = policyIdentifier;
        this.policyQualifiers = policyQualifiers;
    }

    public static PolicyInformation getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof PolicyInformation)
        {
            return (PolicyInformation)obj;
        }

        return new PolicyInformation(ASN1Sequence.getInstance(obj));
    }

    public DERObjectIdentifier getPolicyIdentifier()
    {
        return policyIdentifier;
    }

    public ASN1Sequence getPolicyQualifiers()
    {
        return policyQualifiers;
    }

    /*
     * PolicyInformation ::= SEQUENCE {
     *      policyIdentifier   CertPolicyId,
     *      policyQualifiers   SEQUENCE SIZE (1..MAX) OF
     *              PolicyQualifierInfo OPTIONAL }
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(policyIdentifier);

        if (policyQualifiers != null)
        {
            v.add(policyQualifiers);
        }

        return new DERSequence(v);
    }
}
