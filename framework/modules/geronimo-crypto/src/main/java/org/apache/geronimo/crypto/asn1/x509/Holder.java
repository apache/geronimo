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
import org.apache.geronimo.crypto.asn1.DERTaggedObject;

/**
 * The Holder object.
 * <pre>
 *  Holder ::= SEQUENCE {
 *        baseCertificateID   [0] IssuerSerial OPTIONAL,
 *                 -- the issuer and serial number of
 *                 -- the holder's Public Key Certificate
 *        entityName          [1] GeneralNames OPTIONAL,
 *                 -- the name of the claimant or role
 *        objectDigestInfo    [2] ObjectDigestInfo OPTIONAL
 *                 -- used to directly authenticate the holder,
 *                 -- for example, an executable
 *  }
 * </pre>
 */
public class Holder
    extends ASN1Encodable
{
    IssuerSerial        baseCertificateID;
    GeneralNames        entityName;
    ObjectDigestInfo    objectDigestInfo;

    public static Holder getInstance(
            Object  obj)
    {
        if (obj instanceof Holder)
        {
            return (Holder)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new Holder((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public Holder(
        ASN1Sequence    seq)
    {
        for (int i = 0; i != seq.size(); i++)
        {
            ASN1TaggedObject    tObj = (ASN1TaggedObject)seq.getObjectAt(i);

            switch (tObj.getTagNo())
            {
            case 0:
                baseCertificateID = IssuerSerial.getInstance(tObj, false);
                break;
            case 1:
                entityName = GeneralNames.getInstance(tObj, false);
                break;
            case 2:
                objectDigestInfo = ObjectDigestInfo.getInstance(tObj, false);
                break;
            default:
                throw new IllegalArgumentException("unknown tag in Holder");
            }
        }
    }

    public Holder(
        IssuerSerial    baseCertificateID)
    {
        this.baseCertificateID = baseCertificateID;
    }

    public Holder(
        GeneralNames    entityName)
    {
        this.entityName = entityName;
    }

    public IssuerSerial getBaseCertificateID()
    {
        return baseCertificateID;
    }

    public GeneralNames getEntityName()
    {
        return entityName;
    }

    public ObjectDigestInfo getObjectDigestInfo()
    {
        return objectDigestInfo;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        if (baseCertificateID != null)
        {
            v.add(new DERTaggedObject(false, 0, baseCertificateID));
        }

        if (entityName != null)
        {
            v.add(new DERTaggedObject(false, 1, entityName));
        }

        if (objectDigestInfo != null)
        {
            v.add(new DERTaggedObject(false, 2, objectDigestInfo));
        }

        return new DERSequence(v);
    }
}
