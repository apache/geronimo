/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.twiddle.command;

import junit.framework.TestCase;

/**
 * Tests for <code>CommandPathParser</code>.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class CommandPathParserTest
    extends TestCase
{
    /**
     * Set up instance variables required by this test case.
     */
    protected void setUp()
    {
    }
    
    /**
     * Tear down instance variables required by this test case.
     */
    protected void tearDown()
    {
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                               Tests                                 //
    /////////////////////////////////////////////////////////////////////////
    
    public void testRoot()
    {
        CommandPathParser parser = new CommandPathParser("/");
        String[] elements = parser.elements();
        
        assertEquals(true, parser.isAbsolute());
        assertEquals(1, elements.length);
        assertEquals("/", elements[0]);
        assertEquals("/",parser.getPath());
    }

    public void testTrailing()
    {
        CommandPathParser parser = new CommandPathParser("/one/");
        String[] elements = parser.elements();
        
        assertEquals(true, parser.isAbsolute());
        assertEquals(2, elements.length);
        assertEquals("/", elements[0]);
        assertEquals("one", elements[1]);
        assertEquals("/one/",parser.getPath());
    }
    
    public void testAbsolute2()
    {
        CommandPathParser parser = new CommandPathParser("/one");
        String[] elements = parser.elements();
        
        assertEquals(true, parser.isAbsolute());
        assertEquals(2, elements.length);
        assertEquals("/", elements[0]);
        assertEquals("one", elements[1]);
        assertEquals("/one",parser.getPath());
    }

    public void testAbsolute3()
    {
        CommandPathParser parser = new CommandPathParser("/one/two");
        String[] elements = parser.elements();
        
        assertEquals(true, parser.isAbsolute());
        assertEquals(3, elements.length);
        assertEquals("/", elements[0]);
        assertEquals("one", elements[1]);
        assertEquals("two", elements[2]);
        assertEquals("/one/two",parser.getPath());
    }

    public void testAbsolute4()
    {
        CommandPathParser parser = new CommandPathParser("/one/two/three");
        String[] elements = parser.elements();
        
        assertEquals(true, parser.isAbsolute());
        assertEquals(4, elements.length);
        assertEquals("/", elements[0]);
        assertEquals("one", elements[1]);
        assertEquals("two", elements[2]);
        assertEquals("three", elements[3]);
        assertEquals("/one/two/three",parser.getPath());
    }
    
    public void testRelative1()
    {
        CommandPathParser parser = new CommandPathParser("one");
        String[] elements = parser.elements();
        
        assertEquals(false, parser.isAbsolute());
        assertEquals(1, elements.length);
        assertEquals("one", elements[0]);
        assertEquals("one",parser.getPath());
    }

    public void testRelative2()
    {
        CommandPathParser parser = new CommandPathParser("one/two");
        String[] elements = parser.elements();
        
        assertEquals(false, parser.isAbsolute());
        assertEquals(2, elements.length);
        assertEquals("one", elements[0]);
        assertEquals("two", elements[1]);
        assertEquals("one/two",parser.getPath());
    }

    public void testRelative3()
    {
        CommandPathParser parser = new CommandPathParser("one/two/three");
        String[] elements = parser.elements();
        
        assertEquals(false, parser.isAbsolute());
        assertEquals(3, elements.length);
        assertEquals("one", elements[0]);
        assertEquals("two", elements[1]);
        assertEquals("three", elements[2]);
        assertEquals("one/two/three",parser.getPath());
    }
}