/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.jetty6;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.ServletHandler;

/**
 * @version $Rev: 449059 $ $Date: 2006-09-23 05:23:09 +1000 (Sat, 23 Sep 2006) $
 */
public class JettyServletHandler extends ServletHandler {

    private final PreHandler chainedHandler;
    
    public JettyServletHandler(PreHandler chainedHandler) {
        if (null == chainedHandler) {
            chainedHandler = new NoOpChainedHandler();
        }
        this.chainedHandler = chainedHandler;
        chainedHandler.setNextHandler(new ActualJettyServletHandler());
    }

    @Override
    public void handle(String target, HttpServletRequest request,HttpServletResponse response, int type)
        throws IOException {
        try {
            chainedHandler.handle(target, request, response, type);
        } catch (ServletException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    protected void doHandle(String target, HttpServletRequest request, HttpServletResponse response, int type)
        throws IOException {
        super.handle(target, request, response, type);
    }
    
    private class ActualJettyServletHandler extends AbstractPreHandler {

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int type)
                throws IOException, ServletException {
            doHandle(target, request, response, type);
        }
    }

    private static class NoOpChainedHandler extends AbstractPreHandler {

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int type)
                throws IOException, ServletException {
            next.handle(target, request, response, type);
        }
    }
    
}
