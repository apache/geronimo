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

import java.util.Map;

import javax.resource.spi.XATerminator;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.geronimo.bval.ValidatorFactoryGBean;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;

/**
 *
 * @version $Revision$
 */

@GBean(j2eeType = NameFactory.JCA_RESOURCE_ADAPTER)
public class ResourceAdapterWrapperGBean extends ResourceAdapterWrapper implements GBeanLifecycle, DynamicGBean, JCAResourceAdapter {

    private final DynamicGBeanDelegate delegate;
    private final String objectName;

    public ResourceAdapterWrapperGBean() {
        delegate=null;
        objectName = null;
    }

    public ResourceAdapterWrapperGBean(
            @ParamAttribute(name="resourceAdapterClass") String resourceAdapterClass,
            @ParamAttribute(name="messageListenerToActivationSpecMap") Map<String, String> messageListenerToActivationSpecMap,
            @ParamReference(name="WorkManager", namingType = NameFactory.JCA_WORK_MANAGER) GeronimoWorkManager workManager,
            @ParamReference(name="XATerminator", namingType = NameFactory.JCA_WORK_MANAGER)XATerminator xaTerminator,
            @ParamReference(name="TransactionManager", namingType = NameFactory.JTA_RESOURCE)RecoverableTransactionManager transactionManager,
            @ParamReference(name="TransactionSynchronizationRegistry", namingType = NameFactory.JTA_RESOURCE) TransactionSynchronizationRegistry transactionSynchronizationRegistry,
            @ParamSpecial(type= SpecialAttributeType.classLoader )ClassLoader cl,
            @ParamSpecial(type= SpecialAttributeType.objectName )String objectName,
            @ParamReference(name = "ValidatorFactory", namingType = NameFactory.VALIDATOR_FACTORY) ValidatorFactoryGBean validatorFactory) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(objectName, resourceAdapterClass, messageListenerToActivationSpecMap, new GeronimoBootstrapContext(workManager, xaTerminator, transactionSynchronizationRegistry), transactionManager, cl, validatorFactory != null ? validatorFactory.getFactory() : null);
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(resourceAdapter);
        this.objectName = objectName;
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

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

}
