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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.geronimo.crypto.asn1.ASN1InputStream;
import org.apache.geronimo.crypto.asn1.ASN1OutputStream;
import org.apache.geronimo.crypto.asn1.DEREncodable;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.crypto.params.RSAKeyParameters;
import org.apache.geronimo.crypto.jce.interfaces.PKCS12BagAttributeCarrier;

public class JCERSAPrivateKey
    implements RSAPrivateKey, PKCS12BagAttributeCarrier
{
    protected BigInteger modulus;
    protected BigInteger privateExponent;

    private Hashtable   pkcs12Attributes = new Hashtable();
    private Vector      pkcs12Ordering = new Vector();

    protected JCERSAPrivateKey()
    {
    }

    JCERSAPrivateKey(
        RSAKeyParameters key)
    {
        this.modulus = key.getModulus();
        this.privateExponent = key.getExponent();
    }

    JCERSAPrivateKey(
        RSAPrivateKeySpec spec)
    {
        this.modulus = spec.getModulus();
        this.privateExponent = spec.getPrivateExponent();
    }

    JCERSAPrivateKey(
        RSAPrivateKey key)
    {
        this.modulus = key.getModulus();
        this.privateExponent = key.getPrivateExponent();
    }

    public BigInteger getModulus()
    {
        return modulus;
    }

    public BigInteger getPrivateExponent()
    {
        return privateExponent;
    }

    public String getAlgorithm()
    {
        return "RSA";
    }

    public String getFormat()
    {
        return "NULL";
    }

    public byte[] getEncoded()
    {
        return null;
    }

    public boolean equals(Object o)
    {
        if ( !(o instanceof RSAPrivateKey) )
        {
            return false;
        }

        if ( o == this )
        {
            return true;
        }

        RSAPrivateKey key = (RSAPrivateKey)o;

        return getModulus().equals(key.getModulus())
            && getPrivateExponent().equals(key.getPrivateExponent());
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

    private void readObject(
        ObjectInputStream   in)
        throws IOException, ClassNotFoundException
    {
        this.modulus = (BigInteger)in.readObject();

        Object  obj = in.readObject();

        if (obj instanceof Hashtable)
        {
            this.pkcs12Attributes = (Hashtable)obj;
            this.pkcs12Ordering = (Vector)in.readObject();
        }
        else
        {
            this.pkcs12Attributes = new Hashtable();
            this.pkcs12Ordering = new Vector();

            ByteArrayInputStream    bIn = new ByteArrayInputStream((byte[])obj);
            ASN1InputStream         aIn = new ASN1InputStream(bIn);

            DERObjectIdentifier    oid;

            while ((oid = (DERObjectIdentifier)aIn.readObject()) != null)
            {
                this.setBagAttribute(oid, aIn.readObject());
            }
        }

        this.privateExponent = (BigInteger)in.readObject();
    }

    private void writeObject(
        ObjectOutputStream  out)
        throws IOException
    {
        out.writeObject(modulus);

        if (pkcs12Ordering.size() == 0)
        {
            out.writeObject(pkcs12Attributes);
            out.writeObject(pkcs12Ordering);
        }
        else
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

            Enumeration             e = this.getBagAttributeKeys();

            while (e.hasMoreElements())
            {
                DEREncodable    oid = (DEREncodable)e.nextElement();

                aOut.writeObject(oid);
                aOut.writeObject(pkcs12Attributes.get(oid));
            }

            out.writeObject(bOut.toByteArray());
        }

        out.writeObject(privateExponent);
    }
}
