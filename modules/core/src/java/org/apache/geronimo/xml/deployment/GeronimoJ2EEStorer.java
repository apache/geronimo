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
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JndiContextParam;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDIEnvironmentRefs;

/**
 * Knows how ot store common Geronimo elements from POJOs to DOM
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/01 19:03:40 $
 */
public class GeronimoJ2EEStorer {
    public static void storeEJBRefs(Element parent, EjbRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "ejb-ref");
            storeEJBRef(ref, refs[i]);
        }
    }

    static void storeEJBRef(Element parent, EjbRef ref) {
        J2EEStorer.storeEJBRef(parent, ref);
        StorerUtil.createOptionalChildTextWithNS(parent, "ger:jndi-name", "http://geronimo.apache.org/xml/schema/j2ee", ref.getJndiName());
        for(int i = 0; i < ref.getJndiContextParam().length; i++) {
            storeContextParam(StorerUtil.createChild(parent, "context-param"), ref.getJndiContextParam(i));
        }
    }

    public static void storeEJBLocalRefs(Element parent, EjbLocalRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "ejb-local-ref");
            storeEJBLocalRef(ref, refs[i]);
        }
    }

    static void storeEJBLocalRef(Element parent, EjbLocalRef ref) {
        J2EEStorer.storeEJBLocalRef(parent, ref);
        StorerUtil.createOptionalChildTextWithNS(parent, "ger:jndi-name", "http://geronimo.apache.org/xml/schema/j2ee", ref.getJndiName());
        for(int i = 0; i < ref.getJndiContextParam().length; i++) {
            storeContextParam(StorerUtil.createChild(parent, "context-param"), ref.getJndiContextParam(i));
        }
    }

    public static void storeResourceRefs(Element parent, ResourceRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "resource-ref");
            storeResourceRef(ref, refs[i]);
        }
    }

    static void storeResourceRef(Element parent, ResourceRef ref) {
        J2EEStorer.storeResourceRef(parent, ref);
        StorerUtil.createChildTextWithNS(parent, "ger:jndi-name", "http://geronimo.apache.org/xml/schema/j2ee", ref.getJndiName());
        for(int i = 0; i < ref.getJndiContextParam().length; i++) {
            storeContextParam(StorerUtil.createChild(parent, "context-param"), ref.getJndiContextParam(i));
        }
    }

    public static void storeResourceEnvRefs(Element parent, ResourceEnvRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "resource-env-ref");
            storeResourceEnvRef(ref, refs[i]);
        }
    }

    static void storeResourceEnvRef(Element parent, ResourceEnvRef ref) {
        J2EEStorer.storeResourceEnvRef(parent, ref);
        StorerUtil.createChildTextWithNS(parent, "ger:jndi-name", "http://geronimo.apache.org/xml/schema/j2ee", ref.getJndiName());
    }

    public static void storeMessageDestinationRefs(Element parent, MessageDestinationRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "message-destination-ref");
            storeMessageDestinationRef(ref, refs[i]);
        }
    }

    static void storeMessageDestinationRef(Element parent, MessageDestinationRef ref) {
        J2EEStorer.storeMessageDestinationRef(parent, ref);
        StorerUtil.createOptionalChildTextWithNS(parent, "ger:jndi-name", "http://geronimo.apache.org/xml/schema/j2ee", ref.getJndiName());
        for(int i = 0; i < ref.getJndiContextParam().length; i++) {
            storeContextParam(StorerUtil.createChild(parent, "context-param"), ref.getJndiContextParam(i));
        }
    }

    public static void storeServiceRefs(Element parent, ServiceRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element ref = StorerUtil.createChild(parent, "service-ref");
            storeServiceRef(ref, refs[i]);
        }
    }

    static void storeServiceRef(Element parent, ServiceRef ref) {
        J2EEStorer.storeServiceRef(parent, ref);
    }

    static void storeSecurityRoleRefs(Element parent, SecurityRoleRef[] refs) {
        for(int i = 0; i < refs.length; i++) {
            Element e = StorerUtil.createChild(parent, "security-role-ref");
            storeSecurityRoleRef(e, refs[i]);
        }
    }

    static void storeSecurityRoleRef(Element parent, SecurityRoleRef ref) {
        J2EEStorer.storeSecurityRoleRef(parent, ref);
    }

    private static void storeContextParam(Element parent, JndiContextParam param) {
        StorerUtil.createChildText(parent, "param-name", param.getParamName());
        StorerUtil.createChildText(parent, "param-value", param.getParamValue());
    }

    public static void storeJNDIEnvironmentRefs(Element parent, JNDIEnvironmentRefs owner) {
        J2EEStorer.storeEnvEntries(parent, owner.getEnvEntry());
        storeEJBRefs(parent, owner.getGeronimoEJBRef());
        storeEJBLocalRefs(parent, owner.getGeronimoEJBLocalRef());
        storeServiceRefs(parent, owner.getGeronimoServiceRef());
        storeResourceRefs(parent, owner.getGeronimoResourceRef());
        storeResourceEnvRefs(parent, owner.getGeronimoResourceEnvRef());
        storeMessageDestinationRefs(parent, owner.getGeronimoMessageDestinationRef());
    }
}
