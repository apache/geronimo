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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.geronimo.common.NullArgumentException;

/**
 * A container for commands.
 *
 * @version $Rev$ $Date$
 */
public class CommandContainer
{
    protected Map descriptors;
    
    public CommandContainer()
    {
        descriptors = new HashMap();
    }
    
    public CommandInfo addCommandInfo(final CommandInfo info)
    {
        if (info == null) {
            throw new NullArgumentException("info");
        }
        
        return (CommandInfo)descriptors.put(info.getName(), info);
    }
    
    public CommandInfo getCommandInfo(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return (CommandInfo)descriptors.get(name);
    }
    
    public boolean containsCommandInfo(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return descriptors.containsKey(name);
    }
    
    public CommandInfo removeCommandInfo(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return (CommandInfo)descriptors.remove(name);
    }
    
    public Iterator descriptors()
    {
        return descriptors.values().iterator();
    }
    
    public int size()
    {
        return descriptors.size();
    }
    
    public Command getCommand(final String name) throws CommandException
    {
        CommandInfo info = getCommandInfo(name);
        if (info == null) {
            return null;
        }
        
        Command prototype = info.getPrototype();
        
        return (Command)prototype.clone();
    }
    
    public Command findCommand(final String name) throws CommandException
    {
        Command command = getCommand(name);
        
        if (command == null) {
            throw new CommandNotFoundException(name);
        }
        
        return command;
    }
}
