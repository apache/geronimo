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

package org.apache.geronimo.twiddle.command;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.codehaus.classworlds.ClassWorld;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.twiddle.config.CommandConfig;

/**
 * Unit test for {@link CommandContainer} class.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public class CommandContainerTest extends TestCase {

    ClassWorld world;
    CommandContainer commandContainer;
    CommandInfo commandInfoOne;
    CommandInfo commandInfoTwo;

    protected void setUp() throws Exception {
        world = new ClassWorld();
        world.newRealm(Command.DEFAULT_CLASS_REALM);
        
        commandContainer = new CommandContainer();

        CommandConfig config = new CommandConfig();
        config.setName("Uno");
        config.setDescription("First Command");
        config.setCode("org.apache.geronimo.twiddle.command.TestCommand");
        commandInfoOne = new CommandInfo(config, world);

        config = new CommandConfig();
        config.setName("Due");
        config.setDescription("Second Command");
        config.setCode("org.apache.geronimo.twiddle.command.TestCommand");
        commandInfoTwo = new CommandInfo(config, world);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        commandContainer = null;
        commandInfoOne = null;
        commandInfoTwo = null;
        super.tearDown();
    }

    public void testAll() {

        try {
            commandContainer.addCommandInfo(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        commandContainer.addCommandInfo(commandInfoOne);
        commandContainer.addCommandInfo(commandInfoTwo);

        //tests getCommandInfo
        try {
            commandContainer.getCommandInfo(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        assertEquals("Uno", commandContainer.getCommandInfo("Uno").getName());

        //tests getCommand

        try {
            assertNull(commandContainer.getCommand("Cinque"));
            assertEquals("Uno", commandContainer.getCommand("Uno").getCommandInfo().getName());
        } catch (CommandException e) {

        }

        // tests findCommand
        try {
            commandContainer.findCommand("Cinque");
            fail("Expected CommandException");
        } catch (CommandException ignore) {
        }

        try {
            assertEquals("Uno", commandContainer.findCommand("Uno").getCommandInfo().getName());
        } catch (CommandException e) {
            fail("Unexpected CommandException" + e);
        }

        // tests containsCommandInfo
        assertTrue(commandContainer.containsCommandInfo("Due"));
        assertFalse(commandContainer.containsCommandInfo("Cinque"));

        try {
            commandContainer.containsCommandInfo(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        //tests descriptors
        Iterator itr = commandContainer.descriptors();
        try {
            //
            // jason: currently can not expect the order to be preserved
            //
            // assertEquals("Uno", ((CommandInfo) itr.next()).getName());
            // assertEquals("Due", ((CommandInfo) itr.next()).getName());
            itr.next();
            itr.next();
        } catch (NoSuchElementException e) {
            fail("Unexpected NoSuchElementException" + e);
        }
        try {
            itr.next();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException ignore) {}
        
        //tests removeCommandInfo
        assertEquals(2, commandContainer.size());
        commandContainer.removeCommandInfo("Due");
        assertEquals(1, commandContainer.size());
        commandContainer.removeCommandInfo("Uno");
        assertEquals(0, commandContainer.size());
        assertNull(commandContainer.removeCommandInfo("Cinque"));

        try {
            commandContainer.removeCommandInfo(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }
    }
}
