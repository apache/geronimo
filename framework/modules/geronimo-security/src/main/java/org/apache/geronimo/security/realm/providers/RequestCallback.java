package org.apache.geronimo.security.realm.providers;

import javax.security.auth.callback.Callback;
import javax.servlet.http.HttpServletRequest;

public class RequestCallback implements Callback {
    private HttpServletRequest httpRequest;
    
    public HttpServletRequest getRequest(){
        return httpRequest;
    }
    
    public void setRequest(HttpServletRequest httpRequest){
        this.httpRequest=httpRequest;
    }

}
