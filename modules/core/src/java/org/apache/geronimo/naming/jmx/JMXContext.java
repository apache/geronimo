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

package org.apache.geronimo.naming.jmx;

import java.util.Hashtable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameParser;
import javax.naming.NameNotFoundException;
import javax.naming.OperationNotSupportedException;
import javax.naming.CompositeName;
import javax.naming.spi.NamingManager;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MBeanServerFactory;
import javax.management.AttributeNotFoundException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:30 $
 *
 * */
public class JMXContext  implements Context  {

    private final Hashtable env;

    public JMXContext(Hashtable env) {
        this.env = new Hashtable(env);
    }

    public Object lookup(Name name) throws NamingException {
       return lookup(name.toString());
    }

    public Object lookup(String name) throws NamingException {
        URI uri = null;
        try {
            uri = new URI(name);
        } catch (URISyntaxException e) {
            throw getNamingException("Can not parse URI name supplied", e);
        }

        if (!uri.getScheme().equals("jmx")) {
            throw new NamingException("We only resolve jmx: names. " + name);
        }
        String mbeanServerId = uri.getAuthority();
        String objectName = uri.getPath().substring(1);
        String operation = uri.getQuery();

        try {
            MBeanServer server = (MBeanServer)MBeanServerFactory.findMBeanServer(mbeanServerId).get(0);
            if (!operation.startsWith("get")) {
                return server.invoke(ObjectName.getInstance(objectName), operation, null, null);
            } else {
                return server.getAttribute(ObjectName.getInstance(objectName), operation.substring(3));
            }
        } catch (MalformedObjectNameException e) {
            throw getNamingException("Bad object name part", e);
        } catch (InstanceNotFoundException e) {
            throw getNamingException("No such mbean", e);
        } catch (MBeanException e) {
            throw getNamingException("MBean problem", e);
        } catch (ReflectionException e) {
            throw getNamingException("MBean reflection problem", e);
        } catch (IndexOutOfBoundsException e) {
            throw getNamingException("MBeanServer not found", e);
        } catch (AttributeNotFoundException e) {
            throw getNamingException("Attribute not found", e);
        }
    }

    public static String encode(String mbeanServerId, String objectName, String operation) throws NamingException {
        try {
            return new URI("jmx", mbeanServerId, '/' + objectName, operation, null).toString();
        } catch (URISyntaxException e) {
            throw getNamingException("Invalid syntax in generated URI", e);
        }
    }

    private static NamingException getNamingException(String message, Throwable t) {
        NamingException ne = new NamingException(message);
        ne.setRootCause(t);
        return ne;
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }

    public String composeName(String name, String prefix)
            throws NamingException {
        CompositeName result = new CompositeName(prefix);
        result.addAll(new CompositeName(name));
        return result.toString();
    }

    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        return env.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName)
            throws NamingException {
        return env.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) env.clone();
    }

    public void close() throws NamingException {
    }

    //unimplemented methods
    public NamingEnumeration list(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration list(String name) throws NamingException {
        return null;
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        return null;
    }


    //unsupported methods

    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

}
