/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;

/**
 * @version $Revision: 1.11 $ $Date: 2004/06/11 19:22:04 $
 */
public class ResourceAdapterWrapper implements GBeanLifecycle, DynamicGBean {

    public static final GBeanInfo GBEAN_INFO;

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

    public void registerManagedConnectionFactory(ResourceAdapterAssociation managedConnectionFactory) throws ResourceException {
        managedConnectionFactory.setResourceAdapter(resourceAdapter);
    }

    //endpoint handling
    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ResourceAdapterWrapper.class);
        infoFactory.addAttribute("ResourceAdapterClass", Class.class, true);

        infoFactory.addReference("BootstrapContext", BootstrapContext.class);

        infoFactory.addOperation("registerManagedConnectionFactory", new Class[]{ResourceAdapterAssociation.class});
        infoFactory.addOperation("endpointActivation", new Class[]{MessageEndpointFactory.class, ActivationSpec.class});
        infoFactory.addOperation("endpointDeactivation", new Class[]{MessageEndpointFactory.class, ActivationSpec.class});

        infoFactory.setConstructor(new String[]{"ResourceAdapterClass", "BootstrapContext"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
