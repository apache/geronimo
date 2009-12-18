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

package org.apache.geronimo.jasper;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version $Rev$ $Date$
 */
@GBean
public class JasperJSPFactoryConfigurer implements GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(JasperJSPFactoryConfigurer.class);

    @Override
    public void doFail() {
    }

    @Override
    public void doStart() throws Exception {
        try {
            // Set JSP factory
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        } catch (Throwable t) {
            logger.warn("Couldn't initialize Jasper", t);
        }
    }

    @Override
    public void doStop() throws Exception {
        //TODO How to un-set it ?
        //JspFactory.setDefaultFactory(null);        
    }
}