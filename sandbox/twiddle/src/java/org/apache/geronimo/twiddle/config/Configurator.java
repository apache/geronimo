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

package org.apache.geronimo.twiddle.config;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.File;
import java.io.FilenameFilter;

import java.util.List;
import java.util.LinkedList;

import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.NoSuchRealmException;
import org.codehaus.classworlds.ClassWorldException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.StringValueParser;
import org.apache.geronimo.common.net.URLFactory;

import org.apache.geronimo.twiddle.Twiddle;

import org.apache.geronimo.twiddle.command.CommandContainer;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.command.CommandException;
import org.apache.geronimo.twiddle.command.Command;

import org.apache.geronimo.twiddle.config.classworlds.ClassWorldsConfig;
import org.apache.geronimo.twiddle.config.classworlds.ClassRealmConfig;
import org.apache.geronimo.twiddle.config.classworlds.ImportPackageConfig;

/**
 * Handles the details of Twiddle configuration.
 *
 * @version <code>$Rev$ $Date$</code>
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
        
        try {
            configureProperties(config.getPropertiesConfig());
            configureClassWorlds(config.getClassWorldsConfig());
            configureIncludes(config.getIncludesConfig());
            configureCommands(config.getCommandsConfig());
        }
        catch (Exception e) {
            throw new CommandException("Failed to configure", e);
        }
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
    
    protected void configureClassWorlds(final ClassWorldsConfig config) 
        throws ClassWorldException, MalformedURLException
    {
        if (config == null) return;
        
        ClassRealmConfig[] realms = config.getClassRealmConfig();
        for (int i=0; i<realms.length; i++) {
            if (realms[i] == null) {
                throw new NullArgumentException("ClassRealmConfig", i);
            }
            
            String name = realms[i].getName();
            if (name == null) {
                name = Command.DEFAULT_CLASS_REALM;
            }
            else {
                name = valueParser.parse(name);
            }
            
            configureClassWorldClassRealm(name, realms[i]);
        }
    }
    
    protected void configureClassWorldClassRealm(final String name, final ClassRealmConfig config)
        throws ClassWorldException, MalformedURLException
    {
        assert name != null;
        assert config != null;
        
        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Processing classworld realm: " + name);
        }
        
        // Get or create a new realm for this name
        ClassWorld world = getTwiddle().getClassWorld();
        ClassRealm realm = null;
        try {
            realm = world.getRealm(name);
        }
        catch (NoSuchRealmException e) {
            realm = world.newRealm(name);
        }
        
        String[] constituents = config.getLoadURLConfig();
        if (constituents != null) {
            log.trace("Processing constituents...");
            
            for (int i=0; i<constituents.length; i++) {
                assert constituents[i] != null;
                
                String urlspec = valueParser.parse(constituents[i], true);
                URL[] urls = parseGlobURLs(urlspec);
                for (int j=0; j<urls.length; j++) {
                    if (trace) {
                        log.trace("Adding constituent: " + urls[j]);
                    }
                    realm.addConstituent(urls[j]);
                }
            }
        }
        
        ImportPackageConfig[] imports = config.getImportPackageConfig();
        if (imports != null) {
            log.trace("Processing imports...");
            
            for (int i=0; i<imports.length; i++) {
                assert imports[i] != null;
                assert imports[i].getRealm() != null;
                assert imports[i].getContent() != null;
                
                String from = valueParser.parse(imports[i].getRealm(), true);
                String pkg = valueParser.parse(imports[i].getContent(), true);
                if (trace) {
                    log.trace("Importing " + pkg + " from realm " + from);
                }
                realm.importFrom(from, pkg);
            }
        }
    }
    
    protected void configureIncludes(final IncludesConfig config) throws CommandException
    {
        if (config == null) return;
        
        String[] includes = config.getInclude();
        if (includes == null || includes.length == 0) return;
        
        boolean trace = log.isTraceEnabled();
        
        ConfigurationReader reader = new ConfigurationReader();
        
        for (int i=0; i<includes.length; i++) {
            if (includes[i] == null) {
                throw new NullArgumentException("Includes", i);
            }
            if (trace) {
                log.trace("Processing include: " + includes[i]);
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
    
    /**
     * Parse an array of URLs from a glob.
     *
     * <p>Only supports '*' glob token.
     *
     * @param globspec  The glob to parse.
     * @return          An array of URLs
     *
     * @throws MalformedURLException
     */
    protected URL[] parseGlobURLs(final String globspec) throws MalformedURLException
    {
        assert globspec != null;
        
        boolean trace = log.isDebugEnabled();
        if (trace) {
            log.trace("Parsing glob URLs from spec: " + globspec);
        }
        
        URL baseURL = URLFactory.create(globspec);
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
        if (i < 0) {
            // no glob
            return new URL[] { baseURL };
        }
        
        final String prefix = glob.substring(0, i);
        final String suffix = glob.substring(i + 1);
        if (trace) {
            log.trace("Prefix: " + prefix);
            log.trace("Suffix: " + suffix);
        }
        
        File[] matches = dir.listFiles(new FilenameFilter() {
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
        if (commands == null) return;
        
        CommandContainer container = twiddle.getCommandContainer();
        ClassWorld world = getTwiddle().getClassWorld();
        
        for (int i=0; i<commands.length; i++) {
            if (commands[i] == null) {
                throw new NullArgumentException("CommandConfig", i);
            }
            
            CommandInfo info = new CommandInfo(commands[i], world);
            container.addCommandInfo(info);
        }
    }
}
