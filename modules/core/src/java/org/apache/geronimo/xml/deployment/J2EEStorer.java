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

import org.apache.geronimo.deployment.model.j2ee.Describable;
import org.apache.geronimo.deployment.model.j2ee.Description;
import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.apache.geronimo.deployment.model.j2ee.Icon;
import org.apache.geronimo.deployment.model.j2ee.RunAs;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EJBLocalRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.j2ee.PortComponentRef;
import org.apache.geronimo.deployment.model.j2ee.Handler;
import org.apache.geronimo.deployment.model.j2ee.ParamValue;
import org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef;
import org.w3c.dom.Element;

/**
 * Utility methods to write common J2EE elements to a DOM tree.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/01 19:03:40 $
 */
public class J2EEStorer {
    public static void storeDescribable(Element parent, Describable desc) {
        Description[] ds = desc.getDescription();
        for(int i = 0; i < ds.length; i++) {
            Element e = StorerUtil.createChild(parent, "description");
            if(ds[i].getLang() != null && !ds[i].getLang().equals("")) {
                e.setAttribute("lang", ds[i].getLang());
            }
            StorerUtil.setText(e, ds[i].getContent());
        }
    }

    public static void storeDisplayable(Element parent, Displayable disp) {
        storeDescribable(parent, disp);
        DisplayName[] ds = disp.getDisplayName();
        for(int i = 0; i < ds.length; i++) {
            Element e = StorerUtil.createChild(parent, "display-name");
            if(ds[i].getLang() != null && !ds[i].getLang().equals("")) {
                e.setAttribute("lang", ds[i].getLang());
            }
            StorerUtil.setText(e, ds[i].getContent());
        }
        Icon[] is = disp.getIcon();
        for(int i = 0; i < is.length; i++) {
            Element icon = StorerUtil.createChild(parent, "icon");
            if(is[i].getLang() != null && !is[i].getLang().equals("")) {
                icon.setAttribute("lang", ds[i].getLang());
            }
            if(is[i].getSmallIcon() != null) {
                StorerUtil.createChildText(icon, "small-icon", is[i].getLargeIcon());
            }
            if(is[i].getLargeIcon() != null) {
                StorerUtil.createChildText(icon, "large-icon", is[i].getLargeIcon());
            }
        }
    }

    public static void storeRunAs(Element parent, RunAs as) {
        storeDescribable(parent, as);
        StorerUtil.createChildText(parent, "role-name", as.getRoleName());
    }

    public static void storeEnvEntries(Element parent, EnvEntry[] entries) {
        for(int i = 0; i < entries.length; i++) {
            Element entry = StorerUtil.createChild(parent, "env-entry");
            storeEnvEntry(entry, entries[i]);
        }
    }

    static void storeEnvEntry(Element parent, EnvEntry entry) {
        StorerUtil.createChildText(parent, "env-entry-name", entry.getEnvEntryName());
        StorerUtil.createChildText(parent, "env-entry-type", entry.getEnvEntryType());
        StorerUtil.createOptionalChildText(parent, "env-entry-value", entry.getEnvEntryValue());
    }

