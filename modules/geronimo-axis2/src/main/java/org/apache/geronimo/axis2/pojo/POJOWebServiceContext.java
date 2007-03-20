package org.apache.geronimo.axis2.pojo;

import org.apache.axis2.jaxws.handler.LogicalMessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.security.Principal;

/**
 * Implementation of WebServiceContext for POJO WS to ensure that getUserPrincipal()
 * and isUserInRole() are properly handled.
 */
public class POJOWebServiceContext implements WebServiceContext {

    private MessageContext ctx;

    public POJOWebServiceContext(org.apache.axis2.context.MessageContext ctx) {
        this.ctx = new LogicalMessageContext(new org.apache.axis2.jaxws.core.MessageContext(ctx));
    }
    
    public final MessageContext getMessageContext() {
        return ctx;
    }

    private HttpServletRequest getHttpServletRequest() {
        MessageContext ctx = getMessageContext();
        return (ctx != null) ? (HttpServletRequest)ctx.get(HTTPConstants.MC_HTTP_SERVLETREQUEST) : null;
    }

    public final Principal getUserPrincipal() {
        HttpServletRequest request = getHttpServletRequest();
        return (request != null) ? request.getUserPrincipal() : null;
    }

    public final boolean isUserInRole(String user) {
        HttpServletRequest request = getHttpServletRequest();
        return (request != null) ? request.isUserInRole(user) : false;
    }

}
