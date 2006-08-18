/**
 *
 * Copyright 2006 The Apache Software Foundation
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationHandler;

/**
 * @version $Rev$ $Date$
 */
public class JettyWebApplicationHandler extends WebApplicationHandler {

    /**
     * Wrap the dispatch call to prevent leaking Subject back into the environemnt.
     * 
     * @param pathInContext
     * @param request
     * @param response
     * @param servletHolder
     * @param type
     * @throws ServletException
     * @throws UnavailableException
     * @throws IOException
     */
    protected void dispatch(String pathInContext,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            ServletHolder servletHolder,
                            int type)
        throws ServletException, UnavailableException, IOException
    {
        Callers oldCallers = ContextManager.getCallers();
        try {
            super.dispatch(pathInContext, request, response, servletHolder, type);
        } finally {
            ContextManager.popCallers(oldCallers);
        }

    }
}
