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

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;


/**
 * A class extension to <code>WebApplicationHandler</code> whose sole purpose
 * is to override the implementation of <code>newServletHolder()</code> so
 * that it returns an instance of <code>JettyServletHolder</code>.  The class
 * <code>JettyServletHolder</code> tracks which servlet is currently being
 * handled by the current thread.  This allows <code>JAASJettyRealm</code>
 * to obtain the name of the servlet that is being handled so that it can
 * generate the proper JACC permission.
 *
 * @version $Rev$ $Date$
 * @see org.apache.geronimo.jetty.JettyServletHolder
 * @see org.apache.geronimo.jetty.JAASJettyRealm#isUserInRole(java.security.Principal, java.lang.String)
 */
public class JettyWebAppHandler extends WebApplicationHandler {

    /**
     * Return an instance of <code>JettyServletHolder</code>.
     * <p/>
     * This method overrides <code>WebApplicationHandler</code>'s implementation.
     *
     * @param name         The name of the servlet.
     * @param servletClass The class name of the servlet.
     * @param forcedPath   If non null, the request attribute
     *                     javax.servlet.include.servlet_path will be set to this path before
     *                     service is called.
     * @return an instance of <code>JettyServletHolder</code>
     * @see org.mortbay.jetty.servlet.WebApplicationHandler#newServletHolder(java.lang.String, java.lang.String, java.lang.String)
     */
    public ServletHolder newServletHolder(String name, String servletClass, String forcedPath) {
        if (_nameMap.containsKey(name)) throw new IllegalArgumentException("Named servlet already exists: " + name);

        ServletHolder holder = new JettyServletHolder(this, name, servletClass, forcedPath);

        _nameMap.put(holder.getName(), holder);

        return holder;
    }
}
