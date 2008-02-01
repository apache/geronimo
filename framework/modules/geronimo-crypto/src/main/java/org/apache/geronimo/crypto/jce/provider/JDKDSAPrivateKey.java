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
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAParameterSpec;
import java.security.spec.DSAPrivateKeySpec;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.DEREncodable;
import org.apache.geronimo.crypto.asn1.DERInteger;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.pkcs.PrivateKeyInfo;
import org.apache.geronimo.crypto.asn1.x509.AlgorithmIdentifier;
import org.apache.geronimo.crypto.asn1.x509.DSAParameter;
import org.apache.geronimo.crypto.asn1.x9.X9ObjectIdentifiers;
import org.apache.geronimo.crypto.crypto.params.DSAPrivateKeyParameters;
import org.apache.geronimo.crypto.jce.interfaces.PKCS12BagAttributeCarrier;

public class JDKDSAPrivateKey
    implements DSAPrivateKey, PKCS12BagAttributeCarrier
{
    BigInteger          x;
    DSAParams           dsaSpec;

    private Hashtable   pkcs12Attributes = new Hashtable();
    private Vector      pkcs12Ordering = new Vector();

    protected JDKDSAPrivateKey()
    {
    }

    JDKDSAPrivateKey(
        DSAPrivateKey    key)
    {
        this.x = key.getX();
        this.dsaSpec = key.getParams();
    }

    JDKDSAPrivateKey(
        DSAPrivateKeySpec    spec)
    {
        this.x = spec.getX();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
    }

    JDKDSAPrivateKey(
        PrivateKeyInfo  info)
    {
        DSAParameter    params = new DSAParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger      derX = (DERInteger)info.getPrivateKey();

        this.x = derX.getValue();
        this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
    }

    JDKDSAPrivateKey(
        DSAPrivateKeyParameters  params)
    {
        this.x = params.getX();
        this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
    }

    public String getAlgorithm()
    {
        return "DSA";
    }

    /**
     * return the encoding format we produce in getEncoded().
     *
     * @return the string "PKCS#8"
     */
    public String getFormat()
    {
        return "PKCS#8";
    }

    /**
     * Return a PKCS8 representation of the key. The sequence returned
     * represents a full PrivateKeyInfo object.
     *
     * @return a PKCS8 representation of the key.
     */
    public byte[] getEncoded()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        PrivateKeyInfo          info = new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(dsaSpec.getP(), dsaSpec.getQ(), dsaSpec.getG()).getDERObject()), new DERInteger(getX()));

        try
        {
            dOut.writeObject(info);
            dOut.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error encoding DSA private key", e);
        }

        return bOut.toByteArray();
    }

    public DSAParams getParams()
    {
        return dsaSpec;
    }

    public BigInteger getX()
    {
        return x;
    }

    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute)
    {
        pkcs12Attributes.put(oid, attribute);
        pkcs12Ordering.addElement(oid);
    }

    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid)
    {
        return (DEREncodable)pkcs12Attributes.get(oid);
    }

    public Enumeration getBagAttributeKeys()
    {
        return pkcs12Ordering.elements();
    }
}
