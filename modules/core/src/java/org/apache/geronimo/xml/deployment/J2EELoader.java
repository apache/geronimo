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

import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.EJBLocalRef;
import org.apache.geronimo.deployment.model.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.j2ee.PortComponentRef;
import org.apache.geronimo.deployment.model.j2ee.Handler;
import org.apache.geronimo.deployment.model.j2ee.ParamValue;
import org.apache.geronimo.deployment.model.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.j2ee.MessageDestination;
import org.apache.geronimo.deployment.model.j2ee.Describable;
import org.apache.geronimo.deployment.model.j2ee.Description;
import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.apache.geronimo.deployment.model.j2ee.Icon;
import org.apache.geronimo.deployment.model.j2ee.RunAs;
import org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Knows how to load common J2EE deployment descriptor elements from a DOM
 * into POJOs.
 *
 * @version $Revision: 1.5 $ $Date: 2003/09/17 01:47:14 $
 */
public final class J2EELoader {
    public static EnvEntry[] loadEnvEntries(Element parent) {
        NodeList nodes = parent.getElementsByTagName("env-entry");
        int length = nodes.getLength();
        EnvEntry[] result = new EnvEntry[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadEnvEntry(e, new EnvEntry());
        }
        return result;
    }

    public static EnvEntry loadEnvEntry(Element e, EnvEntry envEntry) {
        envEntry.setEnvEntryName(LoaderUtil.getChildContent(e, "env-entry-name"));
        envEntry.setEnvEntryType(LoaderUtil.getChildContent(e, "env-entry-type"));
        envEntry.setEnvEntryValue(LoaderUtil.getChildContent(e, "env-entry-value"));
        return envEntry;
    }

