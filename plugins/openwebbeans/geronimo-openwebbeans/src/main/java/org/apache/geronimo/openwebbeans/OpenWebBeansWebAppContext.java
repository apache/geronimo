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

package org.apache.geronimo.openwebbeans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.openejb.cdi.OpenWebBeansWebInitializer;
import org.apache.geronimo.openejb.cdi.SharedOwbContext;
import org.apache.webbeans.config.WebBeansContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "OpenWebBeansContext")
public class OpenWebBeansWebAppContext implements GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(OpenWebBeansWebAppContext.class);

    private static final Map<String, OpenWebBeansWebAppContext> OPENWEBBEANS_WEBAPP_CONTEXTS = new ConcurrentHashMap<String, OpenWebBeansWebAppContext>();

    private final Holder holder;

    private final SharedOwbContext sharedOwbContext;

    private final AbstractName abName;

    private WebBeansContext webBeansContext;

    private final boolean shareableWebBeansContext;

    public OpenWebBeansWebAppContext(@ParamAttribute(name = "holder") Holder holder,
            @ParamReference(name = "SharedOwbContext") SharedOwbContext sharedOwbContext,
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName) {
        this.holder = holder;
        this.sharedOwbContext = sharedOwbContext;
        webBeansContext = sharedOwbContext == null ? null : sharedOwbContext.getOWBContext();
        if (webBeansContext == null) {
            if (logger.isDebugEnabled()) {
                if (sharedOwbContext == null) {
                    logger.debug("SharedOwbContext is null, web application {} will create its own WebBeansContext", abName.toString());
                } else {
                    logger.debug("No WebBeansContext is configured in SharedOwbContext, web application {} will create its own WebBeansContext", abName.toString());
                }
            }
            shareableWebBeansContext = false;
            webBeansContext = OpenWebBeansWebInitializer.newWebBeansContext(null);
        } else {
            shareableWebBeansContext = true;
        }
        this.abName = abName;
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        OPENWEBBEANS_WEBAPP_CONTEXTS.put(getWebModuleName(), this);
    }

    @Override
    public void doStop() throws Exception {
        OPENWEBBEANS_WEBAPP_CONTEXTS.remove(getWebModuleName());
    }

    public Holder getHolder() {
        return holder;
    }

    public SharedOwbContext getSharedOwbContext() {
        return sharedOwbContext;
    }

    public String getWebModuleName() {
        return abName.getNameProperty(NameFactory.WEB_MODULE);
    }

    public WebBeansContext getWebBeansContext() {
        return webBeansContext;
    }

    public boolean isShareableWebBeansContext() {
        return shareableWebBeansContext;
    }

    public static OpenWebBeansWebAppContext getOpenWebBeansWebAppContext(String moduleName) {
        return OPENWEBBEANS_WEBAPP_CONTEXTS.get(moduleName);
    }
}
