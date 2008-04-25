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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import javax.management.ObjectName;

import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.UndeployException;
import org.apache.openejb.NoSuchApplicationException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @version $Revision$ $Date$
 */
public class EjbModuleImpl implements EJBModule {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final String objectName;
    private final Collection<? extends EJB> ejbs;
    private final ClassLoader classLoader;

    private final OpenEjbSystem openEjbSystem;
    private final EjbJarInfo ejbJarInfo;

    public EjbModuleImpl(String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor, Collection<? extends EJB> ejbs, ClassLoader classLoader, OpenEjbSystem openEjbSystem, EjbJarInfo ejbJarInfo) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        this.ejbs = ejbs;

        this.classLoader = classLoader;

        this.openEjbSystem = openEjbSystem;
        this.ejbJarInfo = ejbJarInfo;
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
            copy = new ArrayList<EJB>(ejbs);
        }

        String[] result = new String[copy.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (copy.get(i)).getObjectName();
        }
        return result;
    }


    protected void start() throws Exception {
        openEjbSystem.createEjbJar(ejbJarInfo, classLoader);
    }

    protected void stop() {
        try {
            openEjbSystem.removeEjbJar(ejbJarInfo, classLoader);
        } catch (NoSuchApplicationException e) {
            log.error("Module does not exist.", e);
        } catch (UndeployException e) {
            List<Throwable> causes = e.getCauses();
            log.error(e.getMessage()+": Encountered "+causes.size()+" failures.");
            for (Throwable throwable : causes) {
                log.info(throwable.toString(), throwable);
            }
        }
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
