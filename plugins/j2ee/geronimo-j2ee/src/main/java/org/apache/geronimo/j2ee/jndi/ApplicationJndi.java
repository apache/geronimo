/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.j2ee.jndi;

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gjndi.FederatedContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.xbean.naming.context.ImmutableContext;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class ApplicationJndi implements GBeanLifecycle {

    private final Context applicationContext;
    private final Context globalAdditions;
    private final Map<String, Object> liveGlobalMap;
    private final FederatedContext globalContext;

    public ApplicationJndi(@ParamAttribute(name = "globalContextSegment") Map<String, Object> globalContextSegment,
                           @ParamAttribute(name = "applicationContextMap") Map<String, Object> applicationContext,
                           @ParamReference(name = "GlobalContext", namingType = "Context") FederatedContext globalContext,
                           @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                           @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                           @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle
                           ) throws NamingException {
        this.liveGlobalMap = EnterpriseNamingContext.livenReferencesToMap(globalContextSegment, null, kernel, classLoader, bundle, JndiScope.global.name() + "/");
        this.globalAdditions = new ImmutableContext(this.liveGlobalMap, false);
        this.globalContext = globalContext;
        this.globalContext.federateContext(this.globalAdditions);
        this.applicationContext = EnterpriseNamingContext.livenReferences(applicationContext, null, kernel, classLoader, bundle, JndiScope.app.name() + "/");
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public FederatedContext getGlobalContext() {
        return globalContext;
    }

    public Map<String, Object> getGlobalMap() {
        return liveGlobalMap;
    }

    @Override
    public void doStart() throws Exception {
    }

    @Override
    public void doStop() throws Exception {
        globalContext.unfederateContext(globalAdditions);
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }
    }
}
