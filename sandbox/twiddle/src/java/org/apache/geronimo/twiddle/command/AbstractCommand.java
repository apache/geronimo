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

import java.io.PrintWriter;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.CloneableObject;
import org.apache.geronimo.common.NullArgumentException;

/**
 * An abstract implementation of a <code>Command</code>.
 *
 * <p>Sub-classes only need to implement {@link Command#execute}.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:37 $
 */
public abstract class AbstractCommand
    extends CloneableObject
    implements Command
{
    /** Platform dependent line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /** The command logger. */
    protected Log log = LogFactory.getLog(this.getClass());
    
    /** The command information. */
    private CommandInfo info;
    
    /** The command context. */
    private CommandContext context;
    
    
    /////////////////////////////////////////////////////////////////////////
    //                              Command                                //
    /////////////////////////////////////////////////////////////////////////
    
    public void setCommandInfo(final CommandInfo info)
    {
        if (info == null) {
            throw new NullArgumentException("info");
        }
        
        this.info = info;
    }
    
    public CommandInfo getCommandInfo()
    {
        if (info == null) {
            throw new IllegalStateException("Command information was not set");
        }
        
        return info;
    }
    
    public void setCommandContext(final CommandContext ctx)
    {
        if (ctx == null) {
            throw new NullArgumentException("ctx");
        }
        
        this.context = ctx;
    }
    
    public void unsetCommandContext()
    {
        context = null;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                          Sub-class Helpers                          //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Provides sub-classes with access to the command context.
     *
     * @return The command context.
     *
     * @throws IllegalStateException    Command context has not been set or was unset.
     */
    protected CommandContext getCommandContext()
    {
        if (context == null) {
            throw new IllegalStateException("Command context has not been set or was unset");
        }
        
        return context;
    }
    
    /**
     * Provides sub-classes with the commands environment.
     *
     * @return The commands environment.
     */
    protected Environment getEnvironment()
    {
        return getCommandContext().getEnvironment();
    }
    
    /**
     * Provides sub-classes with the reader for the current context.
     *
     * @return The reader for the current context.
     */
    protected Reader getReader()
    {
        return getCommandContext().getIOContext().getReader();
    }
    
    /**
     * Provides sub-classes with the writer for the current context.
     *
     * @return The writer for the current context.
     */
    protected PrintWriter getWriter()
    {
        return getCommandContext().getIOContext().getWriter();
    }
    
    /**
     * Provides sub-classes with the error writer for the current context.
     *
     * @return The error writer for the current context.
     */
    protected PrintWriter getErrorWriter()
    {
        return getCommandContext().getIOContext().getErrorWriter();
    }
}
