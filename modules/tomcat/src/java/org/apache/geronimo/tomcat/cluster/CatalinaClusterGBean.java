/**
*
* Copyright 2003-2005 The Apache Software Foundation
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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.Valve;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterDeployer;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterSender;
import org.apache.catalina.cluster.MembershipService;
import org.apache.catalina.cluster.MessageListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.apache.geronimo.tomcat.ValveGBean;

/**
* @version $Rev: 233391 $ $Date: 2005-08-18 16:38:47 -0600 (Thu, 18 Aug 2005) $
*/
public class CatalinaClusterGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

   private static final Log log = LogFactory.getLog(CatalinaClusterGBean.class);
   
   public static final String J2EE_TYPE = "Cluster";
   
   private final CatalinaCluster cluster;

   public CatalinaClusterGBean(String className, 
           Map initParams,            
           MembershipServiceGBean membership,
           ReceiverGBean receiver,
           SenderGBean sender,
           MessageListenerGBean messageListenerChain,
           ValveGBean tomcatValveChain,
           ClusterDeployerGBean deployer) throws Exception {
       
       super(); // TODO: make it an attribute
       
       //Validate
       if (className == null){
           throw new IllegalArgumentException("Must have a 'className' attribute.");
       }
              
       //Create the CatalinaCluster object
       cluster = (CatalinaCluster)Class.forName(className).newInstance();
       
       //Set the parameters
       setParameters(cluster, initParams);
       
       //Add the MembershipService
       if (membership != null){
           cluster.setMembershipService((MembershipService)membership.getInternalObject());
       }
       
       //Add Receiver
       if (receiver != null){
           cluster.setClusterReceiver((ClusterReceiver)receiver.getInternalObject());
       }
       
       //Add Sender
       if (sender != null){
           cluster.setClusterSender((ClusterSender)sender.getInternalObject());
       }

       //Add the message listeners list
       if (messageListenerChain != null){
           MessageListenerGBean messageListenerGBean = messageListenerChain;
           while(messageListenerGBean != null){
               cluster.addClusterListener((MessageListener)messageListenerGBean.getInternalObject());
               messageListenerGBean = messageListenerGBean.getNextValve();
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
   }

   public Object getInternalObject() {
       return cluster;
   }

   public void doFail() {
       log.info("Failed");
   }

   public void doStart() throws Exception {
       log.info("Started cluster gbean.");
   }

   public void doStop() throws Exception {
       log.info("Stopped cluster gbean.");
   }

   public static final GBeanInfo GBEAN_INFO;

   static {
       GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("CatalinaCluster", CatalinaClusterGBean.class, J2EE_TYPE);
       infoFactory.addAttribute("className", String.class, true);
       infoFactory.addAttribute("initParams", Map.class, true);
       infoFactory.addReference("Membership", MembershipServiceGBean.class, MembershipServiceGBean.J2EE_TYPE);
       infoFactory.addReference("Receiver", ReceiverGBean.class, ReceiverGBean.J2EE_TYPE);
       infoFactory.addReference("Sender", SenderGBean.class, SenderGBean.J2EE_TYPE);
       infoFactory.addReference("MessageListenerChain", MessageListenerGBean.class, MessageListenerGBean.J2EE_TYPE);
       infoFactory.addReference("TomcatValveChain", ValveGBean.class, ValveGBean.J2EE_TYPE);
       infoFactory.addReference("ClusterDeployer", ClusterDeployerGBean.class, ClusterDeployerGBean.J2EE_TYPE);
       infoFactory.addOperation("getInternalObject");
       infoFactory.setConstructor(new String[] { 
               "className", 
               "initParams", 
               "Membership", 
               "Receiver",
               "Sender",
               "MessageListenerChain",
               "TomcatValveChain",
               "ClusterDeployer" });
       GBEAN_INFO = infoFactory.getBeanInfo();
   }

   public static GBeanInfo getGBeanInfo() {
       return GBEAN_INFO;
   }
}
