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

package org.apache.geronimo.connector;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.WaitingException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/25 21:07:03 $
 *
 * */
public class ResourceAdapterWrapper implements GBean, DynamicGBean {

    private static final GBeanInfo GBEAN_INFO;

    private final Class resourceAdapterClass;

    private final BootstrapContext bootstrapContext;

    private final ResourceAdapter resourceAdapter;

    private final DynamicGBeanDelegate delegate;

    //default constructor for enhancement proxy endpoint
    public ResourceAdapterWrapper() {
        this.resourceAdapterClass = null;
        this.bootstrapContext = null;
        this.resourceAdapter = null;
        this.delegate = null;
    }

    public ResourceAdapterWrapper(Class resourceAdapterClass, BootstrapContext bootstrapContext) throws InstantiationException, IllegalAccessException {
        this.resourceAdapterClass = resourceAdapterClass;
        this.bootstrapContext = bootstrapContext;
        resourceAdapter = (ResourceAdapter) resourceAdapterClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(resourceAdapter);
    }

    public Class getResourceAdapterClass() {
        return resourceAdapterClass;
    }

    public void registerManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) throws ResourceException {
        managedConnectionFactory.setResourceAdapter(resourceAdapter);
    }

    //endpoint handling
    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        resourceAdapter.start(bootstrapContext);
    }

    public void doStop() throws WaitingException {
        resourceAdapter.stop();
    }

    public void doFail() {
        resourceAdapter.stop();
    }

    public Object getAttribute(String name) throws Exception {
        return delegate.getAttribute(name);
    }

    public void setAttribute(String name, Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        //we have no dynamic operations
        return null;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ResourceAdapterWrapper.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("ResourceAdapterClass"));
        infoFactory.addReference(new GReferenceInfo("BootstrapContext", BootstrapContext.class.getName()));

        infoFactory.addOperation(new GOperationInfo("registerManagedConnectionFactory", new String[]{ManagedConnectionFactory.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("endpointActivation", new String[]{MessageEndpointFactory.class.getName(), ActivationSpec.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("endpointDeactivation", new String[]{MessageEndpointFactory.class.getName(), ActivationSpec.class.getName()}));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ResourceAdapterClass", "BootstrapContext"},
                new Class[]{Class.class, BootstrapContext.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
