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

package org.apache.geronimo.twiddle.config;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.File;
import java.io.FilenameFilter;

import java.util.List;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Strings;
import org.apache.geronimo.common.StringValueParser;

import org.apache.geronimo.twiddle.Twiddle;

import org.apache.geronimo.twiddle.command.CommandContainer;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.command.CommandException;

/**
 * Handles the details of Twiddle configuration.
 *
 * @version <code>$Revision: 1.8 $ $Date: 2003/08/24 20:09:25 $</code>
 */
public class Configurator
{
    private static final Log log = LogFactory.getLog(Configurator.class);
    
    protected StringValueParser valueParser;
    protected Twiddle twiddle;
    
    public Configurator(final Twiddle twiddle)
    {
        if (twiddle == null) {
            throw new NullArgumentException("twiddle");
        }
        
        this.twiddle = twiddle;
        this.valueParser = new StringValueParser();
    }
    
    public Twiddle getTwiddle()
    {
        return twiddle;
    }
    
    public void configure(final Configuration config) throws CommandException
    {
        if (config == null) {
            throw new NullArgumentException("config");
        }
        
        configureProperties(config.getPropertiesConfig());
        configureIncludes(config.getIncludesConfig());
        configureCommands(config.getCommandsConfig());
    }
    
    protected void configureProperties(final PropertiesConfig config)
    {
        if (config == null) return;
        
        PropertyConfig[] props = config.getPropertyConfig();
        for (int i=0; i<props.length; i++) {
            if (props[i] == null) {
                throw new NullArgumentException("PropertyConfig", i);
            }
            
            String name = props[i].getName().trim();
            String value = props[i].getContent();
            value = valueParser.parse(value);
            System.setProperty(name, value);
        }
    }
    
    protected void configureIncludes(final IncludesConfig config) throws CommandException
    {
        if (config == null) return;
        
        String[] includes = config.getInclude();
        if (includes != null && includes.length != 0) {
            ConfigurationReader reader = new ConfigurationReader();
            
            for (int i=0; i<includes.length; i++) {
                if (includes[i] == null) {
                    throw new NullArgumentException("Includes", i);
                }
                
                try {
                    String value = valueParser.parse(includes[i]);
                    URL[] urls = parseGlobURLs(value);
                    for (int j=0; j<urls.length; j++) {
                        Configuration iconfig = reader.read(urls[j]);
                        this.configure(iconfig);
                    }
                }
                catch (Exception e) {
                    throw new ConfigurationException("Failed to process include: " + includes[i], e);
                }
            }
        }
    }
    
    protected URL[] parseGlobURLs(final String globspec) throws MalformedURLException
    {
        assert globspec != null;
        
        boolean trace = log.isDebugEnabled();
        if (trace) {
            log.trace("Parsing glob URLs from spec: " + globspec);
        }
        
        URL baseURL = Strings.toURL(globspec);
        if (!baseURL.getProtocol().equals("file")) {
            // only can glob on file urls
            return new URL[] { baseURL };
        }
        
        File dir = new File(baseURL.getPath());
        String glob = dir.getName();
        dir = dir.getParentFile();
        
        if (trace) {
            log.trace("Base dir: " + dir);
            log.trace("Glob: " + glob);
        }
        
        int i = glob.indexOf("*");
        final String prefix = glob.substring(0, i);
        final String suffix = glob.substring(i + 1);
        if (trace) {
            log.trace("Prefix: " + prefix);
            log.trace("Suffix: " + suffix);
        }
        
        File[] matches = dir.listFiles(new FilenameFilter()
            {
                public boolean accept(final File dir, final String name)
                {
                    if (!name.startsWith(prefix) || !name.endsWith(suffix)) {
                        return false;
                    }
                    
                    return true;
                }
            });
        
        List list = new LinkedList();
        if (matches != null) {
            for (i=0; i < matches.length; ++i) {
                list.add(matches[i].toURL());
            }
        }
        
        if (trace) {
            log.trace("Parsed URLs: " + list);
        }
        
        return (URL[])list.toArray(new URL[list.size()]);
    }
    
    protected void configureCommands(final CommandsConfig config) throws CommandException
    {
        if (config == null) return;
        
        CommandConfig[] commands = config.getCommandConfig();
        if (commands != null) {
            CommandContainer container = twiddle.getCommandContainer();
            
            for (int i=0; i<commands.length; i++) {
                if (commands[i] == null) {
                    throw new NullArgumentException("CommandConfig", i);
                }
                
                CommandInfo info = new CommandInfo(commands[i]);
                container.addCommandInfo(info);
            }
        }
    }
}
