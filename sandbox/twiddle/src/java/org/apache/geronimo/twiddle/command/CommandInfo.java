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

import org.codehaus.classworlds.ClassWorld;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.config.CommandConfig;

/**
 * Information about a command.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public class CommandInfo
{
    /** The command configuration. */
    protected CommandConfig config;
    
    /** The classworld to load the command class from. */
    protected ClassWorld world;
    
    /** The command prototype. */
    protected Command prototype;
    
    /**
     * Construct a <code>CommandInfo</code> from the given config.
     *
     * @param config    The command configuration.
     * @param world     The classworld to load the command class from.
     */
    public CommandInfo(final CommandConfig config, final ClassWorld world)
    {
        if (config == null) {
            throw new NullArgumentException("config");
        }
        if (world == null) {
            throw new NullArgumentException("world");
        }
        
        this.config = config;
        this.world = world;
    }
    
    /**
     * Get the command configuration.
     *
     * @return The command configuration.
     */
    public CommandConfig getConfig()
    {
        return config;
    }
    
    /**
     * Get classworld to load the command class from.
     *
     * @return The classworld to load the command class from.
     */
    public ClassWorld getClassWorld()
    {
        return world;
    }
    
    /**
     * Create the command prototype.
     *
     * @return The command prototype.
     *
     * @throws CommandException     Failed to create prototype.
     */
    protected Command createPrototype() throws CommandException
    {
        CommandFactory factory = new CommandFactory(config, world);
        Command command = factory.create();
        command.setCommandInfo(this);
        
        return command;
    }
    
    /**
     * Get the command prototype.
     *
     * @return The command prototype.
     *
     * @throws CommandException     Failed to create prototype.
     */
    public Command getPrototype() throws CommandException
    {
        if (prototype == null) {
            prototype = createPrototype();
        }
        
        return prototype;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                               Helpers                               //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the name of the command.
     *
     * @return The name of the command.
     */
    public String getName()
    {
        return config.getName();
    }
    
    /**
     * Get the description of the command.
     *
     * @return The description of the command.
     */
    public String getDescription()
    {
        return config.getDescription();
    }
    
    /**
     * Check if this command has a configured description.
     *
     * @return True if the command has a configured description.
     */
    public boolean hasDescription()
    {
        return getDescription() != null;
    }
}
