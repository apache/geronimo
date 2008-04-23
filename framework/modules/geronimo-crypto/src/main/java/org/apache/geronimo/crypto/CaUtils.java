/**
 *
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
package org.apache.geronimo.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.apache.geronimo.crypto.asn1.ASN1InputStream;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.DERBitString;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;
import org.apache.geronimo.crypto.asn1.DERString;
import org.apache.geronimo.crypto.asn1.pkcs.CertificationRequestInfo;
import org.apache.geronimo.crypto.asn1.pkcs.PKCSObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.x509.RSAPublicKeyStructure;
import org.apache.geronimo.crypto.asn1.x509.SubjectPublicKeyInfo;
import org.apache.geronimo.crypto.asn1.x509.X509CertificateStructure;
import org.apache.geronimo.crypto.asn1.x509.X509Name;
import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.crypto.jce.PKCS10CertificationRequest;

/**
 * This class implements some utility methods used by CA
 *
 * @version $Rev$ $Date$
 */
public class CaUtils {
    public static final String CERT_HEADER = "-----BEGIN CERTIFICATE-----";
    public static final String CERT_FOOTER = "-----END CERTIFICATE-----";
    public static final String CERT_REQ_HEADER = "-----BEGIN CERTIFICATE REQUEST-----";
    public static final String CERT_REQ_FOOTER = "-----END CERTIFICATE REQUEST-----";
    public static final int B64_LINE_SIZE = 76;
    public static final String CERT_REQ_SUBJECT = "subject";
    public static final String CERT_REQ_PUBLICKEY = "publickey";
    public static final String CERT_REQ_PUBLICKEY_OBJ = "publickeyObj";
    public static final String CERT_REQ_VERSION = "version";
    public static final String PKAC_CHALLENGE = "challenge";

    /**
     * This method returns base64 encoded text of a given certificate.
     * @param cert The certificate that needs to be encoded in base64
     */
    public static String base64Certificate(Certificate cert) throws CertificateEncodingException, Exception {
        return base64Text(cert.getEncoded(), CaUtils.CERT_HEADER, CaUtils.CERT_FOOTER, CaUtils.B64_LINE_SIZE);
    }
    
