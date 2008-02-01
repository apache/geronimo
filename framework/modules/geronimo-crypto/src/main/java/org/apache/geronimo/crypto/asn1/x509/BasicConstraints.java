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

import java.math.BigInteger;

import org.apache.geronimo.crypto.asn1.ASN1EncodableVector;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.ASN1TaggedObject;
import org.apache.geronimo.crypto.asn1.DERBoolean;
import org.apache.geronimo.crypto.asn1.ASN1Encodable;
import org.apache.geronimo.crypto.asn1.DERInteger;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;

public class BasicConstraints
    extends ASN1Encodable
{
    DERBoolean  cA = new DERBoolean(false);
    DERInteger  pathLenConstraint = null;

    public static BasicConstraints getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static BasicConstraints getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof BasicConstraints)
        {
            return (BasicConstraints)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new BasicConstraints((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public BasicConstraints(
        ASN1Sequence   seq)
    {
        if (seq.size() == 0)
        {
            this.cA = null;
            this.pathLenConstraint = null;
        }
        else
        {
            this.cA = (DERBoolean)seq.getObjectAt(0);
            if (seq.size() > 1)
            {
                this.pathLenConstraint = (DERInteger)seq.getObjectAt(1);
            }
        }
    }

    /**
     * @deprecated use one of the other two unambigous constructors.
     * @param cA
     * @param pathLenConstraint
     */
    public BasicConstraints(
        boolean cA,
        int     pathLenConstraint)
    {
        if (cA )
        {
            this.cA = new DERBoolean(cA);
            this.pathLenConstraint = new DERInteger(pathLenConstraint);
        }
        else
        {
            this.cA = null;
            this.pathLenConstraint = null;
        }
    }

    public BasicConstraints(
        boolean cA)
    {
        if (cA)
        {
            this.cA = new DERBoolean(true);
        }
        else
        {
            this.cA = null;
        }
        this.pathLenConstraint = null;
    }

    /**
     * create a cA=true object for the given path length constraint.
     *
     * @param pathLenConstraint
     */
    public BasicConstraints(
        int     pathLenConstraint)
    {
        this.cA = new DERBoolean(true);
        this.pathLenConstraint = new DERInteger(pathLenConstraint);
    }

    public boolean isCA()
    {
        return (cA != null) && cA.isTrue();
    }

    public BigInteger getPathLenConstraint()
    {
        if (pathLenConstraint != null)
        {
            return pathLenConstraint.getValue();
        }

        return null;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * BasicConstraints := SEQUENCE {
     *    cA                  BOOLEAN DEFAULT FALSE,
     *    pathLenConstraint   INTEGER (0..MAX) OPTIONAL
     * }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        if (cA != null)
        {
            v.add(cA);

            if (pathLenConstraint != null)
            {
                v.add(pathLenConstraint);
            }
        }

        return new DERSequence(v);
    }

    public String toString()
    {
        if (pathLenConstraint == null)
        {
            if (cA == null)
            {
                return "BasicConstraints: isCa(false)";
            }
            return "BasicConstraints: isCa(" + this.isCA() + ")";
        }
        return "BasicConstraints: isCa(" + this.isCA() + "), pathLenConstraint = " + pathLenConstraint.getValue();
    }
}
