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

import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.config.Configuration;
import org.apache.geronimo.twiddle.config.ConfigurationReader;
import org.apache.geronimo.twiddle.config.Properties;
import org.apache.geronimo.twiddle.config.Property;
import org.apache.geronimo.twiddle.config.Commands;
import org.apache.geronimo.twiddle.config.CommandConfig;

import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.command.CommandContainer;
import org.apache.geronimo.twiddle.command.CommandExecutor;
import org.apache.geronimo.twiddle.command.CommandContext;
import org.apache.geronimo.twiddle.command.CommandException;
import org.apache.geronimo.twiddle.command.CommandNotFoundException;

/**
 * Twiddle is a command processor.
 *
 * @version <code>$Id: Twiddle.java,v 1.1 2003/08/13 08:32:09 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Twiddle
{
    protected CommandContainer container;
    protected CommandExecutor executor;
    
    public Twiddle()
    {
        container = new CommandContainer();
        executor = new CommandExecutor(container);
    }
    
    public void configure(final Configuration config) throws CommandException
    {
        if (config == null) {
            throw new NullArgumentException("config");
        }
        
        // Process properties
        if (config.getProperties() != null) {
            Property[] props = config.getProperties().getProperty();
            for (int i=0; i<props.length; i++) {
                String name = props[i].getName().trim();
                String value = props[i].getContent();
                //
                // TODO: Handle property value evaluation
                //
                System.setProperty(name, value);
            }
        }
        
        // Process includes
        try {
            if (config.getIncludes() != null) {
                String[] includes = config.getIncludes().getInclude();
                ConfigurationReader reader = new ConfigurationReader();
                for (int i=0; i<includes.length; i++) {
                    URL configURL = new URL(includes[i]);
                    //
                    // TODO: Need to properly resolve this URL
                    //
                    Configuration iconfig = reader.read(configURL);
                    this.configure(iconfig);
                }
            }
        }
        catch (Exception e) {
            throw new CommandException("Failed to process includes", e);
        }
        
        // Process commands
        CommandConfig[] commands = config.getCommands().getCommandConfig();
        if (commands != null) {
            for (int i=0; i<commands.length; i++) {
                CommandInfo info = new CommandInfo(commands[i]);
                container.addCommandInfo(info);
            }
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                          Command Execution                          //
    /////////////////////////////////////////////////////////////////////////
    
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
        out.println("  |_|   \\_/\\_/ |_|\\__,_|\\__,_|_|\\___|");
        out.flush();
        
        return writer.toString();
    }
}
