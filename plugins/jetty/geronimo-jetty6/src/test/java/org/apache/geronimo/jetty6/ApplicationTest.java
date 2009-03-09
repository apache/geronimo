/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.component.LifeCycle;


/**
 * @version $Rev$ $Date$
 */
public class ApplicationTest extends AbstractWebModuleTest {

    public void testApplication() throws Exception {
        JettyWebAppContext app = setUpAppContext(null, null, null, null, null, null, null, "war1/");

        setUpStaticContentServlet(app);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    public void testApplicationWithSessionHandler() throws Exception {
        preHandlerFactory = new MockPreHandlerFactory();
        sessionHandlerFactory = new MockSessionHandlerFactory();
        JettyWebAppContext app = setUpAppContext(null, null, null, null, null, null, null, "war1/");

        setUpStaticContentServlet(app);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    public class MockPreHandlerFactory implements PreHandlerFactory {
        public PreHandler createHandler() {
            return new AbstractPreHandler() {

                public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                        throws IOException, ServletException {
                    next.handle(target, request, response, dispatch);
                }

                public void addLifeCycleListener(Listener listener) {
                }

                public void removeLifeCycleListener(Listener listener) {
                }
            };
        }

    }

    public class MockSessionHandlerFactory implements SessionHandlerFactory {
        public SessionHandler createHandler(PreHandler preHandler) {
            return new SessionHandler();
        }
    }

}
