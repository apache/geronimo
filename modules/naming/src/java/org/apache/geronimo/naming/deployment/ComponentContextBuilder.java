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
package org.apache.geronimo.naming.deployment;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.xbeans.geronimo.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.GerMessageDestinationRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbLinkType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationLinkType;
import org.apache.geronimo.naming.proxy.ProxyFactory;
import org.apache.geronimo.naming.java.ReadOnlyContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/15 16:33:44 $
 */
public class ComponentContextBuilder {

    private static final String ENV = "env/";

    private final ProxyFactory proxyFactory;
    private final ClassLoader cl;

    public ComponentContextBuilder(ProxyFactory proxyFactory, ClassLoader cl) {
        this.proxyFactory = proxyFactory;
        this.cl = cl;
    }

    /**
     * Build a component context from definitions contained in POJOs read from
     * a deployment descriptor.
     * @return a Context that can be bound to java:comp
     */
    public ReadOnlyContext buildContext(EjbRefType[] ejbRefs, GerEjbRefType[] gerEjbRefs,
                                        EjbLocalRefType[] ejbLocalRefs, GerEjbLocalRefType[] gerEjbLocalRefs,
                                        EnvEntryType[] envEntries,
                                        MessageDestinationRefType[] messageDestinationRefs, GerMessageDestinationRefType[] gerMessageDestinationRefs ,
                                        ResourceEnvRefType[] resourceEnvRefs, GerResourceEnvRefType[] gerResourceEnvRefs,
                                        ResourceRefType[] resourceRefs, GerResourceRefType[] gerResourceRefs,
                                        UserTransaction userTransaction) throws DeploymentException {
        ReadOnlyContext readOnlyContext = new ReadOnlyContext();
        buildEnvEntries(readOnlyContext, envEntries);
        buildEJBRefs(readOnlyContext, ejbRefs, gerEjbRefs);
        buildEJBLocalRefs(readOnlyContext, ejbLocalRefs, gerEjbLocalRefs);
        buildMessageDestinationRefs(readOnlyContext, messageDestinationRefs, gerMessageDestinationRefs);
        buildResourceEnvRefs(readOnlyContext, resourceEnvRefs, gerResourceEnvRefs);
        buildResourceRefs(readOnlyContext, resourceRefs, gerResourceRefs);

        if (userTransaction != null) {
            try {
                readOnlyContext.externalBind("UserTransaction", userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("could not bind UserTransaction", e);
            }
        }
        readOnlyContext.freeze();
        return readOnlyContext;
    }

    private void buildEnvEntries(ReadOnlyContext readOnlyContext, EnvEntryType[] envEntries) throws DeploymentException {
        if (envEntries == null) {
            return;
        }
        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryType entry = envEntries[i];
            String name = entry.getEnvEntryName().getStringValue();
            String type = entry.getEnvEntryType().getStringValue();
            String value = entry.getEnvEntryValue().getStringValue();
            Object mapEntry;
            try {
                if (value == null) {
                    mapEntry = null;
                } else if ("java.lang.String".equals(type)) {
                    mapEntry = value;
                } else if ("java.lang.Character".equals(type)) {
                    mapEntry = new Character(value.charAt(0));
                } else if ("java.lang.Boolean".equals(type)) {
                    mapEntry = Boolean.valueOf(value);
                } else if ("java.lang.Byte".equals(type)) {
                    mapEntry = Byte.valueOf(value);
                } else if ("java.lang.Short".equals(type)) {
                    mapEntry = Short.valueOf(value);
                } else if ("java.lang.Integer".equals(type)) {
                    mapEntry = Integer.valueOf(value);
                } else if ("java.lang.Long".equals(type)) {
                    mapEntry = Long.valueOf(value);
                } else if ("java.lang.Float".equals(type)) {
                    mapEntry = Float.valueOf(value);
                } else if ("java.lang.Double".equals(type)) {
                    mapEntry = Double.valueOf(value);
                } else {
                    throw new AssertionError("Invalid class for env-entry " + name + ", " + type);
                }
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid numeric value for env-entry " + name + ", value=" + value);
            }
            try {
                readOnlyContext.externalBind(ENV + name, mapEntry);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildEJBRefs(ReadOnlyContext readOnlyContext, EjbRefType[] ejbRefs, GerEjbRefType[] gerEjbRefs) throws DeploymentException {
        if (ejbRefs == null) {
            return;
        }
        assert ejbRefs.length == gerEjbRefs.length;
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];
            String name = ejbRef.getEjbRefName().getStringValue();
            assert name.equals(gerEjbRefs[i].getEjbRefName().getStringValue());
            Object proxy = null;
            try {
                proxy = proxyFactory.getProxy(loadClass(ejbRef.getHome().getStringValue()), loadClass(ejbRef.getRemote().getStringValue()), getLink(gerEjbRefs[i].getUri(), ejbRef.getEjbLink()));
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct proxy for " + ejbRef + ", " + e.getMessage());
            }
            try {
                readOnlyContext.externalBind(ENV + name, proxy);
            } catch (NamingException e) {
                throw new DeploymentException("could not bind", e);
            }
        }
    }

    private void buildEJBLocalRefs(ReadOnlyContext readOnlyContext, EjbLocalRefType[] ejbLocalRefs, GerEjbLocalRefType[] gerEjbLocalRefs) throws DeploymentException {
        if (ejbLocalRefs == null) {
            return;
        }
        assert ejbLocalRefs.length == gerEjbLocalRefs.length;
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];
            String name = ejbLocalRef.getEjbRefName().getStringValue();
            assert name.equals(gerEjbLocalRefs[i].getEjbRefName().getStringValue());
            Object proxy = null;
            try {
                proxy = proxyFactory.getProxy(loadClass(ejbLocalRef.getLocalHome().getStringValue()), loadClass(ejbLocalRef.getLocal().getStringValue()), getLink(gerEjbLocalRefs[i].getUri(), ejbLocalRef.getEjbLink()));
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct reference to " + ejbLocalRef + ", " + e.getMessage());
            }
            try {
                readOnlyContext.externalBind(ENV + name, proxy);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildMessageDestinationRefs(ReadOnlyContext readOnlyContext, MessageDestinationRefType[] messageDestinationRefs, GerMessageDestinationRefType[] gerMessageDestinationRefs) throws DeploymentException {
        if (messageDestinationRefs == null) {
            return;
        }
        assert messageDestinationRefs.length == gerMessageDestinationRefs.length;
        for (int i = 0; i < messageDestinationRefs.length; i++) {
            MessageDestinationRefType messageDestination = messageDestinationRefs[i];
            String name = messageDestination.getMessageDestinationRefName().getStringValue();
            assert name.equals(gerMessageDestinationRefs[i].getMessageDestinationRefName().getStringValue());
            Object proxy = null;
            try {
                proxy = proxyFactory.getProxy(loadClass(messageDestination.getMessageDestinationType().getStringValue()), getLink(gerMessageDestinationRefs[i].getUri(), messageDestination.getMessageDestinationLink()));
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct reference to " + messageDestination + ", " + e.getMessage());
            }
            try {
                readOnlyContext.externalBind(ENV + name, proxy);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildResourceEnvRefs(ReadOnlyContext readOnlyContext, ResourceEnvRefType[] resourceEnvRefs, GerResourceEnvRefType[] gerResourceEnvRefs) throws DeploymentException {
        if (resourceEnvRefs == null) {
            return;
        }
        assert resourceEnvRefs.length == gerResourceEnvRefs.length;
        for (int i = 0; i < resourceEnvRefs.length; i++) {
            ResourceEnvRefType resEnvRef = resourceEnvRefs[i];
            String name = resEnvRef.getResourceEnvRefName().getStringValue();
            assert name.equals(gerResourceEnvRefs[i].getResourceEnvRefName().getStringValue());
            Object proxy = null;
            try {
                proxy = proxyFactory.getProxy(loadClass(resEnvRef.getResourceEnvRefType().getStringValue()), getLink(gerResourceEnvRefs[i].getUri()));
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct reference to " + resEnvRef + ", " + e.getMessage());
            }
            try {
                readOnlyContext.externalBind(ENV + name, proxy);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildResourceRefs(ReadOnlyContext readOnlyContext, ResourceRefType[] resRefs, GerResourceRefType[] gerResRefs) throws DeploymentException {
        if (resRefs == null) {
            return;
        }
        assert resRefs.length == gerResRefs.length;
        for (int i=0; i < resRefs.length; i++) {
            ResourceRefType resRef = resRefs[i];
            String name = resRef.getResRefName().getStringValue();
            assert name.equals(gerResRefs[i].getResRefName().getStringValue());
            String type = resRef.getResType().getStringValue();
            Object proxy;
            if ("java.net.URL".equals(type)) {
                //for some reason the spec regards URL as a connection factory...
                try {
                    proxy = new URL(gerResRefs[i].getUri());
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Invalid URL for resource-proxy "+name, e);
                }
            } else {
                try {
                    proxy = proxyFactory.getProxy(loadClass(resRef.getResType().getStringValue()), getLink(gerResRefs[i].getUri()));
                } catch (NamingException e) {
                    throw new DeploymentException("Could not construct reference to " + resRef);
                }
            }
            try {
                readOnlyContext.externalBind(ENV + name, proxy);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    //TODO figure out how to use the link properly.
    private Object getLink(String uri, EjbLinkType link) {
        if (link != null) {
            return link.getStringValue();
        }
        return uri;
    }

    private Object getLink(String uri, MessageDestinationLinkType link) {
        if (link != null) {
            return link.getStringValue();
        }
        return uri;
    }

    private Object getLink(String uri) {
        return uri;
    }

    private Class loadClass(String stringValue) throws DeploymentException {
        try {
            return cl.loadClass(stringValue);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load interface class: " + stringValue, e);
        }
    }
}
