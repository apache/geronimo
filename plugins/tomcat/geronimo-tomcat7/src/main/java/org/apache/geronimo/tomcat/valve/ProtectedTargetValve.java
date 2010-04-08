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
package org.apache.geronimo.tomcat.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * Valve that prevents access to OSGI-INF and OSGI-OPT directories.
 *
 * @version $Rev$ $Date$
 */
public class ProtectedTargetValve extends ValveBase {

    public ProtectedTargetValve() {
        super(true);
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        // Disallow any direct access to resources under OSGI-INF or OSGI-OPT
        if (request != null) {
            MessageBytes requestPathMB = request.getRequestPathMB();
            if ((requestPathMB.startsWithIgnoreCase("/OSGI-INF/", 0))
                    || (requestPathMB.equalsIgnoreCase("/OSGI-INF"))
                    || (requestPathMB.startsWithIgnoreCase("/OSGI-OPT/", 0))
                    || (requestPathMB.equalsIgnoreCase("/OSGI-OPT"))) {
                notFound(response);
                return;
            }
        }

        getNext().invoke(request, response);
    }

    private void notFound(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        }
    }


}
