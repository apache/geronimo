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

package org.apache.geronimo.crypto.asn1.sec;

import java.math.BigInteger;
import org.apache.geronimo.crypto.asn1.*;

/**
 * the elliptic curve private key object from SEC 1
 */
public class ECPrivateKeyStructure
    extends ASN1Encodable
{
    private ASN1Sequence  seq;

    public ECPrivateKeyStructure(
        ASN1Sequence  seq)
    {
        this.seq = seq;
    }

    public ECPrivateKeyStructure(
        BigInteger  key)
    {
        byte[]  bytes = key.toByteArray();

        if (bytes[0] == 0)
        {
            byte[]  tmp = new byte[bytes.length - 1];

            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }

        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new DERInteger(1));
        v.add(new DEROctetString(bytes));

        seq = new DERSequence(v);
    }

    public BigInteger getKey()
    {
        ASN1OctetString  octs = (ASN1OctetString)seq.getObjectAt(1);

        BigInteger  k = new BigInteger(1, octs.getOctets());

        return k;
    }

    public DERObject toASN1Object()
    {
        return seq;
    }
}
