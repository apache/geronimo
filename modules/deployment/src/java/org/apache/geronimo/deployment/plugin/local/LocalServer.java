/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.JarInputStream;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.plugin.DeploymentServer;
import org.apache.geronimo.deployment.plugin.FailedProgressObject;
import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/03/10 09:58:49 $
 */
public class LocalServer implements DeploymentServer, GBean {
    private final URI rootConfigID;
    private final Target target;
    private final Kernel kernel;
    private final File storeDir;
    private ConfigurationStore store;
    private ObjectName configName;
    private ConfigurationParent parent;

    public LocalServer(URI rootConfigID, File storeDir) throws Exception {
        this.rootConfigID = rootConfigID;
        target = new TargetImpl(this.rootConfigID.toString(), null);
        kernel = new Kernel(this.rootConfigID.toString(), "geronimo.localserver");
        this.storeDir = storeDir;
    }

    public boolean isLocal() {
        return true;
    }

    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{target};
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (targetList.length != 1 || !target.equals(targetList[0])) {
            throw new TargetException("Invalid target");
        }
        return null;
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (targetList.length != 1 || !target.equals(targetList[0])) {
            throw new TargetException("Invalid target");
        }
        return null;
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (targetList.length != 1 || !target.equals(targetList[0])) {
            throw new TargetException("Invalid target");
        }
        return null;
    }

    public ProgressObject distribute(Target[] targetList, ConfigurationBuilder builder, JarInputStream jis, XmlObject plan) throws IllegalStateException {
        if (targetList.length != 1 || !target.equals(targetList[0])) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Invalid Target");
        }
        DistributeCommand command = new DistributeCommand(store, builder, jis, plan);
        new Thread(command).start();
        return command;
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        StartCommand command = new StartCommand(kernel, moduleIDList);
        new Thread(command).start();
        return command;
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        StopCommand command = new StopCommand(kernel, moduleIDList);
        new Thread(command).start();
        return command;
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void release() {
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        kernel.boot();

        ObjectName localStoreName = JMXUtil.getObjectName("geronimo.localserver:role=ConfigurationStore,type=Local");
        GBeanMBean localStore = new GBeanMBean(LocalConfigStore.GBEAN_INFO);
        localStore.setAttribute("root", storeDir);
        kernel.startGBean(localStoreName);
        store = (ConfigurationStore) localStore.getTarget();

        configName = kernel.getConfigurationManager().load(rootConfigID);
        kernel.startRecursiveGBean(configName);
        parent = (ConfigurationParent) MBeanProxyFactory.getProxy(ConfigurationParent.class, kernel.getMBeanServer(), configName);
    }

    public void doStop() throws WaitingException, Exception {
        parent = null;
        kernel.stopGBean(configName);
        kernel.getConfigurationManager().unload(configName);
        kernel.shutdown();
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("JSR88 Local Server", LocalServer.class.getName());
        infoFactory.addInterface(DeploymentServer.class);
        infoFactory.addAttribute(new GAttributeInfo("ConfigID", true));
        infoFactory.addAttribute(new GAttributeInfo("ConfigStore", true));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ConfigID", "ConfigStore"},
                new Class[]{URI.class, File.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
