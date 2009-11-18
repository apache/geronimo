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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class KerberosLoginModule implements LoginModule {

    private Subject subject;
    private LoginModule krb5LoginModule;
    private Subject krb5Subject;
    private Principal addOnPrincipal;
    
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        String krb5LoginModuleClass = (String) options.get("krb5LoginModuleClass");
        try {
            krb5LoginModule = (LoginModule)Class.forName(krb5LoginModuleClass).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to configure kerberos login module: " + e.getMessage(), e);
        }
        
        Map options1 = new HashMap();
        for(Object key : options.keySet()) {
            String key1 = (String) key;
            if(key1.startsWith("krb_")) {
                options1.put(key1.substring(4), options.get(key1));
            }
        }
	
        krb5Subject = new Subject();
        krb5LoginModule.initialize(krb5Subject, callbackHandler, sharedState, options1);
        String addOnPrincipalClass = (String) options.get("addOnPrincipalClass");
        String addOnPrincipalName = (String) options.get("addOnPrincipalName");
        if(addOnPrincipalClass != null && !addOnPrincipalClass.equals("")) {
            try {
                addOnPrincipal = (Principal) Class.forName(addOnPrincipalClass).getConstructor(String.class).newInstance(addOnPrincipalName);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to configure kerberos login module: " + e.getMessage(), e);
            }
        }
    }

    public boolean login() throws LoginException {
        return krb5LoginModule.login();
    }

    public boolean commit() throws LoginException {
        boolean result = krb5LoginModule.commit();
        if(result) {
            if(addOnPrincipal != null) subject.getPrincipals().add(addOnPrincipal);
            subject.getPrincipals().addAll(krb5Subject.getPrincipals());
            subject.getPublicCredentials().addAll(krb5Subject.getPublicCredentials());
            subject.getPrivateCredentials().addAll(krb5Subject.getPrivateCredentials());
        }
        return result;
    }

    public boolean abort() throws LoginException {
        return krb5LoginModule.abort();
    }

    public boolean logout() throws LoginException {
        if(!subject.isReadOnly()) {
            // Remove principals and credentials added by this LoginModule
            if(addOnPrincipal != null) subject.getPrincipals().remove(addOnPrincipal);
            subject.getPrincipals().removeAll(krb5Subject.getPrincipals());
            subject.getPublicCredentials().removeAll(krb5Subject.getPublicCredentials());
            subject.getPrivateCredentials().removeAll(krb5Subject.getPrivateCredentials());
        }
        return krb5LoginModule.logout();
    }
}
