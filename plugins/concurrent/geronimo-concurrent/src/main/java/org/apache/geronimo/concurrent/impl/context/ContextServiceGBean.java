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
package org.apache.geronimo.concurrent.impl.context;

import java.util.List;

import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.context.BasicContextService;
import org.apache.geronimo.concurrent.impl.ContextHandlerUtils;
import org.apache.geronimo.concurrent.naming.ModuleAwareResourceSource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.Bundle;

public class ContextServiceGBean implements ModuleAwareResourceSource {

    public static final GBeanInfo GBEAN_INFO;

    private ManagedContextHandlerChain mainContextHandler;
    private BasicContextService contextService;

    public ContextServiceGBean(Kernel kernel,
                               Bundle bundle,
                               AbstractName name,
                               String[] contextHandlerClasses) {
        List<ManagedContextHandler> handlers =
            ContextHandlerUtils.loadHandlers(bundle, contextHandlerClasses);
        this.mainContextHandler = new ManagedContextHandlerChain(handlers);
    }

    private synchronized BasicContextService getContextService() {
        if (this.contextService == null) {
            this.contextService = new BasicContextService(this.mainContextHandler);
        }
        return this.contextService;
    }

    public Object $getResource(AbstractName moduleID) {
        return new ContextServiceModuleFacade(getContextService(), moduleID);
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ContextServiceGBean.class, "ContextService");

        infoFactory.addAttribute("bundle", ClassLoader.class, false, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false, false);
        infoFactory.addAttribute("kernel", Kernel.class, false, false);

        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.setConstructor(new String[] {"kernel",
                                                 "bundle",
                                                 "abstractName",
                                                 "contextHandlers"} );

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
