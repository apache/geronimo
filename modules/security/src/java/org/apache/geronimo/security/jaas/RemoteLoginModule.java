/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
package org.apache.geronimo.security.jaas;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.remoting.jmx.RemoteLoginServiceFactory;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:39 $
 */
public class RemoteLoginModule implements javax.security.auth.spi.LoginModule {

    public final static String MODULE_IS_LOCAL = "org.apache.geronimo.security.jaas.RemoteLoginModule.MODULE_IS_LOCAL";
    public final static String LOGIN_URI = "org.apache.geronimo.security.jaas.RemoteLoginModule.LOGIN_URI";
    public final static String LOGIN_SERVICE = "org.apache.geronimo.security.jaas.RemoteLoginModule.LOGIN_SERVICE";
    private boolean debug;
    private URI connectURI;
    private LoginServiceMBean remoteLoginService;
    private LoginModule wrapper;
    private static ClassLoader classLoader;

    static {
        classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        String uri = (String) options.get("uri");
        String realm = (String) options.get("realm");

        if (uri == null) throw new GeronimoSecurityException("Initialize error: uri to sercurity service is not set");
        if (realm == null) throw new GeronimoSecurityException("Initialize error: realm name not specified");

        try {
            connectURI = new URI(uri);
            remoteLoginService = RemoteLoginServiceFactory.create(connectURI.getHost(), connectURI.getPort());

            SerializableACE entry = remoteLoginService.getAppConfigurationEntry(realm);

            final String finalClass = entry.getLoginModuleName();
            wrapper = (LoginModule) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                    return Class.forName(finalClass, true, classLoader).newInstance();
                }
            });

            HashMap map = new HashMap(entry.getOptions());
            map.put(LOGIN_SERVICE, remoteLoginService);

            wrapper.initialize(subject, callbackHandler, sharedState, map);

            if (debug) {
                System.out.print("[GeronimoLoginModule] Debug is  " + debug + " uri " + uri + " realm " + realm + "\n");
            }
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof InstantiationException) {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error:" + e.getCause().getMessage()).initCause(e.getCause());
            } else {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString()).initCause(e);
            }
        } catch (URISyntaxException e) {
            throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString()).initCause(e);
        }
    }

    public boolean login() throws LoginException {
        if (wrapper == null) throw new LoginException("RemoteLoginModule not properly initialzied");
        return wrapper.login();
    }

    public boolean commit() throws LoginException {
        if (wrapper == null) throw new LoginException("RemoteLoginModule not properly initialzied");
        return wrapper.commit();
    }

    public boolean abort() throws LoginException {
        if (wrapper == null) throw new LoginException("RemoteLoginModule not properly initialzied");
        return wrapper.abort();
    }

    public boolean logout() throws LoginException {
        if (wrapper == null) throw new LoginException("RemoteLoginModule not properly initialzied");
        return wrapper.logout();
    }
}
