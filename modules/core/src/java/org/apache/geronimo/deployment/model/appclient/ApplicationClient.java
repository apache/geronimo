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
package org.apache.geronimo.deployment.model.appclient;

import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.MessageDestination;
import org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.ServiceRef;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/01 22:12:17 $
 */
public class ApplicationClient extends Displayable {
    private String version;
    private EnvEntry[] envEntry;
    private EJBRef[] ejbRef;
    private ServiceRef[] serviceRef;
    private ResourceRef[] resourceRef;
    private ResourceEnvRef[] resourceEnvRef;
    private MessageDestinationRef[] messageDestinationRef;
    private String callbackHandler;
    private MessageDestination[] messageDestination;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(String callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public EJBRef[] getEJBRef() {
        return ejbRef;
    }

    public void setEJBRef(EJBRef[] ejbRef) {
        this.ejbRef = ejbRef;
    }

    public EnvEntry[] getEnvEntry() {
        return envEntry;
    }

    public void setEnvEntry(EnvEntry[] envEntry) {
        this.envEntry = envEntry;
    }

    public MessageDestination[] getMessageDestination() {
        return messageDestination;
    }

    public void setMessageDestination(MessageDestination[] messageDestination) {
        this.messageDestination = messageDestination;
    }

    public MessageDestinationRef[] getMessageDestinationRef() {
        return messageDestinationRef;
    }

    public void setMessageDestinationRef(MessageDestinationRef[] messageDestinationRef) {
        this.messageDestinationRef = messageDestinationRef;
    }

    public ResourceEnvRef[] getResourceEnvRef() {
        return resourceEnvRef;
    }

    public void setResourceEnvRef(ResourceEnvRef[] resourceEnvRef) {
        this.resourceEnvRef = resourceEnvRef;
    }

    public ResourceRef[] getResourceRef() {
        return resourceRef;
    }

    public void setResourceRef(ResourceRef[] resourceRef) {
        this.resourceRef = resourceRef;
    }

    public ServiceRef[] getServiceRef() {
        return serviceRef;
    }

    public void setServiceRef(ServiceRef[] serviceRef) {
        this.serviceRef = serviceRef;
    }
}
