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


import org.apache.geronimo.deployment.model.geronimo.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EJBLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestination;
import org.w3c.dom.Element;

/**
 * Loads common Geronimo DD tags
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/05 20:18:03 $
 */
public class GeronimoJ2EELoader extends J2EELoader {
    protected org.apache.geronimo.deployment.model.j2ee.EJBRef newEJBRef() {
        return new EJBRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.EJBRef loadEJBRef(Element e) {
        EJBRef ejbRef = (EJBRef) super.loadEJBRef(e);
        ejbRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return ejbRef;
    }

    protected org.apache.geronimo.deployment.model.j2ee.EJBLocalRef newEJBLocalRef() {
        return new EJBLocalRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.EJBLocalRef loadEJBLocalRef(Element e) {
        EJBLocalRef ejbRef = (EJBLocalRef) super.loadEJBLocalRef(e);
        ejbRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return ejbRef;
    }

    protected org.apache.geronimo.deployment.model.j2ee.ResourceRef newResourceRef() {
        return new ResourceRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.ResourceRef loadResourceRef(Element e) {
        ResourceRef resourceRef = (ResourceRef) super.loadResourceRef(e);
        resourceRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return resourceRef;
    }

    protected org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef newResourceEnvRef() {
        return new ResourceEnvRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef loadResourceEnvRef(Element e) {
        ResourceEnvRef resourceEnvRef = (ResourceEnvRef) super.loadResourceEnvRef(e);
        resourceEnvRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return resourceEnvRef;
    }

    protected org.apache.geronimo.deployment.model.j2ee.ServiceRef newServiceRef() {
        return new ServiceRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.ServiceRef loadServiceRef(Element e) {
        ServiceRef serviceRef = (ServiceRef) super.loadServiceRef(e);
        return serviceRef;
    }

    protected org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef newMessageDestinationRef() {
        return new MessageDestinationRef();
    }

    protected org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef loadMessageDestinationRef(Element e) {
        MessageDestinationRef msgDestRef = (MessageDestinationRef) super.loadMessageDestinationRef(e);
        msgDestRef.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return msgDestRef;
    }


    protected org.apache.geronimo.deployment.model.j2ee.MessageDestination newMessageDestination() {
        return new MessageDestination();
    }

    protected org.apache.geronimo.deployment.model.j2ee.MessageDestination loadMessageDestination(Element e) {
        MessageDestination msgDest = (MessageDestination) super.loadMessageDestination(e);
        msgDest.setJndiName(LoaderUtil.getChildContent(e, "jndi-name"));
        return msgDest;
    }
}
