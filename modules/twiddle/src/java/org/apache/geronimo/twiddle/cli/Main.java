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

package org.apache.geronimo.twiddle.cli;

import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import org.apache.geronimo.twiddle.Twiddle;

import org.apache.geronimo.twiddle.config.Configuration;
import org.apache.geronimo.twiddle.config.ConfigurationReader;

import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * Command-line interface to <code>Twiddle</code>.
 *
 * @version <code>$Id: Main.java,v 1.2 2003/08/13 15:18:47 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Main
{
    /** Platform dependent line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private String filename = "etc/twiddle.conf";
    
    public String[] processCommandLine(final String[] args) throws Exception
    {
        // create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
                                       
        options.addOption(OptionBuilder.withLongOpt("define")
                                       .withDescription("Define a system property")
                                       .hasArg()
                                       .create('D'));
                                       
        options.addOption(OptionBuilder.withLongOpt("file")
                                       .withDescription("Read a specific configuration file")
                                       .hasArg()
                                       .create('f'));
        
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        
        // parse the command line arguments
        CommandLine line = parser.parse(options, args, true);
        
        // Display command-line help and exit
        if (line.hasOption('h')) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
            HelpFormatter formatter = new HelpFormatter(out);
            
            formatter.print(
                Twiddle.getBanner(),
                "twiddle [options] (<command> [options] [arguments])*",
                options
            );
            
            System.exit(0);
        }
        
        // Set system properties
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');
            
            for (int i=0; i<values.length; i++) {
                String name, value;
                int j = values[i].indexOf("=");
                
                if (j == -1) {
                    name = values[i];
                    value = "true";
                }
                else {
                    name = values[i].substring(0, j);
                    value = values[i].substring(j + 1, values[i].length());
                }
                
                System.setProperty(name.trim(), value);
            }
        }
        
        if (line.hasOption('f')) {
            filename = line.getOptionValue('f');
        }
        
        return line.getArgs();
    }
    
    public void boot(String[] args) throws Exception
    {
        // Process command-line options
        args = processCommandLine(args);
        
        // Determine what our home directory is
        String temp = System.getProperty("twiddle.home");
        if (temp == null) {
            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            path = URLDecoder.decode(path);
            temp = new File(path).getParentFile().getParentFile().getCanonicalPath();
        }
        File homeDir = new File(temp);
        URL homeURL = homeDir.toURL();
        URL configURL = new URL(homeURL, filename);
        
        System.out.println("home directory: " + homeDir);
        System.out.println("home url: " + homeURL);
        System.out.println("configuration url: " + configURL);
        
        ConfigurationReader reader = new ConfigurationReader();
        Configuration config = reader.read(configURL);
        
        Twiddle twiddle = new Twiddle();
        twiddle.configure(config);
        
        if (args.length != 0) {
            int result = twiddle.execute(args);
            System.exit(result);
        }
        else {
            //
            // TODO: Start the interactive console
            //
            // twiddle.run();
            //
        }
    }
    
    public static void main(final String[] args) throws Exception
    {
        Main main = new Main();
        main.boot(args);
    }
}
