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

import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;

/**
 * Base JavaBean for all geronimo EJBs in the geronimo-ejb-jar.xml DD
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/05 20:44:24 $
 */
public class Ejb {
    private String ejbName;
    private EnvEntry[] envEntry = new EnvEntry[0];
    private EjbLocalRef[] ejbLocalRef = new EjbLocalRef[0];
    private EjbRef[] ejbRef = new EjbRef[0];
    private MessageDestinationRef[] messageDestinationRef = new MessageDestinationRef[0];
    private ResourceRef[] resourceRef = new ResourceRef[0];
    private ResourceEnvRef[] resourceEnvRef = new ResourceEnvRef[0];
    private ServiceRef[] serviceRef = new ServiceRef[0];

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public EjbLocalRef[] getEjbLocalRef() {
        return ejbLocalRef;
    }

    public void setEjbLocalRef(EjbLocalRef[] ejbLocalRef) {
        this.ejbLocalRef = ejbLocalRef;
    }

    public EjbRef[] getEjbRef() {
        return ejbRef;
    }

    public void setEjbRef(EjbRef[] ejbRef) {
        this.ejbRef = ejbRef;
    }

    public EnvEntry[] getEnvEntry() {
        return envEntry;
    }

    public EnvEntry getEnvEntry(int i) {
        return envEntry[i];
    }

    public void setEnvEntry(EnvEntry[] envEntry) {
        this.envEntry = envEntry;
    }

    public void setEnvEntry(int i, EnvEntry envEntry) {
        this.envEntry[i] = envEntry;
    }

    public MessageDestinationRef[] getMessageDestinationRef() {
        return messageDestinationRef;
    }

    public MessageDestinationRef getMessageDestinationRef(int i) {
        return messageDestinationRef[i];
    }

    public void setMessageDestinationRef(MessageDestinationRef[] messageDestinationRef) {
        this.messageDestinationRef = messageDestinationRef;
    }

    public void setMessageDestinationRef(int i, MessageDestinationRef messageDestinationRef) {
        this.messageDestinationRef[i] = messageDestinationRef;
    }

    public ResourceRef[] getResourceRef() {
        return resourceRef;
    }

    public ResourceRef getResourceRef(int i) {
        return resourceRef[i];
    }

    public void setResourceRef(ResourceRef[] resourceRef) {
        this.resourceRef = resourceRef;
    }

    public void setResourceRef(int i, ResourceRef resourceRef) {
        this.resourceRef[i] = resourceRef;
    }

    public ResourceEnvRef[] getResourceEnvRef() {
        return resourceEnvRef;
    }

    public ResourceEnvRef getResourceEnvRef(int i) {
        return resourceEnvRef[i];
    }

    public void setResourceEnvRef(ResourceEnvRef[] resourceEnvRef) {
        this.resourceEnvRef = resourceEnvRef;
    }

    public void setResourceEnvRef(int i, ResourceEnvRef resourceEnvRef) {
        this.resourceEnvRef[i] = resourceEnvRef;
    }

    public ServiceRef[] getServiceRef() {
        return serviceRef;
    }

    public ServiceRef getServiceRef(int i) {
        return serviceRef[i];
    }

    public void setServiceRef(ServiceRef[] serviceRef) {
        this.serviceRef = serviceRef;
    }

    public void setServiceRef(int i, ServiceRef serviceRef) {
        this.serviceRef[i] = serviceRef;
    }
}
