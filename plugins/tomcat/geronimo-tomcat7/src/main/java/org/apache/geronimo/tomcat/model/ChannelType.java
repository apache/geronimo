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

package org.apache.geronimo.tomcat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChannelType")
public class ChannelType {

    @XmlAttribute
    protected String className = GroupChannel.class.getName();

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    @XmlElement(name = "Membership")
    protected MembershipType membership;

    @XmlElement(name = "Interceptor")
    protected List<InterceptorType> interceptor;

    @XmlElement(name = "Receiver")
    protected ReceiverType receiver;

    @XmlElement(name = "Sender")
    protected SenderType sender;

    public Channel getChannel(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Channel channel = (Channel) recipe.create(cl);
        for (InterceptorType interceptorType : getInterceptor()) {
            channel.addInterceptor(interceptorType.getInterceptor(cl));
        }
        if (channel instanceof ManagedChannel) {
            ManagedChannel managedChannel = (ManagedChannel) channel;
            if (sender != null) {
                managedChannel.setChannelSender(sender.getSender(cl));
            }
            if (receiver != null) {
                managedChannel.setChannelReceiver(receiver.getReceiver(cl));
            }
            if (membership != null) {
                managedChannel.setMembershipService(membership.getMembership(cl));
            }
        }
        return channel;
    }

    public String getClassName() {
        return className;
    }

    public MembershipType getMembership() {
        return membership;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public ReceiverType getReceiver() {
        return receiver;
    }

    public SenderType getSender() {
        return sender;
    }

    public List<InterceptorType> getInterceptor() {
        if (interceptor == null) {
            interceptor = new ArrayList<InterceptorType>();
        }
        return interceptor;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMembership(MembershipType membership) {
        this.membership = membership;
    }

    public void setReceiver(ReceiverType receiver) {
        this.receiver = receiver;
    }

    public void setSender(SenderType sender) {
        this.sender = sender;
    }
}
