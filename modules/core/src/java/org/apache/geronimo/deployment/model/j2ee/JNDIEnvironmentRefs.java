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
package org.apache.geronimo.deployment.model.j2ee;

/**
 * Common interface for the jndiEnvironmentRefsGroup schema group.
 *
 * @version $Revision: 1.3 $ $Date: 2003/10/07 17:16:36 $
 */
public interface JNDIEnvironmentRefs {
    EJBLocalRef[] getEJBLocalRef();

    EJBLocalRef getEJBLocalRef(int i);

    void setEJBLocalRef(EJBLocalRef[] ejbLocalRef);

    void setEJBLocalRef(int i, EJBLocalRef ejbLocalRef);

    EJBRef[] getEJBRef();

    EJBRef getEJBRef(int i);

    void setEJBRef(EJBRef[] ejbRef);

    void setEJBRef(int i, EJBRef ejbRef);

    EnvEntry[] getEnvEntry();

    EnvEntry getEnvEntry(int i);

    void setEnvEntry(EnvEntry[] envEntry);

    void setEnvEntry(int i, EnvEntry envEntry);

    MessageDestinationRef[] getMessageDestinationRef();

    MessageDestinationRef getMessageDestinationRef(int i);

    void setMessageDestinationRef(MessageDestinationRef[] messageDestinationRef);

    void setMessageDestinationRef(int i, MessageDestinationRef messageDestinationRef);

    ResourceRef[] getResourceRef();

    ResourceRef getResourceRef(int i);

    void setResourceRef(ResourceRef[] resourceRef);

    void setResourceRef(int i, ResourceRef resourceRef);

    ResourceEnvRef[] getResourceEnvRef();

    ResourceEnvRef getResourceEnvRef(int i);

    void setResourceEnvRef(ResourceEnvRef[] resourceEnvRef);

    void setResourceEnvRef(int i, ResourceEnvRef resourceEnvRef);

    ServiceRef[] getServiceRef();

    ServiceRef getServiceRef(int i);

    void setServiceRef(ServiceRef[] serviceRef);

    void setServiceRef(int i, ServiceRef serviceRef);
}
