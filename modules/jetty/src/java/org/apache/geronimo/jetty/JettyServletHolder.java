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
package org.apache.geronimo.jetty;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.security.jacc.PolicyContext;
import java.io.IOException;

import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpRequest;


/**
 * This ServletHolder's sole purpose is to provide the thread's current
 * ServletHolder for realms that are interested in the current servlet, e.g.
 * current servlet name.
 * @see org.apache.geronimo.jetty.JAASJettyRealm#isUserInRole(java.security.Principal, java.lang.String)
 * @version $Revision: 1.2 $ $Date: 2004/07/27 03:33:53 $
 */
public class JettyServletHolder extends ServletHolder {

    private static final ThreadLocal currentServletHolder = new ThreadLocal();

    public JettyServletHolder() {
        super();
    }

    public JettyServletHolder(ServletHandler handler, String name, String className) {
        super(handler, name, className);
    }

    public JettyServletHolder(ServletHandler handler, String name, String className, String forcedPath) {
        super(handler,name, className, forcedPath);
    }

    /**
     * Service a request with this servlet.  Set the ThreadLocal to hold the
     * current JettyServletHolder.
     * @param request
     * @param response
     * @throws ServletException
     * @throws UnavailableException
     * @throws IOException
     */
    public void handle(ServletRequest request, ServletResponse response)
            throws ServletException, UnavailableException, IOException {

        currentServletHolder.set(this);
        PolicyContext.setHandlerData(ServletHttpRequest.unwrap(request));

        super.handle(request, response);       
    }

    /**
     * Provide the thread's current JettyServletHolder
     * @return the thread's current JettyServletHolder
     * @see org.apache.geronimo.jetty.JAASJettyRealm#isUserInRole(java.security.Principal, java.lang.String)
     */
    static JettyServletHolder getJettyServletHolder() {
        return (JettyServletHolder) currentServletHolder.get();
    }
}
