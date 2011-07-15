/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.naming.NamingException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.loader.SystemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$ $Date$
 */

@GBean(j2eeType = NameFactory.EJB_MODULE)
public class EjbModuleImpl implements EJBModule, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(EjbModuleImpl.class);
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final String objectName;
    private URI moduleURI;
    private final Map<String, EjbDeployment> ejbs = new HashMap<String, EjbDeployment>();
    private final ClassLoader classLoader;

    private final OpenEjbSystem openEjbSystem;
    private final AppInfoGBean appInfoGBean;

    public EjbModuleImpl(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                         @ParamReference(name = "J2EEServer", namingType = NameFactory.J2EE_SERVER) J2EEServer server,
                         @ParamReference(name = "J2EEApplication", namingType = NameFactory.J2EE_APPLICATION) J2EEApplication application,
                         @ParamAttribute(name = "moduleURI") URI moduleURI,
                         @ParamAttribute(name = "deploymentDescriptor") String deploymentDescriptor,
                         @ParamAttribute(name = "isStandalone") boolean isStandalone,
                         @ParamReference(name = "EJBCollection") Collection<? extends EjbDeployment> ejbs,
                         @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                         @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
                         @ParamReference(name = "AppInfo") AppInfoGBean appInfoGBean) throws NamingException {
        this.objectName = objectName;
        this.moduleURI = moduleURI;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        ((ReferenceCollection) ejbs).addReferenceCollectionListener(new ReferenceCollectionListener() {

            public void memberAdded(ReferenceCollectionEvent event) {
                EjbDeployment ejb = (EjbDeployment) event.getMember();
                addEjb(ejb);
            }

            public void memberRemoved(ReferenceCollectionEvent event) {
                EjbDeployment ejb = (EjbDeployment) event.getMember();
                removeEjb(ejb);
            }
        });
        for (EjbDeployment ejb : ejbs) {
            addEjb(ejb);
        }

        this.classLoader = classLoader;

        this.openEjbSystem = openEjbSystem;
        this.appInfoGBean = appInfoGBean;
    }
    
    private void removeEjb(EjbDeployment ejb) {
        GeronimoThreadContextListener.get().removeEjb(ejb.getDeploymentId());
        ejbs.remove(ejb.getDeploymentId());
    }

    private void addEjb(EjbDeployment ejb) {
        ejbs.put(ejb.getDeploymentId(), ejb);
        GeronimoThreadContextListener.get().addEjb(ejb);
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getObjectName();
    }

    public String[] getJavaVMs() {
        return server.getJavaVMs();
    }

    public String[] getEjbs() {
        if (ejbs == null) {
            return new String[0];
        }

        ArrayList<EJB> copy;
        synchronized (ejbs) {
            copy = new ArrayList<EJB>(ejbs.values());
        }

        String[] result = new String[copy.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (copy.get(i)).getObjectName();
        }
        return result;
    }

    public void doStart() throws Exception {
        List<BeanContext> allDeployments = appInfoGBean.getModuleBeanContexts(moduleURI);
        //start code from openejb assembler

                // deploy
                for (BeanContext deployment : allDeployments) {
                    try {
                        Container container = deployment.getContainer();
                        container.deploy(deployment);
                        log.info("createApplication.createdEjb" + deployment.getDeploymentID() + deployment.getEjbName() + container.getContainerID());
                        if (log.isDebugEnabled()) {
                            for (Map.Entry<Object, Object> entry : deployment.getProperties().entrySet()) {
                                log.info("createApplication.createdEjb.property" + deployment.getEjbName() + entry.getKey() + entry.getValue());
                            }
                        }
                    } catch (OpenEJBException e) {
                        log.warn("Apparent double start of ejb?? ", e);
                    } catch (Throwable t) {
                        throw new OpenEJBException("Error deploying '"+deployment.getEjbName()+"'.  Exception: "+t.getClass()+": "+t.getMessage(), t);
                    }
                }

                // start
                for (BeanContext deployment : allDeployments) {
                    try {
                        Container container = deployment.getContainer();
                        container.start(deployment);
                        log.info("createApplication.startedEjb" + deployment.getDeploymentID() + deployment.getEjbName() + container.getContainerID());
                    } catch (Throwable t) {
                        throw new OpenEJBException("Error starting '"+deployment.getEjbName()+"'.  Exception: "+t.getClass()+": "+t.getMessage(), t);
                    }
                }
        EjbResolver globalEjbResolver = SystemInstance.get().getComponent(EjbResolver.class);
        globalEjbResolver.add(appInfoGBean.getEjbJarInfo(moduleURI));

        for (String deploymentId: ejbs.keySet()) {
            BeanContext beanContext = openEjbSystem.getDeploymentInfo(deploymentId);
            GeronimoThreadContextListener.get().getEjbDeployment(beanContext);
        }
    }

    public void doStop() {
            Iterator<EjbDeployment> it= ejbs.values().iterator();
            while(it.hasNext())
            {
                GeronimoThreadContextListener.get().removeEjb(it.next().getDeploymentId());  
                it.remove();
            }

    }

    public void doFail() {
        doStop();
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=EJBModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     * @param objectName object name to verify
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"EJBModule".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("EJBModule object name j2eeType property must be 'EJBModule'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("EJBModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEApplication")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEApplication property", objectName);
        }
        
        // EJB in WAR, The war itself is one big EjbModule
        if (keyPropertyList.size() == 5 && keyPropertyList.containsKey("WebModule")) {
            return;
        }
        
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("EJBModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }
}
