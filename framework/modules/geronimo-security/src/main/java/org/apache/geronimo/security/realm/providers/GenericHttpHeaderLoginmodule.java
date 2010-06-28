package org.apache.geronimo.security.realm.providers;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
/*
 * Parent class for all the generic header login modules.
 */
public abstract class GenericHttpHeaderLoginmodule {
    
    private static Log log = LogFactory.getLog(GenericHttpHeaderLoginmodule.class);
    
    protected Subject subject;
    protected String username;
    protected String headerNames;
    protected String authenticationAuthority;
    protected JCAManagedConnectionFactory factory;
    protected CallbackHandler callbackHandler;
    protected boolean loginSucceeded;
    protected HttpServletRequest httpRequest;
    protected Set<Principal> allPrincipals = new HashSet<Principal>();
    protected Set<String> groups = new HashSet<String>();
    
    public GenericHttpHeaderLoginmodule(){
        
    }
    
    protected void commitHelper(){
        for(String group:groups){
            allPrincipals.add(new GeronimoGroupPrincipal(group));
        }
        subject.getPrincipals().addAll(allPrincipals);
        subject.getPublicCredentials().add(username);
    }
    
    public void abortHelper(){
        allPrincipals.clear();
        groups.clear();
    }
    
    public void logoutHelper(){
        groups.clear();
        if(!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
    }
    
    public Map<String, String> matchHeaders(HttpServletRequest request, String[] headers) throws HeaderMismatchException{
        Map<String,String> headerMap= new HashMap<String, String>();
        for(String header:headers){
            String headerValue=request.getHeader(header);
            if(headerValue!=null){
                headerMap.put(header, headerValue);
            }
            else
                log.warn("An Unauthorized attempt has been made to access the protected resource from host "+request.getRemoteHost());
        }
        return headerMap;
    }

}
