/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentModule;
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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/26 05:55:26 $
 */
public class LocalServer implements DeploymentServer,GBean {
    private final URI rootConfigID;
    private final Target target;
    private final Kernel kernel;
    private ObjectName configName;
    private ConfigurationParent parent;

    public LocalServer(URI rootConfigID, File configStore) {
        this.rootConfigID = rootConfigID;
        target = new TargetImpl(this.rootConfigID.toString(), null);
        kernel = new Kernel("geronimo.localserver", LocalConfigStore.GBEAN_INFO, configStore);
    }

    public boolean isLocal() {
        return true;
    }

    public Target[] getTargets() throws IllegalStateException {
        return new Target[] {target};
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

    public ProgressObject distribute(Target[] targetList, DeploymentModule module, URI configID) throws IllegalStateException {
        if (targetList.length != 1 || !target.equals(targetList[0])) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Invalid Target");
        }
        DistributeCommand command = new DistributeCommand(target, parent, configID, kernel, module);
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
        configName = kernel.load(rootConfigID);
        kernel.getMBeanServer().invoke(configName, "startRecursive", null, null);
        parent = (ConfigurationParent) MBeanProxyFactory.getProxy(ConfigurationParent.class, kernel.getMBeanServer(), configName);
    }

    public void doStop() throws WaitingException, Exception {
        parent = null;
        kernel.getMBeanServer().invoke(configName, "stop", null, null);
        kernel.unload(configName);
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
                new String[] { "ConfigID", "ConfigStore"},
                new Class[] {URI.class, File.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
