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

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Strings;

import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandContainer;
import org.apache.geronimo.twiddle.command.CommandContext;
import org.apache.geronimo.twiddle.command.CommandException;
import org.apache.geronimo.twiddle.command.CommandNotFoundException;

import org.apache.geronimo.twiddle.console.IOContext;

/**
 * Executes commands.
 *
 * @version <code>$Id: CommandExecutor.java,v 1.3 2003/08/13 16:54:48 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CommandExecutor
{
    protected CommandContainer container;
    
    public CommandExecutor(final CommandContainer container)
    {
        if (container == null) {
            throw new NullArgumentException("container");
        }
        
        this.container = container;
    }
    
    public CommandContainer getCommandContainer()
    {
        return container;
    }
    
    public Command findCommand(final String name) throws CommandException
    {
        // name is checked by the container
        return container.findCommand(name);
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                          Command Execution                          //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Execute a command.
     *
     * @param command
     * @param args
     * @return
     *
     * @throws Exception
     */
    protected int execute(final Command command, final String[] args) throws Exception
    {
        // Set the context
        CommandContext ctx = new CommandContext() {
            public Environment getEnvironment()
            {
                //
                // TODO: Fix me :-P
                //
                return null;
            }
            
            public IOContext getIOContext()
            {
                return new IOContext();
            }
        };
        command.setCommandContext(ctx);
        
        // Execute the command
        int result = Command.FAILURE;
        try {
            result = command.execute(args);
        }
        finally {
            command.unsetCommandContext();
        }
        
        return result;
    }
    
    /**
     * Execute a command from the given arguments.
     *
     * @param args
     * @return
     *
     * @throws Exception
     */
    public int execute(final String[] args) throws Exception
    {
        if (args == null) {
            throw new NullArgumentException("args");
        }
        if (args.length == 0) {
            throw new RuntimeException("Arguments are empty");
        }
        
        // Locate the command
        Command command = findCommand(args[0]);
        
        // Setup the arguments for the command (strip off command name/path)
        String[] cargs = new String[args.length - 1];
        System.arraycopy(args, 1, cargs, 0, args.length - 1);
        
        // Execute the command
        return execute(command, cargs);
    }
    
    /**
     * Execute a command from the given raw input.
     *
     * @param input
     * @return
     *
     * @throws Exception
     */
    public int execute(final String input) throws Exception
    {
        if (input == null) {
            throw new NullArgumentException("input");
        }
        
        String[] args = Strings.split(input, " ");
        return execute(args);
    }
    
    /**
     * Execute a command at the given path with the given arguments.
     *
     * @param path
     * @param args
     * @return
     *
     * @throws Exception
     */
    public int execute(final String path, final String[] args) throws Exception
    {
        // path is checked by findCommand
        if (args == null) {
            throw new NullArgumentException("args");
        }
        if (args.length == 0) {
            throw new RuntimeException("Arguments are empty");
        }
        
        // Locate the command
        Command command = findCommand(path);
        
        // Execute the command
        return execute(command, args);
    }
    
    /**
     * Execute a command at the given path with the given raw input as arguments
     *
     * @param path
     * @param input
     * @return
     *
     * @throws Exception
     */
    public int execute(final String path, final String input) throws Exception
    {
        if (input == null) {
            throw new NullArgumentException("input");
        }
        
        String[] args = Strings.split(input, " ");
        return execute(path, args);
    }
}
