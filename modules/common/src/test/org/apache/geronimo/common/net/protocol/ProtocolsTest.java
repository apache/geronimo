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

package org.apache.geronimo.common.net.protocol;

import java.util.List;
import java.util.LinkedList;

import junit.framework.TestCase;

/**
* Unit test for the {@link Protocols} class.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:05 $
 */
public class ProtocolsTest
    extends TestCase
{
    protected String originalHandlerPkgs;
    
    protected void setUp() throws Exception
    {
        originalHandlerPkgs = System.getProperty("java.protocol.handler.pkgs");
        System.getProperties().remove("java.protocol.handler.pkgs");
    }
    
    protected void tearDown() throws Exception
    {
        if (originalHandlerPkgs == null) {
            System.getProperties().remove("java.protocol.handler.pkgs");
        }
        else {
            System.setProperty("java.protocol.handler.pkgs", originalHandlerPkgs);
        }
    }
    
    public void testParseHandlerPackages_1()
    {
        List packages = Protocols.parseHandlerPackages("a");
        assertEquals(1, packages.size());
        assertEquals("a", packages.get(0));
    }
    
    public void testParseHandlerPackages_TrailingSep()
    {
        List packages = Protocols.parseHandlerPackages("a|");
        assertEquals(1, packages.size());
        assertEquals("a", packages.get(0));
    }
    
    public void testParseHandlerPackages_BeginingSep()
    {
        List packages = Protocols.parseHandlerPackages("|a");
        assertEquals(1, packages.size());
        assertEquals("a", packages.get(0));
    }
    
    public void testParseHandlerPackages_3()
    {
        List packages = Protocols.parseHandlerPackages("a|b|c");
        assertEquals(3, packages.size());
        assertEquals("a", packages.get(0));
        assertEquals("b", packages.get(1));
        assertEquals("c", packages.get(2));
    }
    
    public void testAppendHandlerPackage()
    {
        Protocols.appendHandlerPackage("a");
        Protocols.appendHandlerPackage("b");
        Protocols.appendHandlerPackage("c");
        
        List packages = Protocols.getHandlerPackages();
        assertEquals(3, packages.size());
        assertEquals("a", packages.get(0));
        assertEquals("b", packages.get(1));
        assertEquals("c", packages.get(2));
    }
    
    public void testPrependHandlerPackage()
    {
        Protocols.prependHandlerPackage("a");
        Protocols.prependHandlerPackage("b");
        Protocols.prependHandlerPackage("c");
        
        List packages = Protocols.getHandlerPackages();
        assertEquals(3, packages.size());
        assertEquals("c", packages.get(0));
        assertEquals("b", packages.get(1));
        assertEquals("a", packages.get(2));
    }
    
    public void testSetHandlerPackages()
    {
        List packages = Protocols.parseHandlerPackages("a|b|c");
        Protocols.setHandlerPackages(packages);
        
        String raw = System.getProperty(Protocols.HANDLER_PACKAGES);
        assertEquals("a|b|c", raw);
        
        packages = Protocols.getHandlerPackages();
        assertEquals(3, packages.size());
        assertEquals("a", packages.get(0));
        assertEquals("b", packages.get(1));
        assertEquals("c", packages.get(2));
    }
}
