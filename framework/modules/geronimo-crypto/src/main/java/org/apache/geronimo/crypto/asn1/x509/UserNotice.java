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
import org.apache.geronimo.crypto.asn1.DERSequence;

/**
 * <code>UserNotice</code> class, used in
 * <code>CertificatePolicies</code> X509 extensions (in policy
 * qualifiers).
 * <pre>
 * UserNotice ::= SEQUENCE {
 *      noticeRef        NoticeReference OPTIONAL,
 *      explicitText     DisplayText OPTIONAL}
 *
 * </pre>
 *
 * @see PolicyQualifierId
 * @see PolicyInformation
 */
public class UserNotice
    extends ASN1Encodable
{
    NoticeReference noticeRef;
    DisplayText     explicitText;

    /**
     * Creates a new <code>UserNotice</code> instance.
     *
     * @param noticeRef a <code>NoticeReference</code> value
     * @param explicitText a <code>DisplayText</code> value
     */
    public UserNotice(
        NoticeReference noticeRef,
        DisplayText explicitText)
    {
        this.noticeRef = noticeRef;
        this.explicitText = explicitText;
    }

    /**
     * Creates a new <code>UserNotice</code> instance.
     *
     * @param noticeRef a <code>NoticeReference</code> value
     * @param str the explicitText field as a String.
     */
    public UserNotice(
        NoticeReference noticeRef,
        String str)
    {
        this.noticeRef = noticeRef;
        this.explicitText = new DisplayText(str);
    }

   /**
    * Creates a new <code>UserNotice</code> instance.
    * <p>Useful from reconstructing a <code>UserNotice</code> instance
    * from its encodable/encoded form.
    *
    * @param as an <code>ASN1Sequence</code> value obtained from either
    * calling @{link toASN1Object()} for a <code>UserNotice</code>
    * instance or from parsing it from a DER-encoded stream.
    */
   public UserNotice(
       ASN1Sequence as)
   {
       if (as.size() == 2)
       {
           noticeRef = NoticeReference.getInstance(as.getObjectAt(0));
           explicitText = DisplayText.getInstance(as.getObjectAt(1));
       }
       else if (as.size() == 1)
       {
           if (as.getObjectAt(0).getDERObject() instanceof ASN1Sequence)
           {
               noticeRef = NoticeReference.getInstance(as.getObjectAt(0));
           }
           else
           {
               explicitText = DisplayText.getInstance(as.getObjectAt(0));
           }
       }
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector av = new ASN1EncodableVector();

        if (noticeRef != null)
        {
            av.add(noticeRef);
        }

        if (explicitText != null)
        {
            av.add(explicitText);
        }

        return new DERSequence(av);
    }
}
