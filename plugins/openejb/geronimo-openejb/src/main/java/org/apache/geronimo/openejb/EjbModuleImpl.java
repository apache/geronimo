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

import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, EjbDeployment> ejbs = new HashMap<String, EjbDeployment>();
    private final ClassLoader classLoader;

    private final OpenEjbSystem openEjbSystem;
    private final EjbJarInfo ejbJarInfo;

    public EjbModuleImpl(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                         @ParamReference(name = "J2EEServer") J2EEServer server,
                         @ParamReference(name = "J2EEApplication") J2EEApplication application,
                         @ParamAttribute(name = "deploymentDescriptor") String deploymentDescriptor,
                         @ParamReference(name = "EJBCollection") Collection<? extends EjbDeployment> ejbs,
                         @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                         @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
                         @ParamAttribute(name = "ejbJarInfo") EjbJarInfo ejbJarInfo) {
        this.objectName = objectName;
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
        this.ejbJarInfo = ejbJarInfo;
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
        openEjbSystem.createEjbJar(ejbJarInfo, classLoader);
    }

    public void doStop() {
        try {
            openEjbSystem.removeEjbJar(ejbJarInfo, classLoader);
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

    public void doFail() {
        doStop();
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=EJBModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
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
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("EJBModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }
}
