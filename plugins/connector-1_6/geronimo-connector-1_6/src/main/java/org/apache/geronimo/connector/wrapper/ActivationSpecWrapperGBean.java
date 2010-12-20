/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector.wrapper;

import org.apache.geronimo.bval.ValidatorFactoryGBean;
import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 *
 * @version $Revision$
 */

@GBean(j2eeType = NameFactory.JCA_ACTIVATION_SPEC)
public class ActivationSpecWrapperGBean extends ActivationSpecWrapper implements DynamicGBean {

    private final DynamicGBeanDelegate delegate;

    public ActivationSpecWrapperGBean(@ParamAttribute(name="activationSpecClass")final String activationSpecClass,
                                      @ParamAttribute(name="containerId")final String containerId,
                                      @ParamReference(name="ResourceAdapaterWrapper", namingType = NameFactory.RESOURCE_ADAPTER)final ResourceAdapterWrapper resourceAdapterWrapper,
                                      @ParamSpecial(type = SpecialAttributeType.classLoader)final ClassLoader cl,
                                      @ParamReference(name = "ValidatorFactory", namingType = NameFactory.VALIDATOR_FACTORY) ValidatorFactoryGBean validatorFactory) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(activationSpecClass, containerId, resourceAdapterWrapper, cl, validatorFactory != null ? validatorFactory.getFactory() : null);
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

}
