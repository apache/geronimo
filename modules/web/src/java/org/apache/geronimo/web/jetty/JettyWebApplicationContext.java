/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.web.jetty;

import javax.naming.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.security.GeronimoLoginConfiguration;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.WebApplicationContext;

import javax.management.MBeanServer;
import javax.security.jacc.PolicyContext;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2003/11/25 13:51:30 $
 */
public class JettyWebApplicationContext extends WebApplicationContext {
    private Context componentContext;
    private MBeanServer server;
    private String contextID;
    private Log log = LogFactory.getLog(JettyWebApplicationContext.class);

    public JettyWebApplicationContext() {
    }

    public JettyWebApplicationContext(String webApp) {
        super(webApp);
    }

    public Object enterContextScope(HttpRequest httpRequest, HttpResponse httpResponse) {
        log.info("Entering context " + httpRequest.getRequestURL());
        RootContext.setComponentContext((ReadOnlyContext)componentContext);
        GeronimoLoginConfiguration.setMBeanServer(server);
        PolicyContext.setContextID(contextID);
        return super.enterContextScope(httpRequest, httpResponse);
    }

    public void leaveContextScope(HttpRequest httpRequest, HttpResponse httpResponse, Object o) {
        super.leaveContextScope(httpRequest, httpResponse, o);
        RootContext.setComponentContext(null);
        PolicyContext.setContextID(null);
        log.info("Leaving context " + httpRequest.getRequestURL());
    }

    public Context getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Context componentContext) {
        this.componentContext = componentContext;
    }

    public MBeanServer getServer() {
        return server;
    }

    public void setServer(MBeanServer server) {
        this.server = server;
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }
}
