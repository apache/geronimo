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
package org.apache.geronimo.deployment.model.geronimo.ejb;

import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDIEnvironmentRefs;

/**
 * JavaBean for the geronimo-ejb-jar.xml tag session
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/17 01:47:14 $
 */
public class Session extends org.apache.geronimo.deployment.model.ejb.Session implements JNDIEnvironmentRefs {
    private String jndiName;

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }


    public void setGeronimoSecurityRoleRef(org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef[] ref) {
        assert (ref instanceof SecurityRoleRef[]);
        super.setSecurityRoleRef(ref);
    }

    public void setGeronimoSecurityRoleRef(int i, org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef ref) {
        assert (ref instanceof SecurityRoleRef);
        super.setSecurityRoleRef(i, ref);
    }

    public void setGeronimoSecurityRoleRef(int i, SecurityRoleRef ref) {
        super.setSecurityRoleRef(i, ref);
    }

    public SecurityRoleRef getGeronimoSecurityRoleRef(int i) {
        return (SecurityRoleRef)super.getSecurityRoleRef(i);
    }

    public void setGeronimoSecurityRoleRef(SecurityRoleRef[] ref) {
        super.setSecurityRoleRef(ref);
    }

    public SecurityRoleRef[] getGeronimoSecurityRoleRef() {
        return (SecurityRoleRef[])super.getSecurityRoleRef();
    }

    public void setEJBLocalRef(org.apache.geronimo.deployment.model.j2ee.EJBLocalRef[] ejbRef) {
        assert (ejbRef instanceof EjbLocalRef[]);
        super.setEJBLocalRef(ejbRef);
    }

    public void setEJBLocalRef(int i, org.apache.geronimo.deployment.model.j2ee.EJBLocalRef ejbRef) {
        assert (ejbRef instanceof EjbLocalRef);
        super.setEJBLocalRef(i, ejbRef);
    }

    public EjbLocalRef getGeronimoEJBLocalRef(int i) {
        return (EjbLocalRef) getEJBLocalRef(i);
    }

    public void setGeronimoEJBLocalRef(int i, EjbLocalRef ejbRef) {
        super.setEJBLocalRef(i, ejbRef);
    }

    public EjbLocalRef[] getGeronimoEJBLocalRef() {
        return (EjbLocalRef[]) getEJBLocalRef();
    }

    public void setGeronimoEJBLocalRef(EjbLocalRef[] ejbRef) {
        super.setEJBLocalRef(ejbRef);
    }

    public void setEJBRef(org.apache.geronimo.deployment.model.j2ee.EJBRef[] ejbRef) {
        assert (ejbRef instanceof EjbRef[]);
        super.setEJBRef(ejbRef);
    }

    public void setEJBRef(int i, org.apache.geronimo.deployment.model.j2ee.EJBRef ejbRef) {
        assert (ejbRef instanceof EjbRef);
        super.setEJBRef(i, ejbRef);
    }

    public EjbRef getGeronimoEJBRef(int i) {
        return (EjbRef) getEJBRef(i);
    }

    public void setGeronimoEJBRef(int i, EjbRef ejbRef) {
        super.setEJBRef(i, ejbRef);
    }

    public EjbRef[] getGeronimoEJBRef() {
        return (EjbRef[]) getEJBRef();
    }

    public void setGeronimoEJBRef(EjbRef[] ejbRef) {
        super.setEJBRef(ejbRef);
    }

    public void setMessageDestinationRef(org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef[] messageDestinationRef) {
        assert (messageDestinationRef instanceof MessageDestinationRef[]);
        super.setMessageDestinationRef(messageDestinationRef);
    }

    public void setMessageDestinationRef(int i, org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef messageDestinationRef) {
        assert (messageDestinationRef instanceof MessageDestinationRef);
        super.setMessageDestinationRef(i, messageDestinationRef);
    }

    public MessageDestinationRef getGeronimoMessageDestinationRef(int i) {
        return (MessageDestinationRef) super.getMessageDestinationRef(i);
    }

    public void setGeronimoMessageDestinationRef(int i, MessageDestinationRef messageDestinationRef) {
        super.setMessageDestinationRef(i, messageDestinationRef);
    }

    public MessageDestinationRef[] getGeronimoMessageDestinationRef() {
        return (MessageDestinationRef[]) super.getMessageDestinationRef();
    }

    public void setGeronimoMessageDestinationRef(MessageDestinationRef[] messageDestinationRef) {
        super.setMessageDestinationRef(messageDestinationRef);
    }

    public void setResourceEnvRef(org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef[] resourceEnvRef) {
        assert (resourceEnvRef instanceof ResourceEnvRef[]);
        super.setResourceEnvRef(resourceEnvRef);
    }

    public void setResourceEnvRef(int i, org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef ref) {
        assert (ref instanceof ResourceEnvRef);
        super.setResourceEnvRef(i, ref);
    }

    public ResourceEnvRef getGeronimoResourceEnvRef(int i) {
        return (ResourceEnvRef) getResourceEnvRef(i);
    }

    public void setGeronimoResourceEnvRef(int i, ResourceEnvRef resourceEnvRef) {
        super.setResourceEnvRef(i, resourceEnvRef);
    }

    public ResourceEnvRef[] getGeronimoResourceEnvRef() {
        return (ResourceEnvRef[]) getResourceEnvRef();
    }

    public void setGeronimoResourceEnvRef(ResourceEnvRef[] resourceEnvRef) {
        super.setResourceEnvRef(resourceEnvRef);
    }

    public void setResourceRef(org.apache.geronimo.deployment.model.j2ee.ResourceRef[] resourceRef) {
        assert (resourceRef instanceof ResourceRef[]);
        super.setResourceRef(resourceRef);
    }

    public void setResourceRef(int i, org.apache.geronimo.deployment.model.j2ee.ResourceRef ref) {
        assert (ref instanceof ResourceRef);
        super.setResourceRef(i, ref);
    }

    public ResourceRef getGeronimoResourceRef(int i) {
        return (ResourceRef) getResourceRef(i);
    }

    public void setGeronimoResourceRef(int i, ResourceRef resourceRef) {
        super.setResourceRef(i, resourceRef);
    }

    public ResourceRef[] getGeronimoResourceRef() {
        return (ResourceRef[]) getResourceRef();
    }

    public void setGeronimoResourceRef(ResourceRef[] resourceRef) {
        super.setResourceRef(resourceRef);
    }

    public void setServiceRef(org.apache.geronimo.deployment.model.j2ee.ServiceRef[] serviceRef) {
        assert (serviceRef instanceof ServiceRef[]);
        super.setServiceRef(serviceRef);
    }

    public void setServiceRef(int i, org.apache.geronimo.deployment.model.j2ee.ServiceRef ref) {
        assert (ref instanceof ServiceRef);
        super.setServiceRef(i, ref);
    }

    public ServiceRef[] getGeronimoServiceRef() {
        return (ServiceRef[]) getServiceRef();
    }

    public ServiceRef getGeronimoServiceRef(int i) {
        return (ServiceRef)getServiceRef(i);
    }

    public void setGeronimoServiceRef(ServiceRef[] serviceRef) {
        super.setServiceRef(serviceRef);
    }

    public void setGeronimoServiceRef(int i, ServiceRef ref) {
        super.setServiceRef(i, ref);
    }
}
