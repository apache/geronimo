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

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.group.GroupChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;

/**
* @version $Rev$ $Date$
*/
public class ChannelGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

   private static final Logger log = LoggerFactory.getLogger(ChannelGBean.class);
   
   public static final String J2EE_TYPE = "Channel";
   
   private final Channel channel;
   
   public ChannelGBean(){
       channel=null;
   }

   public ChannelGBean(String className, 
           Map initParams,            
           MembershipServiceGBean membership,
           ReceiverGBean receiver,
           SenderGBean sender,
           ChannelInterceptorGBean interceptorChain) throws Exception {
       
       super(); // TODO: make it an attribute
       
       //Validate
       if (className == null){
           throw new IllegalArgumentException("Must have a 'className' attribute.");
       }
       
       //Create the Channel object
       channel = (Channel)Class.forName(className).newInstance();
       
       //Set the parameters
       setParameters(channel, initParams);
       
       // if the channel is a GroupChannel then add the sender, receiver, and membership service
       if (channel instanceof GroupChannel) {
           GroupChannel groupChannel = (GroupChannel)channel;
           //Add the MembershipService
           if (membership != null){
               groupChannel.setMembershipService((MembershipService)membership.getInternalObject());
           }
           
           //Add Receiver
           if (receiver != null){
               groupChannel.setChannelReceiver((ChannelReceiver)receiver.getInternalObject());
           }
           
           //Add Sender
           if (sender != null){
               groupChannel.setChannelSender((ChannelSender)sender.getInternalObject());
           }
       } else {
           log.warn(className + " is not an instance of GroupChannel. Did not set Receiver, Sender, or MembershipService");
       }
       
       
       //Add the interceptros
       if (interceptorChain != null){
           ChannelInterceptorGBean channelInterceptorGBean = interceptorChain;
           while(channelInterceptorGBean != null){
               channel.addInterceptor((ChannelInterceptor)channelInterceptorGBean.getInternalObject());
               channelInterceptorGBean = channelInterceptorGBean.getNextInterceptor();
           }
       }

   }

   public Object getInternalObject() {
       return channel;
   }

   public void doFail() {
       log.warn("Failed");
   }

   public void doStart() throws Exception {
       log.debug("Started channel gbean.");
   }

   public void doStop() throws Exception {
       log.debug("Stopped channel gbean.");
   }

   public static final GBeanInfo GBEAN_INFO;

   static {
       GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Channel", ChannelGBean.class, J2EE_TYPE);
       infoFactory.addAttribute("className", String.class, true);
       infoFactory.addAttribute("initParams", Map.class, true);
       infoFactory.addReference("Membership", MembershipServiceGBean.class, MembershipServiceGBean.J2EE_TYPE);
       infoFactory.addReference("Receiver", ReceiverGBean.class, ReceiverGBean.J2EE_TYPE);
       infoFactory.addReference("Sender", SenderGBean.class, SenderGBean.J2EE_TYPE);
       infoFactory.addReference("ChannelInterceptor", ChannelInterceptorGBean.class, ChannelInterceptorGBean.J2EE_TYPE);
       infoFactory.addOperation("getInternalObject", "Object");
       infoFactory.setConstructor(new String[] { 
               "className", 
               "initParams", 
               "Membership", 
               "Receiver",
               "Sender",
               "ChannelInterceptor"});
       GBEAN_INFO = infoFactory.getBeanInfo();
   }

   public static GBeanInfo getGBeanInfo() {
       return GBEAN_INFO;
   }
}
