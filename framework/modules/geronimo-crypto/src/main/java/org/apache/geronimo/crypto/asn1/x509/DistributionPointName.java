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

import org.apache.geronimo.crypto.asn1.ASN1Choice;
import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.ASN1Set;
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DEREncodable;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERTaggedObject;

/**
 * The DistributionPointName object.
 * <pre>
 * DistributionPointName ::= CHOICE {
 *     fullName                 [0] GeneralNames,
 *     nameRelativeToCRLIssuer  [1] RelativeDistinguishedName
 * }
 * </pre>
 */
public class DistributionPointName
    extends ASN1Encodable
    implements ASN1Choice
{
    DEREncodable        name;
    int                 type;

    public static final int FULL_NAME = 0;
    public static final int NAME_RELATIVE_TO_CRL_ISSUER = 1;

    public static DistributionPointName getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1TaggedObject.getInstance(obj, true));
    }

    public static DistributionPointName getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DistributionPointName)
        {
            return (DistributionPointName)obj;
        }
        else if (obj instanceof ASN1TaggedObject)
        {
            return new DistributionPointName((ASN1TaggedObject)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    /*
     * @deprecated use ASN1Encodable
     */
    public DistributionPointName(
        int             type,
        DEREncodable    name)
    {
        this.type = type;
        this.name = name;
    }

    public DistributionPointName(
        int             type,
        ASN1Encodable   name)
    {
        this.type = type;
        this.name = name;
    }

    /**
     * Return the tag number applying to the underlying choice.
     *
     * @return the tag number for this point name.
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * Return the tagged object inside the distribution point name.
     *
     * @return the underlying choice item.
     */
    public ASN1Encodable getName()
    {
        return (ASN1Encodable)name;
    }

    public DistributionPointName(
        ASN1TaggedObject    obj)
    {
        this.type = obj.getTagNo();

        if (type == 0)
        {
            this.name = GeneralNames.getInstance(obj, false);
        }
        else
        {
            this.name = ASN1Set.getInstance(obj, false);
        }
    }

    public DERObject toASN1Object()
    {
        return new DERTaggedObject(false, type, name);
    }
}
