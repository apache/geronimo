package org.apache.geronimo.security.realm.providers;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.HttpServletRequest;

public class RequestCallbackHandler implements CallbackHandler{

    HttpServletRequest httpRequest;
    
    public RequestCallbackHandler(HttpServletRequest httpRequest){
        this.httpRequest=httpRequest;
    }
    
    public void handle(Callback callbacks[]) throws UnsupportedCallbackException{
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (callback instanceof RequestCallback) {
                RequestCallback rc = (RequestCallback) callback;
                rc.setRequest(httpRequest);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
