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

package org.apache.geronimo.crypto.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAParameterSpec;
import java.security.spec.DSAPublicKeySpec;

import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.DERInteger;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.x509.AlgorithmIdentifier;
import org.apache.geronimo.crypto.asn1.x509.DSAParameter;
import org.apache.geronimo.crypto.asn1.x509.SubjectPublicKeyInfo;
import org.apache.geronimo.crypto.asn1.x9.X9ObjectIdentifiers;
import org.apache.geronimo.crypto.crypto.params.DSAPublicKeyParameters;

public class JDKDSAPublicKey
    implements DSAPublicKey
{
    private BigInteger      y;
    private DSAParams       dsaSpec;

    JDKDSAPublicKey(
        DSAPublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
    }

    JDKDSAPublicKey(
        DSAPublicKey    key)
    {
        this.y = key.getY();
        this.dsaSpec = key.getParams();
    }

    JDKDSAPublicKey(
        DSAPublicKeyParameters  params)
    {
        this.y = params.getY();
        this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
    }

    JDKDSAPublicKey(
        BigInteger        y,
        DSAParameterSpec  dsaSpec)
    {
        this.y = y;
        this.dsaSpec = dsaSpec;
    }

    JDKDSAPublicKey(
        SubjectPublicKeyInfo    info)
    {
        DSAParameter             params = new DSAParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger              derY = null;

        try
        {
            derY = (DERInteger)info.getPublicKey();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in DSA public key", e);
        }

        this.y = derY.getValue();
        this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
    }

    public String getAlgorithm()
    {
        return "DSA";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        SubjectPublicKeyInfo    info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(dsaSpec.getP(), dsaSpec.getQ(), dsaSpec.getG()).getDERObject()), new DERInteger(y));

        try
        {
            dOut.writeObject(info);
            dOut.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error encoding DSA public key", e);
        }

        return bOut.toByteArray();

    }

    public DSAParams getParams()
    {
        return dsaSpec;
    }

    public BigInteger getY()
    {
        return y;
    }

    public String toString()
    {
        StringBuilder    buf = new StringBuilder();
        String          nl = System.getProperty("line.separator");

        buf.append("DSA Public Key" + nl);
        buf.append("            y: " + this.getY().toString(16) + nl);

        return buf.toString();
    }
}
