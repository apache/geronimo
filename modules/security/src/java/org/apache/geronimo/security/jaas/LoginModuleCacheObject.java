/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;


/**
 * @version $Rev$ $Date$
 */
class LoginModuleCacheObject {

    private final LoginModuleId loginModuleId;
    private String realmName;
    private Subject subject;
    private LoginModule loginModule;
    private CallbackHandler callbackHandler;
    private long created;
    private boolean done;

    LoginModuleCacheObject(LoginModuleId loginModuleId) {
        this.loginModuleId = loginModuleId;
        this.created = System.currentTimeMillis();
        this.done = false;
    }

    LoginModuleId getLoginModuleId() {
        return loginModuleId;
    }

    String getRealmName() {
        return realmName;
    }

    void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    long getCreated() {
        return created;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    Subject getSubject() {
        return subject;
    }

    void setSubject(Subject subject) {
        this.subject = subject;
    }

    LoginModule getLoginModule() {
        return loginModule;
    }

    void setLoginModule(LoginModule loginModule) {
        this.loginModule = loginModule;
    }

    CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }
}
