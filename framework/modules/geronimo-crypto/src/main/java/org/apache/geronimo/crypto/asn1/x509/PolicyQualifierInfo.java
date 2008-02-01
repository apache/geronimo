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
import org.apache.geronimo.crypto.asn1.DEREncodable;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERIA5String;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DERSequence;

/**
 * Policy qualifiers, used in the X509V3 CertificatePolicies
 * extension.
 *
 * <pre>
 *   PolicyQualifierInfo ::= SEQUENCE {
 *       policyQualifierId  PolicyQualifierId,
 *       qualifier          ANY DEFINED BY policyQualifierId }
 * </pre>
 */
public class PolicyQualifierInfo
    extends ASN1Encodable
{
   DERObjectIdentifier policyQualifierId;
   DEREncodable qualifier;

   /**
    * Creates a new <code>PolicyQualifierInfo</code> instance.
    *
    * @param policyQualifierId a <code>PolicyQualifierId</code> value
    * @param qualifier the qualifier, defined by the above field.
    */
   public PolicyQualifierInfo (DERObjectIdentifier policyQualifierId,
                               DEREncodable qualifier)
   {
      this.policyQualifierId = policyQualifierId;
      this.qualifier = qualifier;
   }

   /**
    * Creates a new <code>PolicyQualifierInfo</code> containing a
    * cPSuri qualifier.
    *
    * @param cps the CPS (certification practice statement) uri as a
    * <code>String</code>.
    */
   public PolicyQualifierInfo (String cps)
   {
      policyQualifierId = PolicyQualifierId.id_qt_cps;
      qualifier = new DERIA5String (cps);
   }

   /**
    * Creates a new <code>PolicyQualifierInfo</code> instance.
    *
    * @param as <code>PolicyQualifierInfo</code> X509 structure
    * encoded as an ASN1Sequence.
    */
   public PolicyQualifierInfo (ASN1Sequence as)
   {
        policyQualifierId = (DERObjectIdentifier) as.getObjectAt(0);
        qualifier = as.getObjectAt(1);
    }

   public static PolicyQualifierInfo getInstance (Object as)
   {
        if (as instanceof PolicyQualifierInfo)
        {
            return (PolicyQualifierInfo)as;
        }
        else if (as instanceof ASN1Sequence)
        {
            return new PolicyQualifierInfo((ASN1Sequence)as);
        }

        throw new IllegalArgumentException("unknown object in getInstance.");
   }

   /**
    * Returns a DER-encodable representation of this instance.
    *
    * @return a <code>DERObject</code> value
    */
   public DERObject toASN1Object()
   {
      ASN1EncodableVector dev = new ASN1EncodableVector();
      dev.add(policyQualifierId);
      dev.add(qualifier);

      return new DERSequence(dev);
   }
}
