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

import org.codehaus.classworlds.ClassWorld;

import org.apache.geronimo.twiddle.config.CommandConfig;
import org.apache.geronimo.twiddle.config.Attribute;

import org.apache.geronimo.common.NullArgumentException;

/**
 * Tests for <code>CommandFactory</code>.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class CommandFactoryTest
    extends TestCase
{
    protected ClassWorld world;
    
    /**
     * Set up instance variables required by this test case.
     */
    protected void setUp() throws Exception
    {
        world = new ClassWorld();
        world.newRealm(Command.DEFAULT_CLASS_REALM);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    protected void tearDown()
    {
        world = null;
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

        CommandInfo protoInfo = new CommandInfo(config, world);
        assertEquals(config,protoInfo.getConfig());
        assertTrue(protoInfo.hasDescription());
        Command command = protoInfo.getPrototype();

        try {
            new CommandInfo(null, null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        // Verify command info
        CommandInfo info = command.getCommandInfo();
        assertNotNull(command.getCommandInfo());
        assertEquals(name, info.getName());
        assertEquals(desc, info.getDescription());

        // Verify the class type
        assertEquals(type, command.getClass().getName());

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

        CommandInfo protoInfo = new CommandInfo(config, world);
        Command command = protoInfo.getPrototype();

        // Verify that the text attribute was set correctly
        assertEquals(text, ((TestCommand)command).getText());

        command.execute(new String[0]);
    }

    public void testCommandFactory() {
        CommandFactory commandFactory;

        try {
            new CommandFactory(null, null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        CommandConfig config = new CommandConfig();
        String name = "mytest";
        String desc = "my test description";
        String type = "org.apache.geronimo.twiddle.command.TestCommand";
        config.setName(name);
        config.setDescription(desc);
        config.setCode(type);

        commandFactory = new CommandFactory(config, world);
        assertEquals(config,commandFactory.getConfig());
        try {
            commandFactory.create();
        } catch (CommandException e) {
            fail("Unexpected CommandException: " + e);
        }

        try {
            new CommandFactory(new CommandConfig(), world).create();
            fail("Expected CommandException");
        } catch (CommandException ignore) {
        }
    }
}

