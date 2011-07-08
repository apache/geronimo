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
package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.CallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class CertificateChainCallbackHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(CertificateChainCallbackHandler.class);
    Certificate[] certificateChain;

    public CertificateChainCallbackHandler(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (callback instanceof CertificateChainCallback) {
                CertificateChainCallback cc = (CertificateChainCallback) callback;
                cc.setCertificateChain(certificateChain);
            } else if (callback instanceof CertificateCallback) {
                if (certificateChain != null
                        && certificateChain.length > 0
                        && certificateChain[0] instanceof X509Certificate) {
                    CertificateCallback cc = (CertificateCallback) callback;
                    cc.setCertificate((X509Certificate) certificateChain[0]);
                } else {
                    StringBuilder buf = new StringBuilder("Invalid certificate chain: \n");
                    if (certificateChain == null) {
                        buf.append("certificate chain is null");
                    } else {
                        buf.append("certificate chain length: ").append(certificateChain.length).append("\n");
                        if (certificateChain.length > 0) {
                            buf.append("first certificate is a: ").append(certificateChain[0].getClass()).append("\n");
                            buf.append("certificate is an X509Certificate: ").append(certificateChain[0] instanceof X509Certificate).append("\n");
                        }
                    }
                    throw new UnsupportedCallbackException(callback, buf.toString());
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Wrong callback type: " + callback.getClass());
            }
        }
    }

}
