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

import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InterceptorType")
public class InterceptorType {

    @XmlAttribute
    protected String className;

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    @XmlElement(name = "Member")
    protected List<MemberType> member;

    public String getClassName() {
        return className;
    }

    public ChannelInterceptor getInterceptor(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        ChannelInterceptor channelInterceptor = (ChannelInterceptor) recipe.create(cl);
        //Special handle for staticMembershipInterceptor
        if (channelInterceptor instanceof StaticMembershipInterceptor && getMember().size() > 0) {
            StaticMembershipInterceptor staticMembershipInterceptor = (StaticMembershipInterceptor) channelInterceptor;
            for (MemberType memberType : getMember()) {
                staticMembershipInterceptor.addStaticMember(memberType.getMember(cl));
            }
        }
        return channelInterceptor;
    }

    public List<MemberType> getMember() {
        if (member == null) {
            member = new ArrayList<MemberType>();
        }
        return member;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
