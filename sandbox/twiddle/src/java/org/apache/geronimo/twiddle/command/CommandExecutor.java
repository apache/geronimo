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

import org.apache.commons.lang.StringUtils;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.console.IOContext;

/**
 * Executes commands.
 *
 * @version $Rev$ $Date$
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
        
        String[] args = StringUtils.split(input, " ");
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
        
        String[] args = StringUtils.split(input, " ");
        return execute(path, args);
    }
}
