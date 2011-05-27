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

package org.apache.geronimo.jetty8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.Request;


/**
 * @version $Rev$ $Date$
 */
public class ApplicationTest extends AbstractWebModuleTest {

    @Override
    protected void setUp() throws Exception {
        appPath = "war1";
        super.setUp();
    }

    public void testApplication() throws Exception {
        WebAppInfo webAppInfo = new WebAppInfo();
        setUpStaticContentServlet(webAppInfo);

        setUpAppContext(null, null, "policyContextID", null, "war1/", webAppInfo);


        HttpURLConnection connection = (HttpURLConnection) new URL(hostURL + "/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    public void testApplicationWithSessionHandler() throws Exception {
        preHandlerFactory = new MockPreHandlerFactory();
        sessionHandlerFactory = new MockSessionHandlerFactory();
        WebAppInfo webAppInfo = new WebAppInfo();
        setUpStaticContentServlet(webAppInfo);

        setUpAppContext(null, null, "policyContextID", null, "war1/", webAppInfo);

        HttpURLConnection connection = (HttpURLConnection) new URL(hostURL + "/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    public class MockPreHandlerFactory implements PreHandlerFactory {
        public PreHandler createHandler() {
            return new AbstractPreHandler() {

                public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                        throws IOException, ServletException {
                    next.handle(target, baseRequest, request, response);
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
