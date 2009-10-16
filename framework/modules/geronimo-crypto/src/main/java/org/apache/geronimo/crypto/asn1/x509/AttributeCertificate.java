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
import org.apache.geronimo.crypto.asn1.DERBitString;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;

public class AttributeCertificate
    extends ASN1Encodable
{
    AttributeCertificateInfo    acinfo;
    AlgorithmIdentifier         signatureAlgorithm;
    DERBitString                signatureValue;

    /**
     * @param obj
     * @return an AttributeCertificate object
     */
    public static AttributeCertificate getInstance(Object obj)
    {
        if (obj instanceof AttributeCertificate)
        {
            return (AttributeCertificate)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new AttributeCertificate((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public AttributeCertificate(
        AttributeCertificateInfo    acinfo,
        AlgorithmIdentifier         signatureAlgorithm,
        DERBitString                signatureValue)
    {
        this.acinfo = acinfo;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = signatureValue;
    }

    public AttributeCertificate(
        ASN1Sequence    seq)
    {
        this.acinfo = AttributeCertificateInfo.getInstance(seq.getObjectAt(0));
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.signatureValue = DERBitString.getInstance(seq.getObjectAt(2));
    }

    public AttributeCertificateInfo getAcinfo()
    {
        return acinfo;
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return signatureAlgorithm;
    }

    public DERBitString getSignatureValue()
    {
        return signatureValue;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *  AttributeCertificate ::= SEQUENCE {
     *       acinfo               AttributeCertificateInfo,
     *       signatureAlgorithm   AlgorithmIdentifier,
     *       signatureValue       BIT STRING
     *  }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(acinfo);
        v.add(signatureAlgorithm);
        v.add(signatureValue);

        return new DERSequence(v);
    }
}
