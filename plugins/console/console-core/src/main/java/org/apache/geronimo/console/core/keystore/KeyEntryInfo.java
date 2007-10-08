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

package org.apache.geronimo.console.core.keystore;

import java.util.Date;

public class KeyEntryInfo {
    public static final String TRUSTED_CERT_TYPE = "trusted certificate";

    public static final String PRIVATE_KEY_TYPE = "private key";

    private String alias;

    private String type;

    private Date created;

    public KeyEntryInfo(String alias, String type, Date created) {
        this.alias = alias;
        this.type = type;
        this.created = created;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getType() {
        return this.type;
    }

    public Date getCreated() {
        return this.created;
    }

    public boolean isTrustedCertificate() {
        return type.equals(TRUSTED_CERT_TYPE);
    }

    public boolean isPrivateKey() {
        return type.equals(PRIVATE_KEY_TYPE);
    }
}
