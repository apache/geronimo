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

package org.apache.geronimo.twiddle.command;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Tests for <code>CommandPathParser</code>.
 *
 * @version <code>$Id: CommandPathParserTest.java,v 1.3 2003/08/14 20:24:35 bsnyder Exp $</code>
 */
public class CommandPathParserTest
    extends TestCase
{
    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return new TestSuite(CommandPathParserTest.class);
    }
    
    /**
     * Construct a new instance of this test case.
     *
     * @param name  Name of the test case
     */
    public CommandPathParserTest(final String name)
    {
        super(name);
    }
    
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
        
        Assert.assertEquals(true, parser.isAbsolute());
        Assert.assertEquals(1, elements.length);
        Assert.assertEquals("/", elements[0]);
    }

    public void testTrailing()
    {
        CommandPathParser parser = new CommandPathParser("/one/");
        String[] elements = parser.elements();
        
        Assert.assertEquals(true, parser.isAbsolute());
        Assert.assertEquals(2, elements.length);
        Assert.assertEquals("/", elements[0]);
        Assert.assertEquals("one", elements[1]);
    }
    
    public void testAbsolute2()
    {
        CommandPathParser parser = new CommandPathParser("/one");
        String[] elements = parser.elements();
        
        Assert.assertEquals(true, parser.isAbsolute());
        Assert.assertEquals(2, elements.length);
        Assert.assertEquals("/", elements[0]);
        Assert.assertEquals("one", elements[1]);
    }

    public void testAbsolute3()
    {
        CommandPathParser parser = new CommandPathParser("/one/two");
        String[] elements = parser.elements();
        
        Assert.assertEquals(true, parser.isAbsolute());
        Assert.assertEquals(3, elements.length);
        Assert.assertEquals("/", elements[0]);
        Assert.assertEquals("one", elements[1]);
        Assert.assertEquals("two", elements[2]);
    }

    public void testAbsolute4()
    {
        CommandPathParser parser = new CommandPathParser("/one/two/three");
        String[] elements = parser.elements();
        
        Assert.assertEquals(true, parser.isAbsolute());
        Assert.assertEquals(4, elements.length);
        Assert.assertEquals("/", elements[0]);
        Assert.assertEquals("one", elements[1]);
        Assert.assertEquals("two", elements[2]);
        Assert.assertEquals("three", elements[3]);
    }
    
    public void testRelative1()
    {
        CommandPathParser parser = new CommandPathParser("one");
        String[] elements = parser.elements();
        
        Assert.assertEquals(false, parser.isAbsolute());
        Assert.assertEquals(1, elements.length);
        Assert.assertEquals("one", elements[0]);
    }

    public void testRelative2()
    {
        CommandPathParser parser = new CommandPathParser("one/two");
        String[] elements = parser.elements();
        
        Assert.assertEquals(false, parser.isAbsolute());
        Assert.assertEquals(2, elements.length);
        Assert.assertEquals("one", elements[0]);
        Assert.assertEquals("two", elements[1]);
    }

    public void testRelative3()
    {
        CommandPathParser parser = new CommandPathParser("one/two/three");
        String[] elements = parser.elements();
        
        Assert.assertEquals(false, parser.isAbsolute());
        Assert.assertEquals(3, elements.length);
        Assert.assertEquals("one", elements[0]);
        Assert.assertEquals("two", elements[1]);
        Assert.assertEquals("three", elements[2]);
    }
}

