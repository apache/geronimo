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

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.twiddle.config.CommandConfig;

/**
 * Unit test for {@link CommandContainer} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/24 17:10:36 $
 */
public class CommandContainerTest extends TestCase {

    CommandContainer commandContainer;
    CommandInfo commandInfoOne;
    CommandInfo commandInfoTwo;

    protected void setUp() throws Exception {
        commandContainer = new CommandContainer();

        CommandConfig config = new CommandConfig();
        config.setName("Uno");
        config.setDescription("First Command");
        config.setCode("org.apache.geronimo.twiddle.command.TestCommand");
        commandInfoOne = new CommandInfo(config);

        config = new CommandConfig();
        config.setName("Due");
        config.setDescription("Second Command");
        config.setCode("org.apache.geronimo.twiddle.command.TestCommand");
        commandInfoTwo = new CommandInfo(config);

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
