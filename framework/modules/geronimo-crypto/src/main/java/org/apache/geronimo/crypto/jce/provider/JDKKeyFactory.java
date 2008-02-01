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
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;

import org.apache.geronimo.crypto.asn1.ASN1InputStream;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.oiw.OIWObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.pkcs.PKCSObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.pkcs.PrivateKeyInfo;
import org.apache.geronimo.crypto.asn1.pkcs.RSAPrivateKeyStructure;
import org.apache.geronimo.crypto.asn1.x509.AlgorithmIdentifier;
import org.apache.geronimo.crypto.asn1.x509.SubjectPublicKeyInfo;
import org.apache.geronimo.crypto.asn1.x509.X509ObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.x9.X9ObjectIdentifiers;
import org.apache.geronimo.crypto.jce.provider.JCEDHPrivateKey;
import org.apache.geronimo.crypto.jce.provider.JCEDHPublicKey;

public abstract class JDKKeyFactory
    extends KeyFactorySpi
{
    public JDKKeyFactory()
    {
    }

    protected KeySpec engineGetKeySpec(
        Key    key,
        Class    spec)
    throws InvalidKeySpecException
    {
       if (spec.isAssignableFrom(PKCS8EncodedKeySpec.class) && key.getFormat().equals("PKCS#8"))
       {
               return new PKCS8EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(X509EncodedKeySpec.class) && key.getFormat().equals("X.509"))
       {
               return new X509EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(RSAPublicKeySpec.class) && key instanceof RSAPublicKey)
       {
            RSAPublicKey    k = (RSAPublicKey)key;

            return new RSAPublicKeySpec(k.getModulus(), k.getPublicExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateKeySpec.class) && key instanceof RSAPrivateKey)
       {
            RSAPrivateKey    k = (RSAPrivateKey)key;

            return new RSAPrivateKeySpec(k.getModulus(), k.getPrivateExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateCrtKeySpec.class) && key instanceof RSAPrivateCrtKey)
       {
            RSAPrivateCrtKey    k = (RSAPrivateCrtKey)key;

            return new RSAPrivateCrtKeySpec(
                            k.getModulus(), k.getPublicExponent(),
                            k.getPrivateExponent(),
                            k.getPrimeP(), k.getPrimeQ(),
                            k.getPrimeExponentP(), k.getPrimeExponentQ(),
                            k.getCrtCoefficient());
       }
       else if (spec.isAssignableFrom(DHPrivateKeySpec.class) && key instanceof DHPrivateKey)
       {
           DHPrivateKey k = (DHPrivateKey)key;

           return new DHPrivateKeySpec(k.getX(), k.getParams().getP(), k.getParams().getG());
       }
       else if (spec.isAssignableFrom(DHPublicKeySpec.class) && key instanceof DHPublicKey)
       {
           DHPublicKey k = (DHPublicKey)key;

           return new DHPublicKeySpec(k.getY(), k.getParams().getP(), k.getParams().getG());
       }

        throw new RuntimeException("not implemented yet " + key + " " + spec);
    }

    protected Key engineTranslateKey(
        Key    key)
        throws InvalidKeyException
    {
        if (key instanceof RSAPublicKey)
        {
            return new JCERSAPublicKey((RSAPublicKey)key);
        }
        else if (key instanceof RSAPrivateCrtKey)
        {
            return new JCERSAPrivateCrtKey((RSAPrivateCrtKey)key);
        }
        else if (key instanceof RSAPrivateKey)
        {
            return new JCERSAPrivateKey((RSAPrivateKey)key);
        }
        else if (key instanceof DHPublicKey)
        {
            return new JCEDHPublicKey((DHPublicKey)key);
        }
        else if (key instanceof DHPrivateKey)
        {
            return new JCEDHPrivateKey((DHPrivateKey)key);
        }
        else if (key instanceof DSAPublicKey)
        {
            return new JDKDSAPublicKey((DSAPublicKey)key);
        }
        else if (key instanceof DSAPrivateKey)
        {
            return new JDKDSAPrivateKey((DSAPrivateKey)key);
        }
        throw new InvalidKeyException("key type unknown");
    }

    /**
     * create a public key from the given DER encoded input stream.
     */
    static PublicKey createPublicKeyFromDERStream(
        InputStream         in)
        throws IOException
    {
        return createPublicKeyFromPublicKeyInfo(
                new SubjectPublicKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }

    /**
     * create a public key from the given public key info object.
     */
    static PublicKey createPublicKeyFromPublicKeyInfo(
        SubjectPublicKeyInfo         info)
    {
        AlgorithmIdentifier     algId = info.getAlgorithmId();

        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption)
            || algId.getObjectId().equals(X509ObjectIdentifiers.id_ea_rsa))
        {
              return new JCERSAPublicKey(info);
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
              return new JCEDHPublicKey(info);
        }
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.dhpublicnumber))
        {
              return new JCEDHPublicKey(info);
        }
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa))
        {
              return new JDKDSAPublicKey(info);
        }
        else if (algId.getObjectId().equals(OIWObjectIdentifiers.dsaWithSHA1))
        {
              return new JDKDSAPublicKey(info);
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }

    /**
     * create a private key from the given DER encoded input stream.
     */
    static PrivateKey createPrivateKeyFromDERStream(
        InputStream         in)
        throws IOException
    {
        return createPrivateKeyFromPrivateKeyInfo(
                new PrivateKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }

    /**
     * create a private key from the given public key info object.
     */
    static PrivateKey createPrivateKeyFromPrivateKeyInfo(
        PrivateKeyInfo      info)
    {
        AlgorithmIdentifier     algId = info.getAlgorithmId();

        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption))
        {
              return new JCERSAPrivateCrtKey(info);
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
              return new JCEDHPrivateKey(info);
        }
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa))
        {
              return new JDKDSAPrivateKey(info);
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }

    public static class RSA
        extends JDKKeyFactory
    {
        public RSA()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                new ByteArrayInputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    //
                    // in case it's just a RSAPrivateKey object...
                    //
                    try
                    {
                        return new JCERSAPrivateCrtKey(
                            new RSAPrivateKeyStructure(
                                (ASN1Sequence)new ASN1InputStream(new ByteArrayInputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded())).readObject()));
                    }
                    catch (Exception ex)
                    {
                        throw (InvalidKeySpecException)new InvalidKeySpecException(ex.getMessage()).initCause(ex);
                    }
                }
            }
            else if (keySpec instanceof RSAPrivateCrtKeySpec)
            {
                return new JCERSAPrivateCrtKey((RSAPrivateCrtKeySpec)keySpec);
            }
            else if (keySpec instanceof RSAPrivateKeySpec)
            {
                return new JCERSAPrivateKey((RSAPrivateKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }

        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                new ByteArrayInputStream(((X509EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }
            else if (keySpec instanceof RSAPublicKeySpec)
            {
                return new JCERSAPublicKey((RSAPublicKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }
    }

    public static class DH
        extends JDKKeyFactory
    {
        public DH()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                new ByteArrayInputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }
            else if (keySpec instanceof DHPrivateKeySpec)
            {
                return new JCEDHPrivateKey((DHPrivateKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }

        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                new ByteArrayInputStream(((X509EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }
            else if (keySpec instanceof DHPublicKeySpec)
            {
                return new JCEDHPublicKey((DHPublicKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }
    }

    public static class DSA
        extends JDKKeyFactory
    {
        public DSA()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                new ByteArrayInputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }
            else if (keySpec instanceof DSAPrivateKeySpec)
            {
                return new JDKDSAPrivateKey((DSAPrivateKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }

        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                new ByteArrayInputStream(((X509EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }
            else if (keySpec instanceof DSAPublicKeySpec)
            {
                return new JDKDSAPublicKey((DSAPublicKeySpec)keySpec);
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }
    }


    public static class EC
        extends JDKKeyFactory
    {
        String  algorithm;

        public EC()
        {
            this("EC");
        }

        public EC(
            String  algorithm)
        {
            this.algorithm = algorithm;
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                new ByteArrayInputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }

        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                new ByteArrayInputStream(((X509EncodedKeySpec)keySpec).getEncoded()));
                }
                catch (Exception e)
                {
                    throw (InvalidKeySpecException)new InvalidKeySpecException(e.getMessage()).initCause(e);
                }
            }

            throw new InvalidKeySpecException("Unknown KeySpec type.");
        }
    }
}
