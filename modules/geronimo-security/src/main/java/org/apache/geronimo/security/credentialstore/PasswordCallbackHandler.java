/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.security.credentialstore;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

/**
 * @version $Rev$ $Date$
 */
public class PasswordCallbackHandler implements SingleCallbackHandler {

    private final char[] password;

    public PasswordCallbackHandler(char[] password) {
        this.password = password;
    }
    public PasswordCallbackHandler(String password) {
        this.password = password.toCharArray();
    }

    public void handle(Callback callback) {
        ((PasswordCallback)callback).setPassword(password);
    }

    public String getCallbackType() {
        return PasswordCallback.class.getName();
    }
}
