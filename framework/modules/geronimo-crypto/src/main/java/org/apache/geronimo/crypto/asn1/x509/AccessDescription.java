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
 * The AccessDescription object.
 * <pre>
 * AccessDescription  ::=  SEQUENCE {
 *       accessMethod          OBJECT IDENTIFIER,
 *       accessLocation        GeneralName  }
 * </pre>
 */
public class AccessDescription
    extends ASN1Encodable
{
    DERObjectIdentifier accessMethod = null;
    GeneralName accessLocation = null;

    public static AccessDescription getInstance(
        Object  obj)
    {
        if (obj instanceof AccessDescription)
        {
            return (AccessDescription)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new AccessDescription((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public AccessDescription(
        ASN1Sequence   seq)
    {
        if (seq.size() != 2)
        {
            throw new IllegalArgumentException("wrong number of elements in inner sequence");
        }

        accessMethod = (DERObjectIdentifier)seq.getObjectAt(0);
        accessLocation = GeneralName.getInstance(seq.getObjectAt(1));
    }

    /**
     * create an AccessDescription with the oid and location provided.
     */
    public AccessDescription(
        DERObjectIdentifier oid,
        GeneralName location)
    {
        accessMethod = oid;
        accessLocation = location;
    }

    /**
     *
     * @return the access method.
     */
    public DERObjectIdentifier getAccessMethod()
    {
        return accessMethod;
    }

    /**
     *
     * @return the access location
     */
    public GeneralName getAccessLocation()
    {
        return accessLocation;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector accessDescription  = new ASN1EncodableVector();

        accessDescription.add(accessMethod);
        accessDescription.add(accessLocation);

        return new DERSequence(accessDescription);
    }

    public String toString()
    {
        return ("AccessDescription: Oid(" + this.accessMethod.getId() + ")");
    }
}
