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

import java.io.Serializable;
import java.util.Arrays;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoPasswordCredential implements Destroyable, Serializable {

    private String userName;
    private char[] password;
    private boolean destroyed;

    public GeronimoPasswordCredential(String userName, char[] password) {
        assert userName != null;
        assert password != null;

        this.userName = userName;
        this.password = password.clone();
    }

    public String getUserName() {
        return userName;
    }

    public char[] getPassword() {
        return password.clone();
    }

    public void destroy() throws DestroyFailedException {
        userName = null;
        Arrays.fill(password, ' ');
        password = null;
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
