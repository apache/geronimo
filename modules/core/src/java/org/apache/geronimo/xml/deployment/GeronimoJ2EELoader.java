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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JndiContextParam;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;

/**
 * Loads common Geronimo DD tags
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/04 04:59:53 $
 */
public class GeronimoJ2EELoader {
    public static EnvEntry[] loadEnvEntries(Element parent) {
        NodeList nodes = parent.getElementsByTagName("env-entry");
        int length = nodes.getLength();
        EnvEntry[] result = new EnvEntry[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            EnvEntry envEntry = new EnvEntry();
            envEntry.setEnvEntryName(LoaderUtil.getChildContent(e, "env-entry-name"));
            envEntry.setEnvEntryValue(LoaderUtil.getChildContent(e, "env-entry-value"));
            result[i] = envEntry;
        }
        return result;
    }

    public static EjbRef[] loadEJBRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("ejb-ref");
        int length = nodes.getLength();
        EjbRef[] result = new EjbRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            EjbRef ejbRef = new EjbRef();
            ejbRef.setEjbRefName(LoaderUtil.getChildContent(e, "ejb-ref-name"));
            ejbRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
            ejbRef.setJndiContextParam(loadContextParams(LoaderUtil.getChild(e, "jndi-context-params")));
            result[i] = ejbRef;
        }
        return result;
    }

    public static EjbLocalRef[] loadEJBLocalRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("ejb-local-ref");
        int length = nodes.getLength();
        EjbLocalRef[] result = new EjbLocalRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            EjbLocalRef ejbLocalRef = new EjbLocalRef();
            ejbLocalRef.setEjbRefName(LoaderUtil.getChildContent(e, "ejb-ref-name"));
            ejbLocalRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
            ejbLocalRef.setJndiContextParam(loadContextParams(LoaderUtil.getChild(e, "jndi-context-params")));
            result[i] = ejbLocalRef;
        }
        return result;
    }

    private static JndiContextParam[] loadContextParams(Element parent) {
        if(parent == null) {
            return new JndiContextParam[0];
        }
        Element[] roots = LoaderUtil.getChildren(parent, "jndi-context-param");
        JndiContextParam[] params = new JndiContextParam[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            params[i] = new JndiContextParam();
            params[i].setParamName(LoaderUtil.getChildContent(root, "param-name"));
            params[i].setParamValue(LoaderUtil.getChildContent(root, "param-value"));
        }
        return params;
    }

    public static SecurityRoleRef[] loadSecurityRoleRefs(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "security-role-ref");
        SecurityRoleRef[] refs = new SecurityRoleRef[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            refs[i] = new SecurityRoleRef();
            refs[i].setRoleName(LoaderUtil.getChildContent(root, "role-name"));
            refs[i].setRoleLink(LoaderUtil.getChildContent(root, "role-link"));
        }
        return refs;
    }

    public static ServiceRef[] loadServiceRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("service-ref");
        int length = nodes.getLength();
        ServiceRef[] result = new ServiceRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            ServiceRef serviceRef = new ServiceRef();
            serviceRef.setServiceRefName(LoaderUtil.getChildContent(e, "service-ref-name"));
            result[i] = serviceRef;
        }
        return result;
    }

    public static ResourceRef[] loadResourceRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("resource-ref");
        int length = nodes.getLength();
        ResourceRef[] result = new ResourceRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            ResourceRef resRef = new ResourceRef();
            resRef.setResRefName(LoaderUtil.getChildContent(e, "res-ref-name"));
            resRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
            resRef.setJndiContextParam(loadContextParams(LoaderUtil.getChild(e, "jndi-context-params")));
            result[i] = resRef;
        }
        return result;
    }

    public static ResourceEnvRef[] loadResourceEnvRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("resource-env-ref");
        int length = nodes.getLength();
        ResourceEnvRef[] result = new ResourceEnvRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            ResourceEnvRef resEnvRef = new ResourceEnvRef();
            resEnvRef.setResourceEnvRefName(LoaderUtil.getChildContent(e, "resource-env-ref-name"));
            resEnvRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
            result[i] = resEnvRef;
        }
        return result;
    }

    public static MessageDestinationRef[] loadMessageDestinationRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("message-destination-ref");
        int length = nodes.getLength();
        MessageDestinationRef[] result = new MessageDestinationRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            MessageDestinationRef msgDestRef = new MessageDestinationRef();
            msgDestRef.setMessageDestinationRefName(LoaderUtil.getChildContent(e, "message-destination-ref-name"));
            msgDestRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
            msgDestRef.setJndiContextParam(loadContextParams(LoaderUtil.getChild(e, "jndi-context-params")));
            result[i] = msgDestRef;
        }
        return result;
    }
}
