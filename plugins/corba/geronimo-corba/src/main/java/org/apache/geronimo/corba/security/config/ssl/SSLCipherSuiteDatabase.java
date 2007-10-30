/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.corba.security.config.ssl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.NoProtection;


/**
 * @version $Revision: 452600 $ $Date: 2006-10-03 12:29:42 -0700 (Tue, 03 Oct 2006) $
 */
public final class SSLCipherSuiteDatabase {

    /**
     * A map for stroing all the cipher suites.
     */
    private static final Map SUITES = new HashMap();

    static {
        // No protection
        Integer noProt = new Integer(NoProtection.value);
        SUITES.put("SSL_NULL_WITH_NULL_NULL", noProt);
        SUITES.put("TLS_NULL_WITH_NULL_NULL", noProt);

        // No authentication
        Integer noAuth = new Integer(Confidentiality.value);
        SUITES.put("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", noAuth);
        SUITES.put("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", noAuth);
        SUITES.put("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", noAuth);
        SUITES.put("SSL_DH_anon_WITH_RC4_128_MD5", noAuth);
        SUITES.put("SSL_DH_anon_WITH_DES_CBC_SHA", noAuth);

        SUITES.put("TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA", noAuth);
        SUITES.put("TLS_DH_anon_EXPORT_WITH_RC4_40_MD5", noAuth);
        SUITES.put("TLS_DH_anon_WITH_3DES_EDE_CBC_SHA", noAuth);
        SUITES.put("TLS_DH_anon_WITH_RC4_128_MD5", noAuth);
        SUITES.put("TLS_DH_anon_WITH_DES_CBC_SHA", noAuth);

        // No encryption
        Integer noEnc = new Integer(EstablishTrustInTarget.value);
        SUITES.put("SSL_RSA_WITH_NULL_MD5", noEnc);
        SUITES.put("SSL_RSA_WITH_NULL_SHA", noEnc);

        SUITES.put("TLS_RSA_WITH_NULL_MD5", noEnc);
        SUITES.put("TLS_RSA_WITH_NULL_SHA", noEnc);

        // Auth and encrypt
        Integer authEnc = new Integer(EstablishTrustInTarget.value | Confidentiality.value);
        SUITES.put("SSL_DHE_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_RC4_128_MD5", authEnc);
        SUITES.put("SSL_RSA_WITH_RC4_128_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("SSL_RSA_EXPORT_WITH_RC4_40_MD5", authEnc);

        SUITES.put("TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DHE_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_DSS_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_DH_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", authEnc);
        SUITES.put("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_DES_CBC_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_KRB5_WITH_RC4_128_MD5", authEnc);
        SUITES.put("TLS_KRB5_WITH_RC4_128_SHA", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_DES40_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5", authEnc);
        SUITES.put("TLS_RSA_EXPORT_WITH_RC4_40_MD5", authEnc);
        SUITES.put("TLS_RSA_WITH_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_WITH_DES_CBC_SHA", authEnc);
        SUITES.put("TLS_RSA_WITH_RC4_128_MD5", authEnc);
        SUITES.put("TLS_RSA_WITH_RC4_128_SHA", authEnc);

        // RSA supported cipher suite names differ from Sun's
        SUITES.put("RSA_Export_With_RC2_40_CBC_MD5", authEnc);
        SUITES.put("RSA_With_DES_CBC_SHA", authEnc);
        SUITES.put("RSA_Export_With_RC4_40_MD5", authEnc);
        SUITES.put("RSA_With_RC4_SHA", authEnc);
        SUITES.put("RSA_With_3DES_EDE_CBC_SHA", authEnc);
        SUITES.put("RSA_Export_With_DES_40_CBC_SHA", authEnc);
        SUITES.put("RSA_With_RC4_MD5", authEnc);
    }

    /**
     * Do not allow instances of this class.
     */
    private SSLCipherSuiteDatabase() {
    }

    /**
     * Return an array of cipher suites that match the assocRequires and
     * assocSupports options.
     *
     * @param assocRequires         The required associations.
     * @param assocSupports         The supported associations.
     * @param supportedCipherSuites The overall supported cipher suites.
     * @return The cipher suites that matches the two options.
     */
    public static String[] getCipherSuites(int assocRequires, int assocSupports, String[] supportedCipherSuites) {

        assocRequires = assocRequires & (EstablishTrustInTarget.value | Confidentiality.value | NoProtection.value);
        assocSupports = assocSupports & (EstablishTrustInTarget.value | Confidentiality.value | NoProtection.value);

        ArrayList col = new ArrayList();
        for (int i = 0; i < supportedCipherSuites.length; ++i) {
            Integer val = (Integer) SUITES.get(supportedCipherSuites[i]);

            if (val != null && ((assocRequires & ~val.intValue()) == 0 && (val.intValue() & ~assocSupports) == 0)) {
                col.add(supportedCipherSuites[i]);
            }
        }

        String[] ret = new String[col.size()];
        col.toArray(ret);

        return ret;
    }

    /**
     * Return the options values for a cipher suite.
     *
     * @param cypherSuite The cipher suite to get the options value for.
     * @return The int value for the cipher suite.
     */
    public static int getAssociaionOptions(String cypherSuite) {
        return ((Integer) SUITES.get(cypherSuite)).intValue();
    }
}

