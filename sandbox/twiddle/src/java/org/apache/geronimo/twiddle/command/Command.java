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

/**
 * An abstraction of a command.
 *
 * <p>Commands follow the prototype pattern using the {@link #clone} method
 *    to create new instances from the prototype.
 *
 * @version $Rev$ $Date$
 */
public interface Command
    extends Cloneable
{
    /** The default class-realm id. */
    String DEFAULT_CLASS_REALM = "twiddle.default";
    
    /** Standard command success status code. */
    int SUCCESS = 0;
    
    /** Standard command failure status code. */
    int FAILURE = -1;
    
    /**
     * Set the information about the command.
     *
     * @param info  Information about the command.
     */
    void setCommandInfo(CommandInfo info);
    
    /**
     * Get information about the command.
     *
     * @return Information about the command.
     *
     * @throws IllegalStateException    Command information was not set.
     */
    CommandInfo getCommandInfo();
    
    /**
     * Set the context for the command.
     *
     * @param ctx   The context for the command.
     */
    void setCommandContext(CommandContext ctx);
    
    /**
     * Unset the context for the command.
     */
    void unsetCommandContext();
    
    /**
     * Execute the command.
     *
     * @param args  The arguments for the command.
     * @return      The status code for the command.
     *
     * @throws Exception    An unexpected failure has occured.
     */
    int execute(String[] args) throws Exception;
    
    /**
     * Clone this command prototype.
     *
     * @return A cloned instance of the command prototype.
     */
    Object clone();
}
