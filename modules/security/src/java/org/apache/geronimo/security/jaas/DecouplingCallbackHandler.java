/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * This callback handler separates the process of obtaining callbacks from
 * the user from the process of providing the user's values to the login
 * module.  This means the JaasLoginService can figure out what callbacks
 * the module wants and prompt the user in advance, and then turn around
 * and pass those values to the login module, instead of actually prompting
 * the user at the mercy of the login module.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DecouplingCallbackHandler implements CallbackHandler {
    private Callback[] source;
    private boolean exploring = true;

    public DecouplingCallbackHandler() {
    }

    public void handle(Callback[] callbacks)
            throws IllegalArgumentException, UnsupportedCallbackException {
        if (exploring) {
            source = callbacks;
            throw new UnsupportedCallbackException(callbacks.length > 0 ? callbacks[0] : null, "DO NOT PROCEED WITH THIS LOGIN");
        } else {
            if(callbacks.length != source.length) {
                throw new IllegalArgumentException("Mismatched callbacks");
            }
            for (int i = 0; i < callbacks.length; i++) {
                callbacks[i] = source[i];
            }
        }
    }

    public void load(Callback[] callbacks) throws IllegalArgumentException {
        if(callbacks.length != source.length) {
            throw new IllegalArgumentException("Mismatched callbacks");
        }
        for (int i = 0; i < callbacks.length; i++) {
            source[i] = callbacks[i];
        }
    }


    /**
     * While we're exploring, we'll discover new callbacks that the server
     * login module wants.  While not exploring, we'll actually set
     * values for the server callbacks.
     */
    public void setExploring() {
        exploring = true;
        source = null;
    }

    /**
     * Indicates that the exploring phase is over.
     */ 
    public Callback[] finalizeCallbackList() {
        exploring = false;
        return source;
    }
}
