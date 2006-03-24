/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.console.car;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.Iterator;

/**
 * Servlet that lets you download a CAR from the repository
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class CARExportServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String configId = request.getParameter("configId");
        if(configId == null) {
            throw new ServletException("No configId specified for CAR download");
        }
        Kernel kernel = KernelRegistry.getSingleKernel();
        Set set = kernel.listGBeans(new GBeanQuery(null, ConfigurationStore.class.getName()));
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            if(name.getKeyProperty(NameFactory.J2EE_NAME).equals("Local")) {
                response.setContentType("application/zip");
                try {
                    kernel.invoke(name, "exportConfiguration", new Object[]{configId, response.getOutputStream()}, new String[]{String.class.getName(), OutputStream.class.getName()});
                    return;
                } catch (Exception e) {
                    throw new IOException("Unable to write ZIP file: "+e.getMessage());
                }
            }
        }
        response.setContentType("text/html");
        response.getWriter().println("<html><body><p>Error: no LocalConfigStore found in kernel</p></body></html>");
    }
}