    public static EJBRef[] loadEJBRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("ejb-ref");
        int length = nodes.getLength();
        EJBRef[] result= new EJBRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadEJBRef(e, new EJBRef());
        }
        return result;
    }

    public static EJBRef loadEJBRef(Element e, EJBRef ejbRef) {
        ejbRef.setEJBRefName(LoaderUtil.getChildContent(e, "ejb-ref-name"));
        ejbRef.setEJBRefType(LoaderUtil.getChildContent(e, "ejb-ref-type"));
        ejbRef.setHome(LoaderUtil.getChildContent(e, "home"));
        ejbRef.setRemote(LoaderUtil.getChildContent(e, "remote"));
        ejbRef.setEJBLink(LoaderUtil.getChildContent(e, "ejb-link"));
        return ejbRef;
    }

    public static EJBLocalRef[] loadEJBLocalRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("ejb-local-ref");
        int length = nodes.getLength();
        EJBLocalRef[] result = new EJBLocalRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadEJBLocalRef(e, new EJBLocalRef());
        }
        return result;
    }

    public static EJBLocalRef loadEJBLocalRef(Element e, EJBLocalRef ejbLocalRef) {
        ejbLocalRef.setEJBRefName(LoaderUtil.getChildContent(e, "ejb-ref-name"));
        ejbLocalRef.setEJBRefType(LoaderUtil.getChildContent(e, "ejb-ref-type"));
        ejbLocalRef.setLocalHome(LoaderUtil.getChildContent(e, "local-home"));
        ejbLocalRef.setLocal(LoaderUtil.getChildContent(e, "local"));
        ejbLocalRef.setEJBLink(LoaderUtil.getChildContent(e, "ejb-link"));
        return ejbLocalRef;
    }

    public static SecurityRoleRef[] loadSecurityRoleRefs(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "security-role-ref");
        SecurityRoleRef[] refs = new SecurityRoleRef[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            refs[i] = new SecurityRoleRef();
            loadDescribable(root, refs[i]);
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
            result[i] = loadServiceRef(e, new ServiceRef());
        }
        return result;
    }

    public static ServiceRef loadServiceRef(Element e, ServiceRef serviceRef) {
        serviceRef.setServiceRefName(LoaderUtil.getChildContent(e, "service-ref-name"));
        serviceRef.setServiceInterface(LoaderUtil.getChildContent(e, "service-interface"));
        serviceRef.setWSDLFile(LoaderUtil.getChildContent(e, "wsdl-file"));
        serviceRef.setJAXRPCMappingFile(LoaderUtil.getChildContent(e, "jaxrpc-mapping-file"));
        serviceRef.setServiceQName(LoaderUtil.getChildContent(e, "service-qname"));
        serviceRef.setPortComponentRef(loadPortComponentRefs(e));
        serviceRef.setHandler(loadHandlers(e));
        return serviceRef;
    }

    private static PortComponentRef[] loadPortComponentRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("port-component-ref");
        int length = nodes.getLength();
        PortComponentRef[] result = new PortComponentRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            PortComponentRef ref = new PortComponentRef();
            ref.setServiceEndpointInterface(LoaderUtil.getChildContent(e, "service-endpoint-interface"));
            ref.setPortComponentLink(LoaderUtil.getChildContent(e, "port-component-link"));
            result[i] = ref;
        }
        return result;
    }

    private static Handler[] loadHandlers(Element parent) {
        NodeList nodes = parent.getElementsByTagName("handler");
        int length = nodes.getLength();
        Handler[] result = new Handler[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            Handler handler = new Handler();
            handler.setHandlerName(LoaderUtil.getChildContent(e, "handler-name"));
            handler.setHandlerClass(LoaderUtil.getChildContent(e, "handler-class"));
            handler.setInitParam(loadInitParams(e));
            handler.setSoapHeader(LoaderUtil.getChildrenContent(e, "soap-header"));
            handler.setSoapRole(LoaderUtil.getChildrenContent(e, "soap-role"));
            handler.setPortName(LoaderUtil.getChildrenContent(e, "port-name"));
            result[i] = handler;
        }
        return result;
    }

    public static ParamValue[] loadInitParams(Element parent) {
        NodeList nodes = parent.getElementsByTagName("init-param");
        int length = nodes.getLength();
        ParamValue[] result = new ParamValue[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            ParamValue handler = new ParamValue();
            handler.setParamName(LoaderUtil.getChildContent(e, "param-name"));
            handler.setParamValue(LoaderUtil.getChildContent(e, "param-value"));
            result[i] = handler;
        }
        return result;
    }

    public static ResourceRef[] loadResourceRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("resource-ref");
        int length = nodes.getLength();
        ResourceRef[] result = new ResourceRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadResourceRef(e, new ResourceRef());
        }
        return result;
    }

    public static ResourceRef loadResourceRef(Element e, ResourceRef resRef) {
        resRef.setResRefName(LoaderUtil.getChildContent(e, "res-ref-name"));
        resRef.setResType(LoaderUtil.getChildContent(e, "res-type"));
        resRef.setResAuth(LoaderUtil.getChildContent(e, "res-auth"));
        resRef.setResSharingScope(LoaderUtil.getChildContent(e, "res-sharing-scope"));
        return resRef;
    }

    public static ResourceEnvRef[] loadResourceEnvRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("resource-env-ref");
        int length = nodes.getLength();
        ResourceEnvRef[] result = new ResourceEnvRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadResourceEnvRef(e, new ResourceEnvRef());
        }
        return result;
    }

    public static ResourceEnvRef loadResourceEnvRef(Element e, ResourceEnvRef resEnvRef) {
        resEnvRef.setResourceEnvRefName(LoaderUtil.getChildContent(e, "resource-env-ref-name"));
        resEnvRef.setResourceEnvRefType(LoaderUtil.getChildContent(e, "resource-env-ref-type"));
        return resEnvRef;
    }

    public static MessageDestination[] loadMessageDestinations(Element parent) {
        Element[] nodes = LoaderUtil.getChildren(parent, "message-destination");
        MessageDestination[] result = new MessageDestination[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            result[i] = loadMessageDestination(nodes[i], new MessageDestination());
        }
        return result;
    }

    public static MessageDestination loadMessageDestination(Element e, MessageDestination msgDest) {
        loadDisplayable(e, msgDest);
        msgDest.setMessageDestinationName(LoaderUtil.getChildContent(e, "message-destination-name"));
        return msgDest;
    }

    public static MessageDestinationRef[] loadMessageDestinationRefs(Element parent) {
        NodeList nodes = parent.getElementsByTagName("message-destination-ref");
        int length = nodes.getLength();
        MessageDestinationRef[] result = new MessageDestinationRef[length];
        for (int i = 0; i < length; i++) {
            Element e = (Element) nodes.item(i);
            result[i] = loadMessageDestinationRef(e, new MessageDestinationRef());
        }
        return result;
    }

    public static MessageDestinationRef loadMessageDestinationRef(Element e, MessageDestinationRef msgDestRef) {
        msgDestRef.setMessageDestinationRefName(LoaderUtil.getChildContent(e, "message-destination-ref-name"));
        msgDestRef.setMessageDestinationType(LoaderUtil.getChildContent(e, "message-destination-type"));
        msgDestRef.setMessageDestinationLink(LoaderUtil.getChildContent(e, "message-destination-link"));
        return msgDestRef;
    }

    public static void loadDescribable(Element parent, Describable desc) {
        Element[] roots = LoaderUtil.getChildren(parent, "description");
        Description[] ds = new Description[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            ds[i] = new Description();
            ds[i].setLang(root.getAttribute("lang"));
            ds[i].setContent(LoaderUtil.getContent(root));
        }
        desc.setDescription(ds);
    }

    public static void loadDisplayable(Element parent, Displayable disp) {
        loadDescribable(parent, disp);
        Element[] roots = LoaderUtil.getChildren(parent, "display-name");
        DisplayName[] ds = new DisplayName[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            ds[i] = new DisplayName();
            ds[i].setLang(root.getAttribute("lang"));
            ds[i].setContent(LoaderUtil.getContent(root));
        }
        disp.setDisplayName(ds);
        roots = LoaderUtil.getChildren(parent, "icon");
        Icon[] ic = new Icon[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            ic[i] = new Icon();
            ic[i].setLang(root.getAttribute("lang"));
            ic[i].setLargeIcon(LoaderUtil.getChildContent(root, "large-icon"));
            ic[i].setSmallIcon(LoaderUtil.getChildContent(root, "small-icon"));
        }
        disp.setIcon(ic);
    }

    public static RunAs loadRunAs(Element parent) {
        if(parent == null) {
            return null;
        }
        RunAs as = new RunAs();
        loadDescribable(parent, as);
        as.setRoleName(LoaderUtil.getChildContent(parent, "role-name"));
        return as;
    }
}
