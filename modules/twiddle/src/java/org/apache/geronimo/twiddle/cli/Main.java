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

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.InputStream;

import java.net.URL;

import org.codehaus.classworlds.ClassWorld;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.Twiddle;
import org.apache.geronimo.twiddle.config.Configuration;
import org.apache.geronimo.twiddle.config.ConfigurationReader;
import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * Command-line interface to <code>Twiddle</code>.
 *
 * @version $Revision: 1.14 $ $Date: 2003/12/07 03:39:34 $
 */
public class Main
{
    private String filename = "etc/twiddle.conf";
    private ClassWorld world;
    
    public Main(final ClassWorld world)
    {
        if (world == null) {
            throw new NullArgumentException("world");
        }
        
        this.world = world;
    }
    
    private String[] processCommandLine(final String[] args) throws Exception
    {
        assert args != null;
        
        // create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
                                       
        options.addOption(OptionBuilder.withLongOpt("debug")
                                       .withDescription("Enable debug output")
                                       .create('d'));
                                       
        options.addOption(OptionBuilder.withLongOpt("trace")
                                       .withDescription("Enable trace output")
                                       .create('T'));
                                       
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
        
        //
        // TODO handle --trace and --debug flags
        //
        
        return line.getArgs();
    }
    
    public void boot(String[] args) throws Exception
    {
        if (args == null) {
            throw new NullArgumentException("args");
        }
        
        // Read property defaults
        InputStream input = getClass().getResourceAsStream("/twiddle.properties");
        if (input != null) {
            System.getProperties().load(input);
        }
        
        // Process command-line options
        args = processCommandLine(args);
        
        URL homeURL = Twiddle.getHomeURL();
        URL configURL = new URL(homeURL, filename);
        
        ConfigurationReader reader = new ConfigurationReader();
        Configuration config = reader.read(configURL);
        
        Twiddle twiddle = new Twiddle(world);
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
    
    public static void main(final String[] args, final ClassWorld world)
    {
        // args & world are checked for null by Main
        
        try {
            Main main = new Main(world);
            main.boot(args);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public static void main(final String[] args) throws Exception
    {
        // args are checked for null by Main
        
        ClassWorld world = new ClassWorld();
        main(args, world);
    }
}
