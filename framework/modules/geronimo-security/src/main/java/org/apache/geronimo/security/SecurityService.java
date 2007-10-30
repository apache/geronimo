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
package org.apache.geronimo.security;

public interface SecurityService {
    public final static String POLICY_CONFIG_FACTORY = "javax.security.jacc.PolicyConfigurationFactory.provider";
    public final static String POLICY_PROVIDER = "javax.security.jacc.policy.provider";
    public final static String KEYSTORE = "javax.net.ssl.keyStore";
    public final static String KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    public final static String TRUSTSTORE = "javax.net.ssl.trustStore";
    public final static String TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";
}
