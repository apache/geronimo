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
 * @version $Revision: 1.1 $ $Date: 2004/01/23 02:57:16 $
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