    public static void storeEJBRefs(Element parent, EJBRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "ejb-ref");
            storeEJBRef(ref, refs[i]);
        }
    }

    static void storeEJBRef(Element parent, EJBRef ref) {
        StorerUtil.createChildText(parent, "ejb-ref-name", ref.getEJBRefName());
        StorerUtil.createChildText(parent, "ejb-ref-type", ref.getEJBRefType());
        StorerUtil.createChildText(parent, "home", ref.getHome());
        StorerUtil.createChildText(parent, "remote", ref.getRemote());
        StorerUtil.createOptionalChildText(parent, "ejb-link", ref.getEJBLink());
    }

    public static void storeEJBLocalRefs(Element parent, EJBLocalRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "ejb-local-ref");
            storeEJBLocalRef(ref, refs[i]);
        }
    }

    static void storeEJBLocalRef(Element parent, EJBLocalRef ref) {
        StorerUtil.createChildText(parent, "ejb-ref-name", ref.getEJBRefName());
        StorerUtil.createChildText(parent, "ejb-ref-type", ref.getEJBRefType());
        StorerUtil.createChildText(parent, "local-home", ref.getLocalHome());
        StorerUtil.createChildText(parent, "local", ref.getLocal());
        StorerUtil.createOptionalChildText(parent, "ejb-link", ref.getEJBLink());
    }

    public static void storeResourceRefs(Element parent, ResourceRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "resource-ref");
            storeResourceRef(ref, refs[i]);
        }
    }

    static void storeResourceRef(Element parent, ResourceRef ref) {
        StorerUtil.createChildText(parent, "res-ref-name", ref.getResRefName());
        StorerUtil.createChildText(parent, "res-type", ref.getResType());
        StorerUtil.createChildText(parent, "res-auth", ref.getResAuth());
        StorerUtil.createOptionalChildText(parent, "res-sharing-scope", ref.getResSharingScope());
    }

    public static void storeResourceEnvRefs(Element parent, ResourceEnvRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "resource-env-ref");
            storeResourceEnvRef(ref, refs[i]);
        }
    }

    static void storeResourceEnvRef(Element parent, ResourceEnvRef ref) {
        StorerUtil.createChildText(parent, "resource-env-ref-name", ref.getResourceEnvRefName());
        StorerUtil.createChildText(parent, "resource-env-ref-type", ref.getResourceEnvRefType());
    }

    public static void storeMessageDestinationRefs(Element parent, MessageDestinationRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "message-destination-ref");
            storeMessageDestinationRef(ref, refs[i]);
        }
    }

    static void storeMessageDestinationRef(Element parent, MessageDestinationRef ref) {
        storeDescribable(parent, ref);
        StorerUtil.createChildText(parent, "message-destination-ref-name", ref.getMessageDestinationRefName());
        StorerUtil.createChildText(parent, "message-destination-type", ref.getMessageDestinationType());
        StorerUtil.createChildText(parent, "message-destination-usage", ref.getMessageDestinationUsage());
        StorerUtil.createOptionalChildText(parent, "message-destination-link", ref.getMessageDestinationLink());
    }

    public static void storeServiceRefs(Element parent, ServiceRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "service-ref");
            storeServiceRef(ref, refs[i]);
        }
    }

    static void storeServiceRef(Element parent, ServiceRef ref) {
        StorerUtil.createChildText(parent, "service-ref-name", ref.getServiceRefName());
        StorerUtil.createChildText(parent, "service-interface", ref.getServiceInterface());
        StorerUtil.createOptionalChildText(parent, "wsdl-file", ref.getWSDLFile());
        StorerUtil.createOptionalChildText(parent, "jaxrpc-mapping-file", ref.getJAXRPCMappingFile());
        StorerUtil.createOptionalChildText(parent, "service-qname", ref.getServiceQName());
        storePortComponentRefs(parent, ref.getPortComponentRef());
        storeHandlers(parent, ref.getHandler());
    }

    static void storePortComponentRefs(Element parent, PortComponentRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "port-component-ref");
            storePortComponentRef(ref, refs[i]);
        }
    }

    static void storePortComponentRef(Element parent, PortComponentRef ref) {
        StorerUtil.createChildText(parent, "service-endpoint-interface", ref.getServiceEndpointInterface());
        StorerUtil.createOptionalChildText(parent, "port-component-link", ref.getPortComponentLink());
    }

    static void storeHandlers(Element parent, Handler[] handlers) {
        for(int i = 0; i < handlers.length; i++) {
            Element ref = StorerUtil.createChild(parent, "handler");
            storeHandler(ref, handlers[i]);
        }
    }

    static void storeHandler(Element parent, Handler handler) {
        storeDisplayable(parent, handler);
        StorerUtil.createChildText(parent, "handler-name", handler.getHandlerName());
        StorerUtil.createChildText(parent, "handler-class", handler.getHandlerClass());
        storeParamValue(parent, handler.getInitParam(), "init-param");
        for(int i = 0; i < handler.getSoapHeader().length; i++) {
            StorerUtil.createChildText(parent, "soap-header", handler.getSoapHeader(i));
        }
        for(int i = 0; i < handler.getSoapRole().length; i++) {
            StorerUtil.createChildText(parent, "soap-role", handler.getSoapRole(i));
        }
        for(int i = 0; i < handler.getPortName().length; i++) {
            StorerUtil.createChildText(parent, "port-name", handler.getPortName(i));
        }
    }

    public static void storeParamValue(Element parent, ParamValue[] param, String name) {
        for(int i = 0; i < param.length; i++) {
            Element e = StorerUtil.createChild(parent, name);
            storeDescribable(e, param[i]);
            StorerUtil.createChildText(parent, "param-name", param[i].getParamName());
            StorerUtil.createChildText(parent, "param-value", param[i].getParamValue());
        }
    }

    static void storeSecurityRoleRefs(Element parent, SecurityRoleRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element e = StorerUtil.createChild(parent, "security-role-ref");
            storeSecurityRoleRef(e, refs[i]);
        }
    }

    static void storeSecurityRoleRef(Element parent, SecurityRoleRef ref) {
        storeDescribable(parent, ref);
        StorerUtil.createChildText(parent, "role-name", ref.getRoleName());
        StorerUtil.createOptionalChildText(parent, "role-link", ref.getRoleName());
    }
}
