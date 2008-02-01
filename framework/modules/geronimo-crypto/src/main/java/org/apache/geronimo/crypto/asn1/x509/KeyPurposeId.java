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

import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;

/**
 * The KeyPurposeId object.
 * <pre>
 *     KeyPurposeId ::= OBJECT IDENTIFIER
 * </pre>
 */
public class KeyPurposeId
    extends DERObjectIdentifier
{
    private static final String id_kp = "1.3.6.1.5.5.7.3";

    private KeyPurposeId(
        String  id)
    {
        super(id);
    }

    public static final KeyPurposeId anyExtendedKeyUsage = new KeyPurposeId(X509Extensions.ExtendedKeyUsage.getId() + ".0");
    public static final KeyPurposeId id_kp_serverAuth = new KeyPurposeId(id_kp + ".1");
    public static final KeyPurposeId id_kp_clientAuth = new KeyPurposeId(id_kp + ".2");
    public static final KeyPurposeId id_kp_codeSigning = new KeyPurposeId(id_kp + ".3");
    public static final KeyPurposeId id_kp_emailProtection = new KeyPurposeId(id_kp + ".4");
    public static final KeyPurposeId id_kp_ipsecEndSystem = new KeyPurposeId(id_kp + ".5");
    public static final KeyPurposeId id_kp_ipsecTunnel = new KeyPurposeId(id_kp + ".6");
    public static final KeyPurposeId id_kp_ipsecUser = new KeyPurposeId(id_kp + ".7");
    public static final KeyPurposeId id_kp_timeStamping = new KeyPurposeId(id_kp + ".8");
    public static final KeyPurposeId id_kp_OCSPSigning = new KeyPurposeId(id_kp + ".9");

    //
    // microsoft key purpose ids
    //
    public static final KeyPurposeId id_kp_smartcardlogon = new KeyPurposeId("1.3.6.1.4.1.311.20.2.2");
}
