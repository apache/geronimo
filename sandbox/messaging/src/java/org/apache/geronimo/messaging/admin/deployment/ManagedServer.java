/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.admin.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.Manifest;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.plugin.DeploymentServer;
import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.local.DistributeCommand;
import org.apache.geronimo.deployment.plugin.local.StartCommand;
import org.apache.geronimo.deployment.plugin.local.StopCommand;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.reference.ReferenceableEnhancer;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * Managed DeploymentServer.
 * <BR>
 * It is a DeploymentServer managed by an AdminServer. Deployments
 * operations are filtered and forwarded to managed DeploymentServers. These
 * latter perform the operations locally and return to the admin server a
 * result. Results are consolidated by the admin server, which provides a 
 * consistent view of the deployment operations.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/27 15:46:54 $
 */
public class ManagedServer
    extends AbstractEndPoint
    implements DeploymentServer
{

    /**
     * EndPoint identifier of a ManagedDeploymentServer.
     */
    public static final Object END_POINT_ID = "DeploymentServerWrapper";

    private final Target target;
    private final ConfigurationStore store;
    private final Kernel kernel;
    
    private final ReplacerResolver replacerResolver;
    
    /**
     * Creates a managed deployment server for the specified node.
     * 
     * @param aNode Hosting Node.
     * @param aStore Where the deployment are distributed.
     * @param aKernel Used to control - start, stop et cetera - and query
     * deployments.
     */
    public ManagedServer(Node aNode, ConfigurationStore aStore,
        KernelMBean aKernel) {
        super(aNode, END_POINT_ID);
        if ( null == aStore ) {
            throw new IllegalArgumentException("Store is required");
        } else if ( null == aKernel ) {
            throw new IllegalArgumentException("Kernel is required");
        }
        
        target = new TargetImpl(aNode.getNodeInfo().getName(), null);
        store = aStore;
        // TODO why the various CommandSupports take as parameter a Kernel?
        // Should be a KernelMBean
        kernel = (Kernel) aKernel;
        
        replacerResolver = new DeploymentReplacerResolver();
    }
    
    public void doStart() throws WaitingException, Exception {
        super.doStart();
        replacerResolver.online();
        node.getReplacerResolver().append(replacerResolver);
    }
    
    public void doStop() throws WaitingException, Exception {
        super.doStop();
        replacerResolver.offline();
    }
    
    public void doFail() {
        super.doFail();
        replacerResolver.offline();
    }
    
    public ProgressObject distribute(
        Target[] targetList,
        ConfigurationBuilder builder,
        InputStream jis,
        XmlObject plan)
        throws IllegalStateException {
        // The administration server has already built the module configuration.
        // Provides a No-op ConfigurationBuilder such that the provided
        // module is saved "as-is" in the data-store.
        // Injects the Referenceable interface such that the ProgressObject
        // is not marshalled.
        DistributeCommand command =
            new DistributeCommand(
                store,
                new NoOpConfigurationBuilder(),
                jis,
                null);
        new Thread(command).start();
        return (ProgressObject) ReferenceableEnhancer.enhance(command);
    }

    public TargetModuleID[] getAvailableModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return null;
    }

    public TargetModuleID[] getNonRunningModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return null;
    }

    public TargetModuleID[] getRunningModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return null;
    }

    public Target[] getTargets() throws IllegalStateException {
        return new Target[] {target};
    }

    public boolean isLocal() {
        return true;
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(
        TargetModuleID[] moduleIDList,
        InputStream moduleArchive,
        InputStream deploymentPlan)
        throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not yet supported.");
    }

    public void release() {
    }

    public ProgressObject start(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        StartCommand command = new StartCommand(kernel, moduleIDList);
        new Thread(command).start();
        return (ProgressObject) ReferenceableEnhancer.enhance(command);
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        StopCommand command = new StopCommand(kernel, moduleIDList);
        new Thread(command).start();
        return (ProgressObject) ReferenceableEnhancer.enhance(command);
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    private class NoOpConfigurationBuilder implements ConfigurationBuilder {

        public SchemaTypeLoader[] getTypeLoaders() {
            throw new UnsupportedOperationException();
        }

        public boolean canConfigure(XmlObject plan) {
            throw new UnsupportedOperationException();
        }

        public XmlObject getDeploymentPlan(URL module) throws XmlException {
            throw new UnsupportedOperationException();
        }

        public void buildConfiguration(File outfile, Manifest manifest,
            File module, XmlObject plan)
            throws IOException, DeploymentException {
            throw new UnsupportedOperationException();
        }

        public void buildConfiguration(File outfile, Manifest manifest,
            InputStream module, XmlObject plan)
            throws IOException, DeploymentException {
            FileOutputStream output = new FileOutputStream(outfile);
            try {
                OutputStream bufOut = new BufferedOutputStream(output);
                byte[] buffer = new byte[1024];
                int nbRead;
                while ( -1 != (nbRead = module.read(buffer)) ) {
                    bufOut.write(buffer);
                }
                bufOut.flush();
            } finally {
                output.close();
            }
        }
        
    }
    
}
