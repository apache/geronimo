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

package org.apache.geronimo.twiddle;

import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.config.Configuration;
import org.apache.geronimo.twiddle.config.Configurator;

import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandContainer;
import org.apache.geronimo.twiddle.command.CommandExecutor;
import org.apache.geronimo.twiddle.command.CommandException;

import org.apache.geronimo.twiddle.console.IOContext;

/**
 * <em>Twiddle</em> is a command processor.
 *
 * <p><em>Twiddle</em> is a facade over the various components of the 
 *    command processor, it serves only to facilitate their operation and to
 *    provide a simple API to execute commands (hence facade).
 *
 * @version <tt>$Revision: 1.1 $ $Date: 2004/01/23 02:57:16 $</tt>
 */
public class Twiddle
{
    public static final String TWIDDLE_HOME = "twiddle.home";
    
    private static final Log log = LogFactory.getLog(Twiddle.class);
    
    /** The input/output context. */
    protected IOContext io;
    
    /** The class world to isolate classes. */
    protected ClassWorld world;
    
    /** The command container. */
    protected CommandContainer container;
    
    /** The command executor. */
    protected CommandExecutor executor;
    
    /**
     * Construct a <code>Twiddle</code> command processor.
     */
    public Twiddle(final IOContext io, final ClassWorld world)
    {
        if (io == null) {
            throw new NullArgumentException("io");
        }
        if (world == null) {
            throw new NullArgumentException("world");
        }
        
        this.io = io;
        this.world = world;
        this.container = new CommandContainer();
        this.executor = new CommandExecutor(container);
        
        // Make sure the default realm is there
        try {
            world.newRealm(Command.DEFAULT_CLASS_REALM);
            log.debug("Created new default class-realm");
        }
        catch (DuplicateRealmException ignore) {
            // default realm already exists
            log.debug("Default class-realm already exists, using it");
        }
    }
    
    /**
     * Construct a <code>Twiddle</code> command processor using system
     * defaults for the input/output context.
     */
    public Twiddle(final ClassWorld world)
    {
        this(new IOContext(System.in, System.out, System.err), world);
    }
    
    /**
     * Construct a <code>Twiddle</code> command processor using system
     * defaults for the input/output context and a default class world.
     */
    public Twiddle()
    {
        this(new ClassWorld());
    }
    
    /**
     * Get the input/output context.
     *
     * @return The input/output context.
     */
    public IOContext getIOContext()
    {
        return io;
    }
    
    /**
     * Get the class world.
     *
     * @return The class world.
     */
    public ClassWorld getClassWorld()
    {
        return world;
    }
    
    /**
     * Get the command container.
     *
     * @return The command container.
     */
    public CommandContainer getCommandContainer()
    {
        return container;
    }
    
    /**
     * Get the command executor.
     *
     * @return The command executor.
     */
    public CommandExecutor getCommandExecutor()
    {
        return executor;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                             Configuration                           //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Configure <em>Twiddle</em> from the given configuration metadata.
     *
     * @param config    Configuration metadata.
     *
     * @throws CommandException     Failed to configure.
     */
    public void configure(final Configuration config) throws CommandException
    {
        log.debug("Configuring...");
        
        Configurator c = new Configurator(this);
        c.configure(config);
        
        log.debug("Configured");
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                          Command Execution                          //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Execute a command line.
     *
     * <p>The first argument is assumed to be the command name.
     *
     * @param args  The command line.
     * @return      The command status code.
     *
     * @throws Exception    An unhandled command failure has occured.
     */
    public int execute(final String[] args) throws Exception
    {
        return executor.execute(args);
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                                 Misc                                //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the fancy <em>Twiddle</em> banner text.
     *
     * @return The fancy <em>Twiddle</em> banner text.
     */
    public static String getBanner()
    {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        
        out.println(" _____          _     _     _ _");
        out.println("|_   _|_      _(_) __| | __| | | ___");
        out.println("  | | \\ \\ /\\ / / |/ _` |/ _` | |/ _ \\");
        out.println("  | |  \\ V  V /| | (_| | (_| | |  __/");
        out.print("  |_|   \\_/\\_/ |_|\\__,_|\\__,_|_|\\___|");
        out.flush();
        
        return writer.toString();
    }
    
    /**
     * Get the <em>Twiddle</em> home directory
     *
     * @return The <em>Twiddle</em> home directory
     *
     * @throws RuntimeException     Unable to determine home dir.
     */
    public static File getHomeDir()
    {
        // Determine what our home directory is
        String temp = System.getProperty(TWIDDLE_HOME);
        File dir = null;
        
        try {
            if (temp == null) {
                String path = Twiddle.class.getProtectionDomain().getCodeSource().getLocation().getFile();
                path = URLDecoder.decode(path, "UTF-8");
                
                // home dir is expected to be lib/..
                dir = new File(path).getParentFile().getParentFile();
            }
            else {
                dir = new File(temp);
            }
            
            // Make sure the home dir does not have any ../ bits
            dir = dir.getCanonicalFile();
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to determine home dir", e);
        }
        
        return dir;
    }
    
    /**
     * Get the <em>Twiddle</em> home URL
     *
     * @return The <em>Twiddle</em> home URL
     *
     * @throws RuntimeException     Unable to determine home URL.
     */
    public static URL getHomeURL()
    {
        try {
            return getHomeDir().toURL();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Unable to determine home URL", e);
        }
    }
}
