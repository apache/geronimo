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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class WebContextSource implements ContextSource {

    private final Context context;

    public WebContextSource(@ParamAttribute(name = "moduleContext") Map<String, Object> moduleContext,
                            @ParamAttribute(name = "componentContext") Map<String, Object> componentContext,
                            @ParamReference(name = "TransactionManager") TransactionManager transactionManager,
                            @ParamReference(name = "ApplicationJndi") ApplicationJndi applicationJndi,
                            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) throws NamingException {
        GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
        Set<Context> contexts = new LinkedHashSet<Context>(3);
        Context localCompContext = EnterpriseNamingContext.livenReferences(componentContext, userTransaction, kernel, classLoader, bundle, "comp/");
        Context localModuleContext = EnterpriseNamingContext.livenReferences(moduleContext, null, kernel, classLoader, bundle, "module/");
        contexts.add(localCompContext);
        contexts.add(localModuleContext);
        if (applicationJndi != null) {
            if (applicationJndi.getApplicationContext() != null) {
                contexts.add(applicationJndi.getApplicationContext());
            }
            if (applicationJndi.getGlobalContext() != null) {
                contexts.add(applicationJndi.getGlobalContext());
            }
        }
        this.context = EnterpriseNamingContext.createEnterpriseNamingContext(contexts);
    }

    @Override
    public Context getContext() {
        return context;
    }
}
