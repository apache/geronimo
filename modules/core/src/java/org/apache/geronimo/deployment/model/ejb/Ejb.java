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
package org.apache.geronimo.deployment.model.ejb;

import org.apache.geronimo.deployment.model.j2ee.Displayable;

/**
 * 
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/01 22:12:16 $
 */
public class Ejb extends Displayable {
    private String ejbName;
    private String ejbClass;
    private SecurityIdentity securityIdentity;
    private Object[] ejbRef = new Object[0];
    private Object[] ejbLocalRef = new Object[0];
    private Object[] resourceRef = new Object[0];
    private Object[] resourceEnvRef = new Object[0];
    private Object[] messageDestinationRef = new Object[0];
    private Object[] envEntry = new Object[0];
    private Object[] serviceRefGroup = new Object[0];

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String ejbClass) {
        this.ejbClass = ejbClass;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public Object[] getEjbLocalRef() {
        return ejbLocalRef;
    }

    public Object getEjbLocalRef(int i) {
        return ejbLocalRef[i];
    }

    public void setEjbLocalRef(Object[] ejbLocalRef) {
        this.ejbLocalRef = ejbLocalRef;
    }

    public void setEjbLocalRef(int i, Object ejbLocalRef) {
        this.ejbLocalRef[i] = ejbLocalRef;
    }

    public Object[] getEjbRef() {
        return ejbRef;
    }

    public Object getEjbRef(int i) {
        return ejbRef[i];
    }

    public void setEjbRef(Object[] ejbRef) {
        this.ejbRef = ejbRef;
    }

    public void setEjbRef(int i, Object ejbRef) {
        this.ejbRef[i] = ejbRef;
    }

    public Object[] getEnvEntry() {
        return envEntry;
    }

    public Object getEnvEntry(int i) {
        return envEntry[i];
    }

    public void setEnvEntry(Object[] envEntry) {
        this.envEntry = envEntry;
    }

    public void setEnvEntry(int i, Object envEntry) {
        this.envEntry[i] = envEntry;
    }

    public Object[] getMessageDestinationRef() {
        return messageDestinationRef;
    }

    public Object getMessageDestinationRef(int i) {
        return messageDestinationRef[i];
    }

    public void setMessageDestinationRef(Object[] messageDestinationRef) {
        this.messageDestinationRef = messageDestinationRef;
    }

    public void setMessageDestinationRef(int i, Object messageDestinationRef) {
        this.messageDestinationRef[i] = messageDestinationRef;
    }

    public Object[] getResourceRef() {
        return resourceRef;
    }

    public Object getResourceRef(int i) {
        return resourceRef[i];
    }

    public void setResourceRef(Object[] resourceRef) {
        this.resourceRef = resourceRef;
    }

    public void setResourceRef(int i, Object resourceRef) {
        this.resourceRef[i] = resourceRef;
    }

    public Object[] getResourceEnvRef() {
        return resourceEnvRef;
    }

    public Object getResourceEnvRef(int i) {
        return resourceEnvRef[i];
    }

    public void setResourceEnvRef(Object[] resourceEnvRef) {
        this.resourceEnvRef = resourceEnvRef;
    }

    public void setResourceEnvRef(int i, Object resourceEnvRef) {
        this.resourceEnvRef[i] = resourceEnvRef;
    }

    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    public Object[] getServiceRefGroup() {
        return serviceRefGroup;
    }

    public Object getServiceRefGroup(int i) {
        return serviceRefGroup[i];
    }

    public void setServiceRefGroup(Object[] serviceRefGroup) {
        this.serviceRefGroup = serviceRefGroup;
    }

    public void setServiceRefGroup(int i, Object serviceRefGroup) {
        this.serviceRefGroup[i] = serviceRefGroup;
    }
}
