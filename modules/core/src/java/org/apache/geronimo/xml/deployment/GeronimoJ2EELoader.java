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
package org.apache.geronimo.xml.deployment;


import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestination;
import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ClassSpace;
import org.w3c.dom.Element;

/**
 * Loads common Geronimo DD tags
 *
 * @version $Revision: 1.6 $ $Date: 2003/11/17 02:03:16 $
 */
public final class GeronimoJ2EELoader {
    public static EjbRef[] loadEJBRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "ejb-ref");
        EjbRef[] result = new EjbRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadEJBRef(nodes[i], new EjbRef());
        }
        return result;
    }

    public static EjbRef loadEJBRef(Element e, EjbRef ejbRef) {
        J2EELoader.loadEJBRef(e, ejbRef);
        ejbRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return ejbRef;
    }

    public static EjbLocalRef[] loadEJBLocalRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "ejb-local-ref");
        EjbLocalRef[] result = new EjbLocalRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadEJBLocalRef(nodes[i], new EjbLocalRef());
        }
        return result;
    }

    public static EjbLocalRef loadEJBLocalRef(Element e, EjbLocalRef ejbRef) {
        J2EELoader.loadEJBLocalRef(e, ejbRef);
        ejbRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return ejbRef;
    }

    public static ResourceRef[] loadResourceRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "resource-ref");
        ResourceRef[] result = new ResourceRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadResourceRef(nodes[i], new ResourceRef());
        }
        return result;
    }

    public static ResourceRef loadResourceRef(Element e, ResourceRef resourceRef) {
        J2EELoader.loadResourceRef(e, resourceRef);
        resourceRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return resourceRef;
    }

    public static ResourceEnvRef[] loadResourceEnvRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "resource-env-ref");
        ResourceEnvRef[] result = new ResourceEnvRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadResourceEnvRef(nodes[i], new ResourceEnvRef());
        }
        return result;
    }

    public static ResourceEnvRef loadResourceEnvRef(Element e, ResourceEnvRef resourceEnvRef) {
        J2EELoader.loadResourceEnvRef(e, resourceEnvRef);
        resourceEnvRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return resourceEnvRef;
    }

    public static ServiceRef[] loadServiceRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "service-ref");
        ServiceRef[] result = new ServiceRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadServiceRef(nodes[i], new ServiceRef());
        }
        return result;
    }

    public static ServiceRef loadServiceRef(Element e, ServiceRef serviceRef) {
        J2EELoader.loadServiceRef(e, serviceRef);
        return serviceRef;
    }

    public static SecurityRoleRef[] loadSecurityRoleRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "security-role-ref");
        SecurityRoleRef[] result = new SecurityRoleRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadSecurityRoleRef(nodes[i], new SecurityRoleRef());
        }
        return result;
    }

    public static SecurityRoleRef loadSecurityRoleRef(Element e, SecurityRoleRef ref) {
        J2EELoader.loadSecurityRoleRef(e, ref);
        return ref;
    }

    public static MessageDestinationRef[] loadMessageDestinationRefs(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "message-destination-ref");
        MessageDestinationRef[] result = new MessageDestinationRef[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadMessageDestinationRef(nodes[i], new MessageDestinationRef());
        }
        return result;
    }

    public static MessageDestinationRef loadMessageDestinationRef(Element e, MessageDestinationRef messageDestinationRef) {
        J2EELoader.loadMessageDestinationRef(e, messageDestinationRef);
        messageDestinationRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return messageDestinationRef;
    }

    public static MessageDestination[] loadMessageDestinations(Element e) {
        Element[] nodes = LoaderUtil.getChildren(e, "message-destination");
        MessageDestination[] result = new MessageDestination[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadMessageDestination(nodes[i], new MessageDestination());
        }
        return result;
    }

    public static MessageDestination loadMessageDestination(Element e, MessageDestination messageDestination) {
        J2EELoader.loadMessageDestination(e, messageDestination);
        messageDestination.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return messageDestination;
    }

    public static ClassSpace loadClassSpace(Element e) {
        Element cs = LoaderUtil.getChild(e, "class-space");
        ClassSpace classSpace = new ClassSpace();
        classSpace.setClassSpace(LoaderUtil.getAttribute(cs, "name"));
        classSpace.setParentClassSpace((LoaderUtil.getAttribute(cs, "parent")));
        return classSpace;
    }
}
