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

package org.apache.geronimo.classloaderserver.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.classloaderserver.ClassLoaderInfo;
import org.apache.geronimo.classloaderserver.ClassLoaderServer;
import org.apache.geronimo.classloaderserver.ClassLoaderServerException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.jetty.connector.JettyConnector;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;


/**
 *
 * @version $Rev: 109957 $ $Date: 2004-12-06 18:52:06 +1100 (Mon, 06 Dec 2004) $
 */
public class HTTPClassLoaderServer implements ClassLoaderServer, GBeanLifecycle {
    private final Log log = LogFactory.getLog(HTTPClassLoaderServer.class);
    
    private final JettyContainer jettyContainer;
    private final JettyConnector jettyConnector;
    private final Map infos = new HashMap();
    private String protocol;
    private String host;
    private int port;
        
    public HTTPClassLoaderServer(JettyContainer jettyContainer, JettyConnector jettyConnector) {
        this.jettyContainer = jettyContainer;
        this.jettyConnector = jettyConnector;
    }

    public void export(ClassLoaderInfo info) throws ClassLoaderServerException {
        String id = getID(info);
        ClassLoaderContext context = new ClassLoaderContext(id, info.getClassLoader());
        synchronized(infos) {
            if (infos.containsKey(id)) {
                throw new ClassLoaderServerException("ClassLoader with ID " + id + " already defined");
            }
            infos.put(id, context);
        }
        jettyContainer.addContext(context);
        try {
            context.start();
        } catch (Exception e) {
            throw new ClassLoaderServerException("Cannot start ClassLoaderContext", e);
        }
        
        URL url;
        try {
            url = new URL(protocol, host, port, id);
        } catch (MalformedURLException e) {
            throw new ClassLoaderServerException("Cannot build baseURL", e);
        }
        info.setClassLoaderServerURLs(new URL[] {url});
    }

    public void unexport(ClassLoaderInfo info) throws ClassLoaderServerException {
        String id = getID(info);
        ClassLoaderContext context;
        synchronized(infos) {
            context = (ClassLoaderContext) infos.remove(id);
        }
        if (null == context) {
            throw new ClassLoaderServerException("ClassLoader with ID " + id + " is undefined");
        }
        try {
            context.stop();
        } catch (InterruptedException e) {
            log.error("Error while stopping ClassLoaderContext", e);
        }
        jettyContainer.removeContext(context);
        info.setClassLoaderServerURLs(null);
    }

    public void doStart() throws Exception {
        protocol = jettyConnector.getDefaultScheme();
        host = jettyConnector.getHost();
        port = jettyConnector.getPort();
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }
    
    private String getID(ClassLoaderInfo info) {
        String id = info.getID().toString().replace('/', '.');
        return "/HTTPClassLoaderServer[" + id + "]/";
    }
    
    private static class ClassLoaderContext extends HttpContext {
        private ClassLoaderContext(String contextPath, ClassLoader cl) {
            setContextPath(contextPath);
            addHandler(new ClassLoaderHandler(cl));
        }
    }
    
    private static class ClassLoaderHandler implements HttpHandler {
        private final ClassLoader cl;
        private HttpContext httpContext;
        private boolean started;
        
        private ClassLoaderHandler(ClassLoader cl) {
            this.cl = cl;
        }
        
        public String getName() {
            return "ClassLoaderHandler";
        }

        public HttpContext getHttpContext() {
            return httpContext;
        }
        
        public void initialize(HttpContext httpContext) {
            this.httpContext = httpContext;
        }

        public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
            String method=request.getMethod();
            if (false == method.equals(HttpRequest.__GET)) {
                response.sendError(HttpResponse.__405_Method_Not_Allowed);
                return;
            }
           
            String resource = pathInContext.substring(1);
            
            InputStream in = cl.getResourceAsStream(resource);
            if (null == in) {
                response.sendError(HttpResponse.__404_Not_Found);
                return;
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            try {
                int read = 0;
                while (0 < (read = in.read(buffer))) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException e) {
                response.sendError(HttpResponse.__500_Internal_Server_Error);
                return;
            }
            
            response.setContentLength(out.size());
            OutputStream respOut = response.getOutputStream();
            out.writeTo(respOut);
        }

        public void start() throws Exception {
            started = true;
        }

        public void stop() throws InterruptedException {
            started = false;
        }

        public boolean isStarted() {
            return started;
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("HTTP ClassLoader Server", HTTPClassLoaderServer.class);

        infoBuilder.addOperation("export", new Class[] {ClassLoaderInfo.class});
        infoBuilder.addOperation("unexport", new Class[] {ClassLoaderInfo.class});
        
        infoBuilder.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("JettyConnector", JettyConnector.class, NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.setConstructor(new String[] {"JettyContainer", "JettyConnector"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

