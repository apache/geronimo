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
 */
public class DecouplingCallbackHandler implements CallbackHandler {
    private Callback[] source;
    private boolean exploring = true;

    public DecouplingCallbackHandler() {
    }

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        if (exploring) {
            source = callbacks;
            throw new UnsupportedCallbackException(callbacks.length > 0 ? callbacks[0] : null, "DO NOT PROCEED WITH THIS LOGIN");
        } else {
            if(callbacks.length != source.length) {
                throw new IOException("Mismatched callbacks");
            }
            for (int i = 0; i < callbacks.length; i++) {
                callbacks[i] = source[i];
            }
        }
    }

    public void setExploring() {
        exploring = true;
        source = null;
    }

    public Callback[] finalizeCallbackList() {
        exploring = false;
        return source;
    }
}
