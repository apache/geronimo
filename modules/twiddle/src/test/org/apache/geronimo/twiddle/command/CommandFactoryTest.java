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

import org.apache.geronimo.twiddle.config.CommandConfig;
import org.apache.geronimo.twiddle.config.Attribute;

/**
 * Tests for <code>CommandFactory</code>.
 *
 * @version <code>$Id: CommandFactoryTest.java,v 1.2 2003/08/13 11:48:57 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CommandFactoryTest
    extends TestCase
{
    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return new TestSuite(CommandFactoryTest.class);
    }
    
    /**
     * Construct a new instance of this test case.
     *
     * @param name  Name of the test case
     */
    public CommandFactoryTest(final String name)
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
    
    public void testCreateCommand() throws Exception
    {
        CommandConfig config = new CommandConfig();
        String name = "mytest";
        String desc = "my test description";
        String type = "org.apache.geronimo.twiddle.command.TestCommand";
        config.setName(name);
        config.setDescription(desc);
        config.setCode(type);
        
        CommandInfo protoInfo = new CommandInfo(config);
        Command command = protoInfo.getPrototype();
        
        // Verify command info
        CommandInfo info = command.getCommandInfo();
        Assert.assertNotNull(command.getCommandInfo());
        Assert.assertEquals(name, info.getName());
        Assert.assertEquals(desc, info.getDescription());
        
        // Verify the class type
        Assert.assertEquals(type, command.getClass().getName());
        
        command.execute(new String[0]);
    }
    
    public void testCreateCommandWithAttribute() throws Exception
    {
        CommandConfig config = new CommandConfig();
        config.setName("mytest");
        config.setCode("org.apache.geronimo.twiddle.command.TestCommand");
        
        Attribute attr = new Attribute();
        String text = "this is the value for the text attribute";
        attr.setName("text");
        attr.setContent(text);
        config.addAttribute(attr);
        
        CommandInfo protoInfo = new CommandInfo(config);
        Command command = protoInfo.getPrototype();
        
        // Verify that the text attribute was set correctly
        Assert.assertEquals(text, ((TestCommand)command).getText());
        
        command.execute(new String[0]);
    }
}

