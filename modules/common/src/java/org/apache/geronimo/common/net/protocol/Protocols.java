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

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Strings;
import org.apache.geronimo.common.Classes;
import org.apache.geronimo.common.ThrowableHandler;

/**
 * Protocol utilties.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/03 09:25:32 $
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
        return parseHandlerPackages(System.getProperty(HANDLER_PACKAGES));
    }
    
    public static void setHandlerPackages(final List packages)
    {
        if (packages == null || packages.size() == 0) {
            System.getProperties().remove(HANDLER_PACKAGES);
        }
        else {
            String pkgs = Strings.join(packages.toArray(), "|");
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
