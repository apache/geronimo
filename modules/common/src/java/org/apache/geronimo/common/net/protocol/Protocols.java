/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.common.net.protocol;

import java.net.URL;
import java.net.URLStreamHandler;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang.StringUtils;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Classes;
import org.apache.geronimo.common.ThrowableHandler;

/**
 * Protocol utilties.
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/25 09:57:03 $
 */
public class Protocols
{
    private static final Log log = LogFactory.getLog(Protocols.class);
    
    
    /////////////////////////////////////////////////////////////////////////
    //                     Protocol Handler Packages                       //
    /////////////////////////////////////////////////////////////////////////
    
    public static final String HANDLER_PACKAGES = "java.protocol.handler.pkgs";
    public static final String SYSTEM_HANDLER_PACKAGES = System.getProperty(HANDLER_PACKAGES);
    
    static List parseHandlerPackages(final String pkgs)
    {
        assert pkgs != null;
        
        List list = new LinkedList();
        
        if (pkgs != null)  {
            StringTokenizer stok = new StringTokenizer(pkgs, "|");
            while (stok.hasMoreTokens()) {
                list.add(stok.nextToken().trim());
            }
        }
        
        return list;
    }
    
    public static List getSystemHandlerPackages()
    {
        return parseHandlerPackages(SYSTEM_HANDLER_PACKAGES);
    }
    
    public static List getHandlerPackages()
    {
        String handlersProperty = System.getProperty(HANDLER_PACKAGES);
        if (handlersProperty != null) {
            return parseHandlerPackages(handlersProperty);
        } else {
            return new LinkedList();
        }
    }
    
    public static void setHandlerPackages(final List packages)
    {
        if (packages == null || packages.size() == 0) {
            System.getProperties().remove(HANDLER_PACKAGES);
        }
        else {
            String pkgs = StringUtils.join(packages.toArray(), "|");
            System.setProperty(HANDLER_PACKAGES, pkgs);
        }
    }
    
    public static void appendHandlerPackage(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        List list = getHandlerPackages();
        list.add(name);
        setHandlerPackages(list);
    }
    
    public static void prependHandlerPackage(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        List list = getHandlerPackages();
        list.add(0, name);
        setHandlerPackages(list);
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                        URL Stream Handlers                          //
    /////////////////////////////////////////////////////////////////////////
    
    public static Class getURLStreamHandlerType(final String protocol)
    {
        if (protocol == null) {
            throw new NullArgumentException("protocol");
        }
        
        Iterator iter = getHandlerPackages().iterator();
        while (iter.hasNext()) {
            String pkg = (String)iter.next();
            String classname = pkg + "." + protocol + ".Handler";
            
            try {
                return Classes.loadClass(classname);
            }
            catch (ClassNotFoundException e) {
                ThrowableHandler.add(e);
            }
        }
        
        return null;
    }
    
    public static URLStreamHandler createURLStreamHandler(final String protocol)
    {
        // protocol checked by getURLStreamHandlerType
        
        Class type = getURLStreamHandlerType(protocol);
        
        if (type != null) {
            try {
                Object obj = type.newInstance();
                if (obj instanceof URLStreamHandler) {
                    return (URLStreamHandler)obj;
                }
                else {
                    // Handler is not instance of URLStreamHandler; ignoring
                }
            }
            catch (Exception e) {
                ThrowableHandler.add(e);
            }
        }
        
        return null;
    }
    
    /**
     * Preload the specific protocol handlers, so that URL knows about
     * them even if the handler factory is changed.
     */
    public static void preloadURLStreamHandlers(final String[] protocols)
    {
        if (protocols == null) {
            throw new NullArgumentException("protocols");
        }
        
        for (int i=0; i<protocols.length; i++) {
            if (protocols[i] == null) {
                throw new NullArgumentException("protocols", i);
            }
            
            try {
                URL url = new URL(protocols[i], "", -1, "");
            }
            catch (Exception ignore) {}
        }
    }
    
    public static void registerURLStreamHandler(final String protocol,
                                                final URLStreamHandler handler)
    {
        if (protocol == null) {
            throw new NullArgumentException("protocol");
        }
        if (handler == null) {
            throw new NullArgumentException("handler");
        }
        
        // This way is "naughty" but works great
        Map handlers = (Map)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() { 
                    try {
                        Field field = URL.class.getDeclaredField("handlers");
                        field.setAccessible(true);
                        
                        return field.get(null);
                    }
                    catch (Exception e) {
                        log.warn("Failed to access URL 'handlers' field", e);
                    }
                    return null;
                }
            }
        );
        
        if (handlers != null) {
            handlers.put(protocol, handler);
        }
    }
}
