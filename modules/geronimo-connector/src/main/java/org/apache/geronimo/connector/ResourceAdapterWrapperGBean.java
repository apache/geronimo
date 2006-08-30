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

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

/**
 * 
 * @version $Revision$
 */
public class ResourceAdapterWrapperGBean extends ResourceAdapterWrapper implements GBeanLifecycle, DynamicGBean, JCAResourceAdapter {

    private final DynamicGBeanDelegate delegate;
    private final String objectName;

    public ResourceAdapterWrapperGBean() {
        delegate=null;
        objectName = null;
    }

    public ResourceAdapterWrapperGBean(String resourceAdapterClass, WorkManager workManager, XATerminator xaTerminator, ClassLoader cl, String objectName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(resourceAdapterClass, new GeronimoBootstrapContext (workManager, xaTerminator), cl);
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ResourceAdapterWrapperGBean.class, NameFactory.JCA_RESOURCE_ADAPTER);
        infoBuilder.addAttribute("resourceAdapterClass", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);

        infoBuilder.addReference("WorkManager", WorkManager.class, NameFactory.JCA_WORK_MANAGER);
        infoBuilder.addReference("XATerminator", XATerminator.class, NameFactory.JCA_WORK_MANAGER);

        infoBuilder.addOperation("registerResourceAdapterAssociation", new Class[]{ResourceAdapterAssociation.class});

        infoBuilder.addInterface(ResourceAdapter.class);
        infoBuilder.addInterface(JCAResourceAdapter.class);

        infoBuilder.setConstructor(new String[]{"resourceAdapterClass", "WorkManager", "XATerminator", "classLoader", "objectName"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
