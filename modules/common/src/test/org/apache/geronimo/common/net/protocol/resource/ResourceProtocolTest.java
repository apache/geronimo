/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.1 $ $Date: 2003/08/30 11:58:43 $
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
