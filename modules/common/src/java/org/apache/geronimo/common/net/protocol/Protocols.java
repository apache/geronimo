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

import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Strings;

/**
 * Protocol utilties.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 11:57:14 $
 */
public class Protocols
{
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
}
