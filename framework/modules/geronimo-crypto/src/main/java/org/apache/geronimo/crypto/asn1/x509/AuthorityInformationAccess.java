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
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DERSequence;

/**
 * The AuthorityInformationAccess object.
 * <pre>
 * id-pe-authorityInfoAccess OBJECT IDENTIFIER ::= { id-pe 1 }
 *
 * AuthorityInfoAccessSyntax  ::=
 *      SEQUENCE SIZE (1..MAX) OF AccessDescription
 * AccessDescription  ::=  SEQUENCE {
 *       accessMethod          OBJECT IDENTIFIER,
 *       accessLocation        GeneralName  }
 *
 * id-ad OBJECT IDENTIFIER ::= { id-pkix 48 }
 * id-ad-caIssuers OBJECT IDENTIFIER ::= { id-ad 2 }
 * id-ad-ocsp OBJECT IDENTIFIER ::= { id-ad 1 }
 * </pre>
 */
public class AuthorityInformationAccess
    extends ASN1Encodable
{
    private AccessDescription[]    descriptions;

    public static AuthorityInformationAccess getInstance(
        Object  obj)
    {
        if (obj instanceof AuthorityInformationAccess)
        {
            return (AuthorityInformationAccess)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new AuthorityInformationAccess((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public AuthorityInformationAccess(
        ASN1Sequence   seq)
    {
        descriptions = new AccessDescription[seq.size()];

        for (int i = 0; i != seq.size(); i++)
        {
            descriptions[i] = AccessDescription.getInstance(seq.getObjectAt(i));
        }
    }

    /**
     * create an AuthorityInformationAccess with the oid and location provided.
     */
    public AuthorityInformationAccess(
        DERObjectIdentifier oid,
        GeneralName location)
    {
        descriptions = new AccessDescription[1];

        descriptions[0] = new AccessDescription(oid, location);
    }


    /**
     *
     * @return the access descriptions contained in this object.
     */
    public AccessDescription[] getAccessDescriptions()
    {
        return descriptions;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector vec = new ASN1EncodableVector();

        for (int i = 0; i != descriptions.length; i++)
        {
            vec.add(descriptions[i]);
        }

        return new DERSequence(vec);
    }

    public String toString()
    {
        return ("AuthorityInformationAccess: Oid(" + this.descriptions[0].getAccessMethod().getId() + ")");
    }
}
