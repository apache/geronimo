/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.manager.ResourceManager;

/**
 * 
 * @version $Revision$
 */
public class ActivationSpecWrapperGBean extends ActivationSpecWrapper implements DynamicGBean {

    private final DynamicGBeanDelegate delegate;

    public ActivationSpecWrapperGBean() {
        delegate = null;
    }

    public ActivationSpecWrapperGBean(final String activationSpecClass, final String containerId, final ResourceAdapterWrapper resourceAdapterWrapper, final ClassLoader cl) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(activationSpecClass, containerId, resourceAdapterWrapper, cl);
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(activationSpec);
    }

    //DynamicGBean implementation

    /**
     * Delegating DynamicGBean getAttribute method.
     *
     * @param name of attribute.
     * @return attribute value.
     * @throws Exception
     */
    public Object getAttribute(final String name) throws Exception {
        return delegate.getAttribute(name);
    }

    /**
     * Delegating DynamicGBean setAttribute method.
     *
     * @param name  of attribute.
     * @param value of attribute to be set.
     * @throws Exception
     */
    public void setAttribute(final String name, final Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    /**
     * no-op DynamicGBean method
     *
     * @param name
     * @param arguments
     * @param types
     * @return nothing, there are no operations.
     * @throws Exception
     */
    public Object invoke(final String name, final Object[] arguments, final String[] types) throws Exception {
        //we have no dynamic operations.
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ActivationSpecWrapperGBean.class, NameFactory.JCA_ACTIVATION_SPEC);
        infoBuilder.addAttribute("activationSpecClass", String.class, true);
        infoBuilder.addAttribute("containerId", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ResourceAdapterWrapper", ResourceAdapterWrapper.class, NameFactory.RESOURCE_ADAPTER);

        infoBuilder.addOperation("activate", new Class[]{MessageEndpointFactory.class});
        infoBuilder.addOperation("deactivate", new Class[]{MessageEndpointFactory.class});

        infoBuilder.addInterface(ResourceManager.class);

        infoBuilder.setConstructor(new String[]{
            "activationSpecClass",
            "containerId",
            "ResourceAdapterWrapper",
            "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
