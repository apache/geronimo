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

package org.apache.geronimo.system.url;

import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.net.URLStreamHandler;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:31 $
 */
public class GeronimoURLFactoryTest extends TestCase {
    public void testForceInstall() throws Exception{
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                throw new RuntimeException("Bogus handler still installed");
            }
        });
        GeronimoURLFactory.forceInstall();
        new URL("ftp://www.apache.org/");
    }

    public void testSunURLHandlerAccess() throws Exception {
        new URL("ftp://www.apache.org/");
        new URL("http://www.apache.org/index.html");
        new URL("https://www.apache.org/index.html");
        new URL(" http://www.apache.org/index.html");
    }
}

