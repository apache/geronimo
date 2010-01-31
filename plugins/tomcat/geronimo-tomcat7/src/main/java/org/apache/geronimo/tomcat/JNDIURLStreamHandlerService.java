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
package org.apache.geronimo.tomcat;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import javax.naming.directory.DirContext;

import org.apache.naming.resources.DirContextURLConnection;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * URL handler for "jndi" protocol. 
 * 
 * @version $Rev$ $Date$
 */
public class JNDIURLStreamHandlerService extends AbstractURLStreamHandlerService implements BundleActivator {

    public URLConnection openConnection(URL url) throws IOException {        
        DirContext currentContext = DirContextURLStreamHandler.get();
        return new DirContextURLConnection(currentContext, url);
    }

    public void start(BundleContext context) throws Exception {
        Hashtable properties = new Hashtable();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] {"jndi"});
        context.registerService(URLStreamHandlerService.class.getName(), this, properties);
    }

    public void stop(BundleContext context) throws Exception {        
    }
   
}
