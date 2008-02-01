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

/**
 * PKIX RFC-2459
 *
 * The X.509 v2 CRL syntax is as follows.  For signature calculation,
 * the data that is to be signed is ASN.1 DER encoded.
 *
 * <pre>
 * CertificateList  ::=  SEQUENCE  {
 *      tbsCertList          TBSCertList,
 *      signatureAlgorithm   AlgorithmIdentifier,
 *      signatureValue       BIT STRING  }
 * </pre>
 */
public class CertificateList
    extends ASN1Encodable
{
    TBSCertList            tbsCertList;
    AlgorithmIdentifier    sigAlgId;
    DERBitString           sig;

    public static CertificateList getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static CertificateList getInstance(
        Object  obj)
    {
        if (obj instanceof CertificateList)
        {
            return (CertificateList)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new CertificateList((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public CertificateList(
        ASN1Sequence seq)
    {
        if (seq.size() == 3)
        {
            tbsCertList = TBSCertList.getInstance(seq.getObjectAt(0));
            sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
            sig = (DERBitString)seq.getObjectAt(2);
        }
        else
        {
            throw new IllegalArgumentException("sequence wrong size for CertificateList");
        }
    }

    public TBSCertList getTBSCertList()
    {
        return tbsCertList;
    }

    public TBSCertList.CRLEntry[] getRevokedCertificates()
    {
        return tbsCertList.getRevokedCertificates();
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return sigAlgId;
    }

    public DERBitString getSignature()
    {
        return sig;
    }

    public int getVersion()
    {
        return tbsCertList.getVersion();
    }

    public X509Name getIssuer()
    {
        return tbsCertList.getIssuer();
    }

    public Time getThisUpdate()
    {
        return tbsCertList.getThisUpdate();
    }

    public Time getNextUpdate()
    {
        return tbsCertList.getNextUpdate();
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCertList);
        v.add(sigAlgId);
        v.add(sig);

        return new DERSequence(v);
    }
}
