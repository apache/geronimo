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

package org.apache.geronimo.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import junit.framework.TestCase;

/**
 * Unit tests for {@link RMIClassLoaderSpiImpl} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:30 $
 */
public class RMIClassLoaderSpiImplTest
    extends TestCase
{
    private String baseURL;
    private String normalizedBaseURL;
    
    protected void setUp() throws Exception
    {
        File dir = new File(System.getProperty("user.home"));
        
        baseURL = dir.toURL().toString();
        if (baseURL.endsWith("/")) {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }
        
        normalizedBaseURL = dir.toURI().toURL().toString();
        if (normalizedBaseURL.endsWith("/")) {
            normalizedBaseURL = normalizedBaseURL.substring(0, normalizedBaseURL.length() - 1);
        }
        
        System.out.println("Using base URL: " + baseURL);
        System.out.println("Using normalized base URL: " + normalizedBaseURL);
    }
    
    public void testNormalizeURL() throws MalformedURLException
    {
        URL url = new URL(baseURL + "/Apache Group/Geronimo");
        URL normal = RMIClassLoaderSpiImpl.normalizeURL(url);
        assertEquals(normalizedBaseURL + "/Apache%20Group/Geronimo", normal.toString());
    }
    
    public void testNormalizeCodebase() throws MalformedURLException
    {
        String codebase = baseURL + "/Apache Group/Geronimo " + baseURL + "/Apache Group/Apache2";
        
        String normal = RMIClassLoaderSpiImpl.normalizeCodebase(codebase);
        assertEquals(normalizedBaseURL + "/Apache%20Group/Geronimo " + 
                     normalizedBaseURL + "/Apache%20Group/Apache2", normal);
    }
}
