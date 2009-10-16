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
package org.apache.geronimo.security.jaas;

import java.io.Serializable;
import java.util.Arrays;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;


/**
 * A username/password credential.  Used to store the username/password in the
 * Subject's private credentials.
 *
 * @version $Revision$ $Date$
 */
public class UsernamePasswordCredential implements Destroyable, Refreshable, Serializable {

    private String username;
    private char[] password;
    private boolean destroyed;

    public UsernamePasswordCredential(String username, char[] password) {
        assert username != null;
        assert password != null;

        this.username = username;
        this.password = new char[password.length];
        System.arraycopy(password, 0, this.password, 0, password.length);
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public void destroy() throws DestroyFailedException {
        Arrays.fill(password, ' ');
        
        username = null;
        password = null;
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void refresh() throws RefreshFailedException {
    }

    public boolean isCurrent() {
        return !destroyed;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsernamePasswordCredential)) return false;

        final UsernamePasswordCredential credential = (UsernamePasswordCredential) o;

        if (destroyed != credential.destroyed) return false;
        if (!Arrays.equals(password, credential.password)) return false;
        if (username != null ? !username.equals(credential.username) : credential.username != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (username != null ? username.hashCode() : 0);
        result = 29 * result + (destroyed ? 1 : 0);
        return result;
    }
}
