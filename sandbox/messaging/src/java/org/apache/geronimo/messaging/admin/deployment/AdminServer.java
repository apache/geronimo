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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.plugin.DeploymentServer;
import org.apache.geronimo.deployment.plugin.FailedProgressObject;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.proxy.EndPointProxyInfo;
import org.apache.xmlbeans.XmlObject;

/**
 * Administration DeploymentServer.
 * <BR>
 * It is in charge of:
 * <UL>
 * <LI>dispatching DeploymentServer operations to deployment Targets; and
 * <LI>consolidating their results.
 * </UL>
 *
 * TODO This implementation assumes that the set of Targets is static.
 *
 * @version $Revision: 1.4 $ $Date: 2004/06/03 14:32:50 $
 */
public class AdminServer
    extends AbstractEndPoint
    implements DeploymentServer
{

    /**
     * Target name to ServerInfo map.
     */
    private final Map nameToInfo;
    
    private final ReplacerResolver replacerResolver;
    
    /**
     * Creates an administration server mounted by the specified node and
     * having the provided identifier. 
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     */
    public AdminServer(Node aNode, Object anID) {
        super(aNode, anID);
        
        replacerResolver = new DeploymentReplacerResolver();
        
        nameToInfo = new HashMap();
        NodeInfo[] nodes =
            (NodeInfo[]) node.getRemoteNodeInfos().toArray(new NodeInfo[0]);
        for (int i = 0; i < nodes.length; i++) {
            NodeInfo nodeInfo = nodes[i];
            EndPointProxyInfo proxyInfo =
                new EndPointProxyInfo(ManagedServer.END_POINT_ID,
                    DeploymentServer.class, nodeInfo);
            ServerInfo info = new ServerInfo();
            info.server =
                (DeploymentServer) node.factoryEndPointProxy(proxyInfo);
            info.target = new TargetImpl2(nodeInfo.getName(), null);
            nameToInfo.put(nodeInfo.getName(), info);
        }
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
    
    public boolean isLocal() {
        return false;
    }

    public Target[] getTargets() throws IllegalStateException {
        ServerInfo[] info;
        synchronized(nameToInfo) {
            info = (ServerInfo[]) nameToInfo.values().toArray(new ServerInfo[0]);
        }
        Target[] targets = new Target[info.length];
        for (int i = 0; i < targets.length; i++) {
            targets[i] = info[i].target;
        }
        return targets;
    }

    public TargetModuleID[] getRunningModules(final ModuleType moduleType,
        Target[] targetList) throws TargetException {
        ArrayAggregator aggregator = new ArrayAggregator();
        aggregator.setArrayType(new TargetModuleID[0]);
        aggregator.aggregate(targetList, new TargetCommand() {
            public Object execute(
                DeploymentServer aServer, Target[] aTargets)
                throws TargetException {
                return aServer.getRunningModules(moduleType, aTargets);
            }
        });
        return (TargetModuleID[]) aggregator.getAggregatedResult();
    }

    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType,
        Target[] targetList) throws TargetException {
        ArrayAggregator aggregator = new ArrayAggregator();
        aggregator.setArrayType(new TargetModuleID[0]);
        aggregator.aggregate(targetList, new TargetCommand() {
            public Object execute(
                DeploymentServer aServer, Target[] aTargets)
                throws TargetException {
                return aServer.getNonRunningModules(moduleType, aTargets);
            }
        });
        return (TargetModuleID[]) aggregator.getAggregatedResult();
    }

    public TargetModuleID[] getAvailableModules(final ModuleType moduleType,
        Target[] targetList) throws TargetException {
        ArrayAggregator aggregator = new ArrayAggregator();
        aggregator.setArrayType(new TargetModuleID[0]);
        aggregator.aggregate(targetList, new TargetCommand() {
            public Object execute(
                DeploymentServer aServer, Target[] aTargets)
                throws TargetException {
                return aServer.getAvailableModules(moduleType, aTargets);
            }
        });
        return (TargetModuleID[]) aggregator.getAggregatedResult();
    }

    public ProgressObject distribute(Target[] targetList,
        ConfigurationBuilder builder, InputStream jis, XmlObject plan) {
        // validates the deployment configuration data, generates all
        // container specific classes and interfaces.
        final File configFile;
        try {
            configFile = File.createTempFile("deploy", ".car");
            builder.buildConfiguration(configFile, null, jis, plan);
        } catch (Exception e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE,
                e.getMessage());
        }
        // moves the fully baked archive to the designated deployment targets.
        ProgressObjectAggregator aggregator = new ProgressObjectAggregator();
        try {
            aggregator.aggregate(targetList, new TargetCommand() {
                public Object execute(
                    DeploymentServer aServer, Target[] aTargets)
                    throws TargetException {
                    try {
                        return aServer.distribute(aTargets, null,
                            new FileInputStream(configFile), null);
                    } catch (FileNotFoundException e) {
                        IllegalStateException exc = new IllegalStateException();
                        exc.initCause(e);
                        throw exc;
                    }
                }
            });
        } catch (TargetException e) {
            // Never thrown.
            throw new AssertionError(e);
        }
        return (ProgressObject) aggregator.getAggregatedResult();
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) {
        TargetModuleIDAggregator aggregator = new TargetModuleIDAggregator();
        aggregator.aggregate(moduleIDList, new TargetModuleIDCommand() {
            public ProgressObject execute(DeploymentServer aServer,
                TargetModuleID[] anIDst) {
                return aServer.start(anIDst);
            }
        });
        return aggregator.getAggregatedResult();
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) {
        TargetModuleIDAggregator aggregator = new TargetModuleIDAggregator();
        aggregator.aggregate(moduleIDList, new TargetModuleIDCommand() {
            public ProgressObject execute(DeploymentServer aServer,
                TargetModuleID[] anIDst) {
                return aServer.stop(anIDst);
            }
        });
        return aggregator.getAggregatedResult();
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) {
        TargetModuleIDAggregator aggregator = new TargetModuleIDAggregator();
        aggregator.aggregate(moduleIDList, new TargetModuleIDCommand() {
            public ProgressObject execute(DeploymentServer aServer,
                TargetModuleID[] anIDst) {
                return aServer.undeploy(anIDst);
            }
        });
        return aggregator.getAggregatedResult();
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public void release() {
        synchronized(nameToInfo) {
            Collection info = nameToInfo.values(); 
            for (Iterator iter = info.iterator(); iter.hasNext();) {
                ServerInfo curInfo = (ServerInfo) iter.next();
                node.releaseEndPointProxy(curInfo.server);
                iter.remove();
            }
        }
    }

    private interface TargetCommand {
        public Object execute(DeploymentServer aServer, Target[] aTargets)
            throws TargetException;
    }
 
    private abstract class TargetAggregator {
        
        public abstract Object getAggregatedResult();
        public abstract void addResult(Object anOpaque);
        
        public void aggregate(Target[] targetList, TargetCommand aCommand)
            throws TargetException {
            Map tmpMap = new HashMap();
            synchronized(nameToInfo) {
                tmpMap.putAll(nameToInfo);
            }
            for (int i = 0; i < targetList.length; i++) {
                if ( null == nameToInfo.get(targetList[i].getName()) ) {
                    throw new IllegalArgumentException(targetList[i] +
                        " does not exist.");
                }
            }
            for (int i = 0; i < targetList.length; i++) {
                ServerInfo info =
                    (ServerInfo) nameToInfo.get(targetList[i].getName());
                Object opaque =
                    aCommand.execute(info.server, new Target[] {targetList[i]});
                addResult(opaque);
            }
        }
        
    }
    
    private class ArrayAggregator extends TargetAggregator {

        private final Collection result = new ArrayList();
        private Object[] arrayType;
        
        public void setArrayType(Object[] anArrayType) {
            arrayType = anArrayType;
        }
        
        public Object getAggregatedResult() {
            return result.toArray(arrayType);
        }

        public void addResult(Object anOpaque) {
            if ( null == anOpaque ) {
                return;
            }
            TargetModuleID[] ids = (TargetModuleID[]) anOpaque;
            for (int i = 0; i < ids.length; i++) {
                result.add(ids[i]);
            }
        }

    }

    private class ProgressObjectAggregator extends TargetAggregator {

        private final MultiProgressObject result = new MultiProgressObject();
        
        public Object getAggregatedResult() {
            result.consolidate();
            return result;
        }

        public void addResult(Object anOpaque) {
            ProgressObject progress = (ProgressObject) anOpaque;
            result.addProgressObject(progress);
        }
        
    }
    
    private interface TargetModuleIDCommand {
        public ProgressObject execute(
            DeploymentServer aServer, TargetModuleID[] anIDst);
    }
    
    private class TargetModuleIDAggregator {
        
        private final MultiProgressObject result = new MultiProgressObject();
        
        public ProgressObject getAggregatedResult() {
            return result;
        }

        public void addResult(ProgressObject aProgress) {
            result.addProgressObject(aProgress);
        }
        
        public void aggregate(TargetModuleID[] moduleIDList,
            TargetModuleIDCommand aCommand) {
            Map tmpMap = new HashMap();
            synchronized(nameToInfo) {
                tmpMap.putAll(nameToInfo);
            }
            for (int i = 0; i < moduleIDList.length; i++) {
                Target target= moduleIDList[i].getTarget();
                if ( null == nameToInfo.get(target.getName()) ) {
                    throw new IllegalArgumentException(
                        target + " does not exist.");
                }
            }
            for (int i = 0; i < moduleIDList.length; i++) {
                Target target= moduleIDList[i].getTarget();
                ServerInfo info =
                    (ServerInfo) tmpMap.get(target.getName());
                ProgressObject progress =
                    aCommand.execute(info.server,
                        new TargetModuleID[] {moduleIDList[i]});
                addResult(progress);
            }
        }
        
    }

    private static class ServerInfo {
        private DeploymentServer server;
        private Target target;
    }
    
}
