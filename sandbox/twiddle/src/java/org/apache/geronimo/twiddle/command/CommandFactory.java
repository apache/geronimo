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

import java.io.IOException;

import java.beans.Beans;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.NoSuchRealmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.beanutils.BeanUtils;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.config.CommandConfig;
import org.apache.geronimo.twiddle.config.Attribute;

/**
 * A factory for creating <code>Command</code> instances from
 * a <code>CommandConfig</code>.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:37 $
 */
public class CommandFactory
{
    private static final Log log = LogFactory.getLog(CommandFactory.class);
    
    protected CommandConfig config;
    protected ClassWorld world;
    
    public CommandFactory(final CommandConfig config, final ClassWorld world)
    {
        if (config == null) {
            throw new NullArgumentException("config");
        }
        if (world == null) {
            throw new NullArgumentException("world");
        }
        
        this.config = config;
        this.world = world;
    }
    
    public CommandConfig getConfig()
    {
        return config;
    }
    
    public ClassWorld getClassWorld()
    {
        return world;
    }
    
    protected ClassRealm getClassRealm()
        throws NoSuchRealmException
    {
        String name = config.getRealm();
        if (name == null) {
            name = Command.DEFAULT_CLASS_REALM;
        }
        
        if (log.isTraceEnabled()) {
            log.trace("Loading command from class-realm: " + name);
        }
        
        return getClassWorld().getRealm(name);
    }
    
    protected Command doCreate()
        throws ClassNotFoundException, IllegalAccessException,
               InvocationTargetException, NoSuchRealmException,
               IOException
    {
        // Get a handle on the class realm to load the class from
        ClassRealm realm = getClassRealm();
        ClassLoader cl = realm.getClassLoader();
        
        // Load the command instance
        Command command = (Command)Beans.instantiate(cl, config.getCode());
        
        // Configure the command attributes
        configureAttributes(command, config.getAttribute());
        
        return command;
    }
    
    protected void configureAttributes(final Command command, final Attribute[] attrs)
        throws IllegalAccessException, InvocationTargetException
    {
        assert command != null;
        
        if (attrs == null) return;
        
        for (int i=0; i<attrs.length; i++) {
            String name = attrs[i].getName();
            Object value = attrs[i].getContent();
            BeanUtils.setProperty(command, name, value);
        }
    }
    
    public Command create() throws CommandException
    {
        try {
            return doCreate();
        }
        catch (Exception e) {
            throw new CommandException("Failed to create command: " + config.getName(), e);
        }
    }
}
