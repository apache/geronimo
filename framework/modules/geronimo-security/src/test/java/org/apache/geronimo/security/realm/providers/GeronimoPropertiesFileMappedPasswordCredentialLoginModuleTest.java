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


package org.apache.geronimo.security.realm.providers;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoPropertiesFileMappedPasswordCredentialLoginModuleTest extends TestCase {
    private GeronimoPropertiesFileMappedPasswordCredentialLoginModule loginModule;
    private Set<NamedUsernamePasswordCredential> passwordCredentials;

    protected void setUp() {
        loginModule = new GeronimoPropertiesFileMappedPasswordCredentialLoginModule();
        passwordCredentials = new TreeSet<NamedUsernamePasswordCredential>(new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((NamedUsernamePasswordCredential)o1).getName().compareTo(((NamedUsernamePasswordCredential)o2).getName());
            }
        });
    }

    public void testParsingOne() throws Exception {
        loginModule.parseCredentials("foo:bar=baz", passwordCredentials);
        assertEquals(1, passwordCredentials.size());
        NamedUsernamePasswordCredential cred = passwordCredentials.iterator().next();
        checkCredential(cred, "foo", "bar", "baz");
    }
    public void testParsingTwo() throws Exception {
        loginModule.parseCredentials("foo:bar=baz,foo2:bar2=baz2", passwordCredentials);
        assertEquals(2, passwordCredentials.size());
        Iterator<NamedUsernamePasswordCredential> iterator = passwordCredentials.iterator();
        NamedUsernamePasswordCredential cred = iterator.next();
        checkCredential(cred, "foo", "bar", "baz");
        cred = iterator.next();
        checkCredential(cred, "foo2", "bar2", "baz2");
    }

    private void checkCredential(NamedUsernamePasswordCredential cred, String name, String user, String pw) {
        assertEquals(name, cred.getName());
        assertEquals(user, cred.getUsername());
        assertEquals(pw, new String(cred.getPassword()));
    }

}
