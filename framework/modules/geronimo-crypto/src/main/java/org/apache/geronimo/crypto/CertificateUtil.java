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
package org.apache.geronimo.crypto;

import org.apache.geronimo.crypto.encoders.HexEncoder;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utility functions for dealing with X.509 certificates
 *
 * @version $Rev$ $Date$
 */
public class CertificateUtil {
    public static String generateFingerprint(Certificate cert, String digestAlgorithm) throws NoSuchAlgorithmException, CertificateEncodingException, IOException {
        MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
        byte[] digest = md.digest(cert.getEncoded());
        ByteArrayOutputStream out = new ByteArrayOutputStream(digest.length*2);
        new HexEncoder().encode(digest, 0, digest.length, out);
        String all = new String(out.toByteArray(), "US-ASCII").toUpperCase();
        Matcher matcher = Pattern.compile("..").matcher(all);
        StringBuilder buf = new StringBuilder();
        while(matcher.find()) {
            if(buf.length() > 0) {
                buf.append(":");
            }
            buf.append(matcher.group());
        }
        return buf.toString();
    }
}
