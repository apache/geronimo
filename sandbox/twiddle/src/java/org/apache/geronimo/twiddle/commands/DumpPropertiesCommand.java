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

package org.apache.geronimo.twiddle.commands;

import java.io.PrintWriter;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandException;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.command.AbstractCommand;
import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * Dumps system properties.
 *
 * @version <code>$Revision: 1.1 $ $Date: 2004/01/23 02:57:16 $</code>
 */
public class DumpPropertiesCommand
    extends AbstractCommand
{
    public int execute(String[] args) throws Exception
    {
        if (args == null) {
            throw new NullArgumentException("args");
        }
        
        // Get our output writer
        PrintWriter out = getWriter();
        
        // Create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
        
        // Create the command line parser
        CommandLineParser parser = new PosixParser();
        
        // Carse the command line arguments
        CommandLine line = parser.parse(options, args);
        
        // Display help
        if (line.hasOption('h')) {
            CommandInfo info = getCommandInfo();
            if (info.hasDescription()) {
                out.println(info.getDescription());
                out.println();
            }
            
            HelpFormatter formatter = new HelpFormatter(out);
            formatter.print(info.getName() + " [options] [prefix]", options);
            
            return Command.SUCCESS;
        }
        
        String[] unused = line.getArgs();
        String prefix = null;
        if (unused != null && unused.length != 0) {
            if (unused.length == 1) {
                prefix = unused[0];
            }
            else {
                throw new CommandException("Too many arguments");
            }
        }
        log.debug("Using prefix: " + prefix);
        
        java.util.Iterator iter = System.getProperties().keySet().iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            if (prefix == null || name.startsWith(prefix)) {
                out.print(name + "=");
                out.println(System.getProperty(name));
            }
        }
        
        // Will never reach here
        assert false;
        
        return Command.FAILURE;
    }
}
