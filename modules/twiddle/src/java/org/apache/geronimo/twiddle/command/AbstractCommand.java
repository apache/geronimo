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

import java.io.PrintWriter;
import java.io.Reader;

import org.apache.geronimo.common.CloneableObject;
import org.apache.geronimo.common.NullArgumentException;

/**
 * An abstract implementation of a <code>Command</code>.
 *
 * <p>Sub-classes only need to implement {@link Command#execute} and
 *    can access the command context from {@link #getCommandContext}.
 *
 * @version <code>$Id: AbstractCommand.java,v 1.2 2003/08/13 15:18:47 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class AbstractCommand
    extends CloneableObject
    implements Command
{
    /** Platform dependent line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
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
