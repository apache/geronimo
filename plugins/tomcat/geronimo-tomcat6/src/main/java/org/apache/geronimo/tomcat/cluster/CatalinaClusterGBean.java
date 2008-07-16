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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.Valve;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.tribes.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.apache.geronimo.tomcat.ValveGBean;

/**
* @version $Rev$ $Date$
*/
public class CatalinaClusterGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

   private static final Logger log = LoggerFactory.getLogger(CatalinaClusterGBean.class);
   
   public static final String J2EE_TYPE = "Cluster";
   
   private final CatalinaCluster cluster;
   
   public CatalinaClusterGBean(){
       cluster=null;
   }

   public CatalinaClusterGBean(String className, 
           Map initParams,            
           ClusterListenerGBean clusterListenerChain,
           ValveGBean tomcatValveChain,
           ClusterDeployerGBean deployer,
           ChannelGBean channel,
           ClusterManagerGBean manager) throws Exception {
       
       super(); // TODO: make it an attribute
       
       //Validate
       if (className == null){
           throw new IllegalArgumentException("Must have a 'className' attribute.");
       }
              
       //Create the CatalinaCluster object
       cluster = (CatalinaCluster)Class.forName(className).newInstance();
       
       //Set the parameters
       setParameters(cluster, initParams);
       
       //Add the cluster listeners list
       if (clusterListenerChain != null){
           ClusterListenerGBean clusterListenerGBean = clusterListenerChain;
           while(clusterListenerGBean != null){
               cluster.addClusterListener((ClusterListener)clusterListenerGBean.getInternalObject());
               clusterListenerGBean = clusterListenerGBean.getNextValve();
           }
       }

       //Add the valve list
       if (tomcatValveChain != null){
           ValveGBean valveGBean = tomcatValveChain;
           while(valveGBean != null){
               cluster.addValve((Valve)valveGBean.getInternalObject());
               valveGBean = valveGBean.getNextValve();
           }
       }
       
       //Add deployer
       if (deployer != null){
           cluster.setClusterDeployer((ClusterDeployer)deployer.getInternalObject());
       }

       //Add channel
       if (channel != null){
           cluster.setChannel((Channel)channel.getInternalObject());
       }
       
       //Add manager
       if (manager != null) {
           cluster.registerManager((ClusterManager)manager.getInternalObject());
       }
   }

   public Object getInternalObject() {
       return cluster;
   }

   public void doFail() {
       log.warn("Failed");
   }

   public void doStart() throws Exception {
       log.debug("Started cluster gbean.");
   }

   public void doStop() throws Exception {
       log.debug("Stopped cluster gbean.");
   }

   public static final GBeanInfo GBEAN_INFO;

   static {
       GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("CatalinaCluster", CatalinaClusterGBean.class, J2EE_TYPE);
       infoFactory.addAttribute("className", String.class, true);
       infoFactory.addAttribute("initParams", Map.class, true);
       infoFactory.addReference("ClusterListenerChain", ClusterListenerGBean.class, ClusterListenerGBean.J2EE_TYPE);
       infoFactory.addReference("TomcatValveChain", ValveGBean.class, ValveGBean.J2EE_TYPE);
       infoFactory.addReference("ClusterDeployer", ClusterDeployerGBean.class, ClusterDeployerGBean.J2EE_TYPE);
       infoFactory.addReference("Channel", ChannelGBean.class, ChannelGBean.J2EE_TYPE);
       infoFactory.addReference("ClusterManager", ClusterManagerGBean.class, ClusterManagerGBean.J2EE_TYPE);
       infoFactory.addOperation("getInternalObject", "Object");
       infoFactory.setConstructor(new String[] { 
               "className", 
               "initParams", 
               "ClusterListenerChain",
               "TomcatValveChain",
               "ClusterDeployer",
               "Channel",
               "ClusterManager"});
       GBEAN_INFO = infoFactory.getBeanInfo();
   }

   public static GBeanInfo getGBeanInfo() {
       return GBEAN_INFO;
   }
}
