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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.openejb.cdi.SharedOwbContext;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.webbeans.config.WebBeansContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:$ $Date:$
 */

@GBean
public class AppInfoGBean implements SharedOwbContext, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AppInfoGBean.class);

    private final AppInfo appInfo;
    private final ClassLoader classLoader;
    private final OpenEjbSystem openEjbSystem;
    private final AppContext appContext;

    public AppInfoGBean(@ParamAttribute(name = "appInfo")AppInfo appInfo,
                        @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                        @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem) throws NamingException, IOException, OpenEJBException {
        this.appInfo = appInfo;
        this.classLoader = classLoader;
        this.openEjbSystem = openEjbSystem;
        this.appContext = openEjbSystem.createApplication(appInfo, classLoader, false);

    }

    @Override
    public void doStart() throws Exception {
    }

    @Override
    public void doStop() {
        try {
            openEjbSystem.removeApplication(appInfo, classLoader);
        } catch (NoSuchApplicationException e) {
            log.error("Module does not exist.", e);
        } catch (UndeployException e) {
            List<Throwable> causes = e.getCauses();
            log.error(e.getMessage() + ": Encountered " + causes.size() + " failures.");
            for (Throwable throwable : causes) {
                log.info(throwable.toString(), throwable);
            }
        }
    }

    @Override
    public void doFail() {
        doStop();
    }

    @Override
    public WebBeansContext getOWBContext() {
        return appContext.getWebBeansContext();
    }

    public List<BeanContext> getModuleBeanContexts(URI moduleURI) {
        List<BeanContext> beanContexts = new ArrayList<BeanContext>();
        for (BeanContext beanContext: appContext.getDeployments()) {
            if (moduleURI.toString().equals(beanContext.getModuleID())) {
                beanContexts.add(beanContext);
            }
        }
        return beanContexts;
    }

    public EjbJarInfo getEjbJarInfo(URI moduleURI) {
        return getEjbJarInfo(appInfo, moduleURI);
    }

    public static EjbJarInfo getEjbJarInfo(AppInfo appInfo, URI moduleURI) {
        for (EjbJarInfo info: appInfo.ejbJars) {
            if (moduleURI.equals(info.moduleUri)) {
                return info;
            }
        }
        //differing interpretation of standalone modules.  Openejb uses the module file name as moduleURI and geronimo uses an empty string.
        if (appInfo.ejbJars.size() == 1 && moduleURI.toString().isEmpty()) {
            return appInfo.ejbJars.get(0);
        }
        throw new IllegalStateException("No module named '" + moduleURI + "' found in appInfo " + appInfo);
    }
}
