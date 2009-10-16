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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;

/**
 * The extendedKeyUsage object.
 * <pre>
 *      extendedKeyUsage ::= SEQUENCE SIZE (1..MAX) OF KeyPurposeId
 * </pre>
 */
public class ExtendedKeyUsage
    extends ASN1Encodable
{
    Hashtable     usageTable = new Hashtable();
    ASN1Sequence  seq;

    public static ExtendedKeyUsage getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static ExtendedKeyUsage getInstance(
        Object obj)
    {
        if(obj == null || obj instanceof ExtendedKeyUsage)
        {
            return (ExtendedKeyUsage)obj;
        }

        if(obj instanceof ASN1Sequence)
        {
            return new ExtendedKeyUsage((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("Invalid ExtendedKeyUsage: " + obj.getClass().getName());
    }

    public ExtendedKeyUsage(
        KeyPurposeId  usage)
    {
        this.seq = new DERSequence(usage);

        this.usageTable.put(usage, usage);
    }

    public ExtendedKeyUsage(
        ASN1Sequence  seq)
    {
        this.seq = seq;

        Enumeration e = seq.getObjects();

        while (e.hasMoreElements())
        {
            Object  o = e.nextElement();

            this.usageTable.put(o, o);
        }
    }

    public ExtendedKeyUsage(
        Vector  usages)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        Enumeration         e = usages.elements();

        while (e.hasMoreElements())
        {
            DERObject  o = (DERObject)e.nextElement();

            v.add(o);
            this.usageTable.put(o, o);
        }

        this.seq = new DERSequence(v);
    }

    public boolean hasKeyPurposeId(
        KeyPurposeId keyPurposeId)
    {
        return (usageTable.get(keyPurposeId) != null);
    }

    public int size()
    {
        return usageTable.size();
    }

    public DERObject toASN1Object()
    {
        return seq;
    }
}
