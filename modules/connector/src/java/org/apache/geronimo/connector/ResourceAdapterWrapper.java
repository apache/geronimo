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
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * Dynamic GBean wrapper around a ResourceAdapter object, exposing the config-properties as
 * GBean attributes.
 *
 * @version $Rev$ $Date$
 */
public class ResourceAdapterWrapper implements GBeanLifecycle, DynamicGBean, ResourceAdapter {

    public static final GBeanInfo GBEAN_INFO;

    private final String resourceAdapterClass;

    private final BootstrapContext bootstrapContext;

    private final ResourceAdapter resourceAdapter;

    private final DynamicGBeanDelegate delegate;

    /**
     *  default constructor for enhancement proxy endpoint
     */
    public ResourceAdapterWrapper() {
        this.resourceAdapterClass = null;
        this.bootstrapContext = null;
        this.resourceAdapter = null;
        this.delegate = null;
    }

    public ResourceAdapterWrapper(final String resourceAdapterClass,
                                  final GeronimoWorkManager workManager,
                                  ClassLoader cl) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.resourceAdapterClass = resourceAdapterClass;
        this.bootstrapContext = new BootstrapContextImpl(workManager);
        Class clazz = cl.loadClass(resourceAdapterClass);
        resourceAdapter = (ResourceAdapter) clazz.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(resourceAdapter);
    }

    public String getResourceAdapterClass() {
        return resourceAdapterClass;
    }

    public void registerResourceAdapterAssociation(final ResourceAdapterAssociation resourceAdapterAssociation) throws ResourceException {
        resourceAdapterAssociation.setResourceAdapter(resourceAdapter);
    }

    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        throw new IllegalStateException("Don't call this");
    }

    public void stop() {
        throw new IllegalStateException("Don't call this");
    }

    //endpoint handling
    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
        resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
    }

    public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
        resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return resourceAdapter.getXAResources(specs);
    }

    public void doStart() throws Exception {
        resourceAdapter.start(bootstrapContext);
    }

    public void doStop() {
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
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ResourceAdapterWrapper.class, NameFactory.RESOURCE_ADAPTER);
        infoBuilder.addAttribute("resourceAdapterClass", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("WorkManager", GeronimoWorkManager.class, NameFactory.JCA_WORK_MANAGER);

        infoBuilder.addOperation("registerResourceAdapterAssociation", new Class[]{ResourceAdapterAssociation.class});

        infoBuilder.addInterface(ResourceAdapter.class);

        infoBuilder.setConstructor(new String[]{"resourceAdapterClass", "WorkManager", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
