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

package org.apache.geronimo.common.net.protocol.resource;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.util.Properties;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.geronimo.common.net.protocol.Protocols;

/**
 * Unit test for the 'resource' protocol.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:05 $
 */
public class ResourceProtocolTest
    extends TestCase
{
    protected void setUp() throws Exception
    {
        Protocols.appendHandlerPackage("org.apache.geronimo.common.net.protocol");
    }
    
    protected void tearDown() throws Exception
    {
        Protocols.setHandlerPackages(Protocols.getSystemHandlerPackages());
    }
    
    public void testCreateURL() throws MalformedURLException
    {
        URL url = new URL("resource:resource.properties");
    }
    
    public void testRead() throws Exception
    {
        URL url = new URL("resource:resource.properties");
        Properties props = new Properties();
        InputStream input = url.openConnection().getInputStream();
        try {
            props.load(input);
            assertEquals("whatever", props.getProperty("some.property"));
        }
        finally {
            input.close();
        }
    }
    
    public void testRead_FromClass() throws Exception
    {
        URL url = new URL("resource:resource.properties#" + getClass().getName());
        Properties props = new Properties();
        InputStream input = url.openConnection().getInputStream();
        try {
            props.load(input);
            assertEquals("fromclass", props.getProperty("some.property"));
        }
        finally {
            input.close();
        }
    }
}
