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


package org.apache.geronimo.openejb;

import java.util.Collections;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.geronimo.datasource.DataSourceService;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.JndiFactory;
import org.apache.xbean.naming.context.ImmutableFederatedContext;

/**
 * @version $Rev$ $Date$
 */
public class XBeanJndiFactory implements JndiFactory {
    private final Context rootContext;

    XBeanJndiFactory() throws NamingException {
        DeepBindableContext context = new DeepBindableContext("openejb", false, true, true, false);
        rootContext = context.newContextWrapper();
        rootContext.createSubcontext("local");
        rootContext.createSubcontext("remote");
        rootContext.createSubcontext("client");
        rootContext.createSubcontext("Deployment");
        rootContext.createSubcontext("global");
    }

    @Override
    public Context createComponentContext(Map<String, Object> bindings) throws SystemException {
        try {
            DeepBindableContext context = new DeepBindableContext("", false, true, true, false);
            Context rootContext = new ImmutableFederatedContext("", Collections.<Context>singleton(context));
            return context.newContextWrapper(rootContext);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
//            boolean hasEnv = false;
//            for (String name : bindings.keySet()) {
//                if (name.startsWith("java:comp/env")) {
//                    hasEnv = true;
//                    break;
//                }
//            }
//            if (!hasEnv) bindings.put("java:comp/env/dummy", "dummy");
//
//            WritableContext context = null;
//            try {
//                context = new WritableContext("", bindings);
//            } catch (NamingException e) {
//                throw new IllegalStateException(e);
//            }
//            return context;
    }

    @Override
    public Context createRootContext() {
        return rootContext;
    }

    public void addGlobals(Map<String, Object> globals) {
        for (Map.Entry<String, Object> entry: globals.entrySet()) {
            if (entry.getValue() instanceof Reference) {
                Reference ref = (Reference)entry.getValue();
                if(ref.getClassName().equals(DataSourceService.class.getName())) {
                    String name = "openejb/global/" + entry.getKey();
                    try {
                        rootContext.bind(name, entry.getValue());
                    } catch (Exception ignore) {
                        //??
                    }
                }
            }
        }
    }

    public void removeGlobals(Map<String, Object> globals) {
        for (Map.Entry<String, Object> entry: globals.entrySet()) {
            if (entry.getValue() instanceof Reference) {
                Reference ref = (Reference)entry.getValue();
                if(ref.getClassName().equals(DataSourceService.class.getName())) {
                    String name = "openejb/global/" + entry.getKey();
                    try {
                        rootContext.unbind(name);
                    } catch (NamingException e) {
                        //??
                    }
                }
            }
        }
    }
}

