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

package org.apache.geronimo.command;

import java.io.PrintWriter;
import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.geronimo.console.cli.Deployer;
import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.twiddle.command.AbstractCommand;
import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * A <em>Twiddle</em> command to configure and/or deploy a J2EE module
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/04 05:24:21 $
 */
public class DeployCommand
    extends AbstractCommand
{

    /**
     * Manages help and command line arguments, and hands control over to the
     * {@link Deployer} to handle the user interaction and resulting work. 
     */
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
        args = line.getArgs();
        if (line.hasOption('h') || args.length > 1) {
            CommandInfo info = getCommandInfo();
            if (info.hasDescription()) {
                out.println(info.getDescription());
                out.println();
            }
            
            HelpFormatter formatter = new HelpFormatter(out);
            formatter.print(info.getName() + " [options] [EJB_JAR_File]", options);
            
            return Command.SUCCESS;
        }
        
//        String homeDir = Twiddle.getHomeDir().toString(); todo: do we need to use this when resolving the file?
        Deployer deployer;
        if(args.length == 1) {
            File file = new File(args[0]);
            if(!file.exists() || !file.canRead() || file.isDirectory()) {
                out.println("ERROR: Unable to read file "+file.getAbsolutePath());
                return Command.FAILURE;
            }
            deployer = new Deployer(file, out, getReader());
        } else {
            deployer = new Deployer(out, getReader());
        }
        deployer.run();

        return Command.SUCCESS;
    }
}
