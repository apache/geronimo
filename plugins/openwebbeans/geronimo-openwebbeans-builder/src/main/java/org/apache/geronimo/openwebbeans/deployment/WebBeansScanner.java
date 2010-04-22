/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.geronimo.openwebbeans.deployment;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.ScannerService;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev $Date
 */
public final class WebBeansScanner {
    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(WebBeansScanner.class);

    /** Root container. */
    // Activities are removed from the specification.
   // private final BeanManagerImpl rootManager;

    /** XML discovery. */
    // XML discovery is removed from the specification. It is here for next revisions of spec.
   // private final WebBeansXMLConfigurator xmlDeployer;

    /** Deploy discovered beans */
   // private final BeansDeployer deployer;

    private final Bundle bundle;

    private JNDIService jndiService;

    private ScannerService scannerService;

    private ContextsService contextsService;

    /**
     * Creates a new WebBeansScanner instance and initializes the instance variables.
     */
    public WebBeansScanner(Bundle _bundle) {
        
        this.bundle=_bundle;
    }

    public void scanWebBeans() {

       //scan webbeans here

    }

    Set<Bean<?>> getWebBeans() {

        return Collections.EMPTY_SET;
    }

}