    /**
     * This method encodes a given byte array into base64 along with specified header and footers.
     * @param data The byte array to be encoded in base64
     * @param header Header for base64 encoded text
     * @param footer Footer for base64 encoded text
     * @param lineSize Maximum line size to split base64 encoded text if required
     */
    public static String base64Text(byte[] data, String header, String footer, int lineSize) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        storeInBase64(bout, data, header, footer, lineSize);
        bout.close();
        return bout.toString();
    }
    /**
     * This method encodes a given byte array into base64 along with specified header and footers and writes
     * the output to a specified OutputStream.
     * @param fout Output stream to write the encoded text
     * @param data The byte array to be encoded in base64
     * @param header Header for base64 encoded text
     * @param footer Footer for base64 encoded text
     * @param lineSize Maximum line size to split base64 encoded text if required
     */
    public static void storeInBase64(OutputStream fout, byte[] data, String header, String footer, int lineSize) throws Exception {
        PrintWriter out = new PrintWriter(fout);
        if(header != null) out.println(header);

        byte[] encodedData = Base64.encode(data);
        int i = 0;
        do {
            out.println(new String(encodedData, i, Math.min(lineSize, encodedData.length-i)));
            i += lineSize;
        } while(i < encodedData.length);

        if(footer != null) out.println(footer);
        out.flush();
    }

    /**
     * This method encodes a given byte array into base64 along with specified header and footers and writes
     * the output to a specified file.
     * @param outfile File name to write the output to
     * @param data The byte array to be encoded in base64
     * @param header Header for base64 encoded text
     * @param footer Footer for base64 encoded text
     * @param lineSize Maximum line size to split base64 encoded text if required
     */
    public static void storeInBase64(String outfile, byte[] data, String header, String footer, int lineSize) throws Exception {
        FileOutputStream fout = new FileOutputStream(outfile);
        storeInBase64(fout, data, header, footer, lineSize);
        fout.close();
    }

    /**
     * This method creates a java.security.PublicKey object based on the public key information given in SubjectPublicKeyInfo
     * @param pubKeyInfo SubjectPublicKeyInfo instance containing the public key information.
     */
    public static PublicKey getPublicKeyObject(SubjectPublicKeyInfo pubKeyInfo) throws Exception{
        RSAPublicKeyStructure pubkeyStruct = new RSAPublicKeyStructure((ASN1Sequence)pubKeyInfo.getPublicKey());
        RSAPublicKeySpec pubkeySpec = new RSAPublicKeySpec(pubkeyStruct.getModulus(), pubkeyStruct.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubkeySpec);
        return pubKey;
    }
    
    /**
     * This method returns a X509Name object corresponding to the subject in a given certificate
     * @param cert Certificate from which subject needs to be retrieved
     */
    public static X509Name getSubjectX509Name(Certificate cert) throws CertificateEncodingException, IOException {
        ASN1InputStream ais = new ASN1InputStream(cert.getEncoded());
        X509CertificateStructure x509Struct = new X509CertificateStructure((ASN1Sequence)ais.readObject());
        ais.close();
        return x509Struct.getSubject();
    }

    /**
     * This method returns a X509Name object corresponding to a given principal
     */
    public static X509Name getX509Name(X500Principal principal) throws CertificateEncodingException, IOException {
        ASN1InputStream ais = new ASN1InputStream(principal.getEncoded());
        X509Name name = new X509Name((ASN1Sequence)ais.readObject());
        ais.close();
        return name;
    }

    /**
     * This method processes a certificate request and returns a map containing subject
     * and public key in the request.
     * @param certreq base64 encoded PKCS10 certificate request
     */
    public static Map processPKCS10Request(String certreq) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, Exception {
        if(certreq.indexOf("-----") != -1) {
            // Strip any header and footer
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(certreq.getBytes())));
            String line = null;
            String b64data = "";
            while((line = br.readLine()) != null) {
                if(!line.startsWith("-----")) {
                    b64data += line;
                }
            }
            br.close();
            certreq = b64data;
        }
        byte[] data = Base64.decode(certreq);
        
        PKCS10CertificationRequest pkcs10certreq = new PKCS10CertificationRequest(data);
        if(!pkcs10certreq.verify()) {
            throw new Exception("CSR verification failed.");
        }
        CertificationRequestInfo certReqInfo = pkcs10certreq.getCertificationRequestInfo();
        Map map = new HashMap();
        map.put(CERT_REQ_SUBJECT, certReqInfo.getSubject());
        map.put(CERT_REQ_PUBLICKEY, certReqInfo.getSubjectPublicKeyInfo());
        map.put(CERT_REQ_PUBLICKEY_OBJ, getPublicKeyObject(certReqInfo.getSubjectPublicKeyInfo()));
        map.put(CERT_REQ_VERSION, certReqInfo.getVersion());
        return map;
    }
    
    /**
     * This method processes a DER encoded SignedPublicKeyAndChallenge in base64 format.
     * @param spkac SignedPublicKeyAndChallenge in base64 text format
     * @return a Map with Subject, public-key and challenge 
     */
    public static Map processSPKAC(String spkac) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, Exception {
        Map map = new HashMap();
        byte[]data = Base64.decode(spkac);
        ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(data));
        DERSequence spkacSeq = (DERSequence)ais.readObject();

        // SPKAC = SEQ {PKAC, SIGN-ALG, SIGN}
        // Get PKAC and obtain PK and C
        DERSequence pkacSeq = (DERSequence)spkacSeq.getObjectAt(0);
        DERObject pk = (DERObject)pkacSeq.getObjectAt(0);
        DERObject ch = (DERObject)pkacSeq.getObjectAt(1);
        SubjectPublicKeyInfo pkInfo = new SubjectPublicKeyInfo((DERSequence)pk);
        PublicKey pubKey =  getPublicKeyObject(pkInfo);

        // Get SIGN-ALG
        DERSequence signAlg = (DERSequence) spkacSeq.getObjectAt(1);
        DERObject alg0 = (DERObject)signAlg.getObjectAt(0);

        // Get SIGN
        DERBitString sign = (DERBitString) spkacSeq.getObjectAt(2);
        byte[] signature = sign.getBytes();
        
        // Verify the signature on SPKAC
        String signAlgString = PKCSObjectIdentifiers.md5WithRSAEncryption.equals(alg0) ? "MD5withRSA" :
                               PKCSObjectIdentifiers.md2WithRSAEncryption.equals(alg0) ? "MD2withRSA" :
                               PKCSObjectIdentifiers.sha1WithRSAEncryption.equals(alg0) ? "SHA1withRSA" : null;
        Signature signObj = Signature.getInstance(signAlgString);
        signObj.initVerify(pubKey);
        signObj.update(pkacSeq.getEncoded());
        boolean verified = signObj.verify(signature);
        if(!verified) throw new Exception("SignedPublicKeyAndChallenge verification failed.");
        map.put(CERT_REQ_PUBLICKEY, pkInfo);
        map.put(CERT_REQ_PUBLICKEY_OBJ, pubKey);
        if(((DERString)ch).getString() != null) map.put(PKAC_CHALLENGE, ((DERString)ch).getString());
        return map;
    }
    
    /**
     * This method creates a X509Name object using the name attributes specified.
     * @param cn Common Name
     * @param ou Organization Unit
     * @param o Organization
     * @param l Locality
     * @param st State
     * @param c Country
     */
    public static X509Name getX509Name(String cn, String ou, String o, String l, String st, String c)  {
        Vector order = new Vector();
        Hashtable attrmap = new Hashtable();
        if (c != null) {
            attrmap.put(X509Name.C, c);
            order.add(X509Name.C);
        }

        if (st != null) {
            attrmap.put(X509Name.ST, st);
            order.add(X509Name.ST);
        }

        if (l != null) {
            attrmap.put(X509Name.L, l);
            order.add(X509Name.L);
        }

        if (o != null) {
            attrmap.put(X509Name.O, o);
            order.add(X509Name.O);
        }

        if (ou != null) {
            attrmap.put(X509Name.OU, ou);
            order.add(X509Name.OU);
        }

        if (cn != null) {
            attrmap.put(X509Name.CN, cn);
            order.add(X509Name.CN);
        }

        return new X509Name(order, attrmap);
    }
}
