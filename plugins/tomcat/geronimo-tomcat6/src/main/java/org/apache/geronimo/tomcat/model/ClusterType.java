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

import org.apache.catalina.Cluster;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Valve;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

/**
 * <p>Java class for ClusterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClusterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClusterType", propOrder = { "manager", "channel", "valve", "listener", "clusterListener" })
public class ClusterType {

    @XmlAttribute
    protected String className = SimpleTcpCluster.class.getName();

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    @XmlElement(name = "Manager")
    protected List<ManagerType> manager;

    @XmlElement(name = "Channel")
    protected ChannelType channel;

    @XmlElement(name = "Valve")
    protected List<ValveType> valve;

    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;

    @XmlElement(name = "ClusterListener")
    protected List<ClusterListenerType> clusterListener;

    public ChannelType getChannel() {
        return channel;
    }

    public String getClassName() {
        return className;
    }

    public Cluster getCluster(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Cluster cluster = (Cluster) recipe.create(cl);
        for (ManagerType managerType : getManager()) {
            cluster.registerManager(managerType.getManager(cl));
        }
        if (cluster instanceof CatalinaCluster) {
            CatalinaCluster catalinaCluster = (CatalinaCluster) cluster;
            //Channel
            if (getChannel() != null) {
                catalinaCluster.setChannel(getChannel().getChannel(cl));
            }
            //Valve
            for (ValveType valveType : getValve()) {
                Valve valve = valveType.getValve(cl);
                catalinaCluster.addValve(valve);
            }
            //ClusterListener
            for (ClusterListenerType clusterListenerType : getClusterListener()) {
                ClusterListener clusterListener = clusterListenerType.getLifecycleListener(cl);
                catalinaCluster.addClusterListener(clusterListener);
            }
        }
        // LifecycleListener
        if (cluster instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) cluster;
            for (ListenerType listenerType : getListener()) {
                lifecycle.addLifecycleListener(listenerType.getLifecycleListener(cl));
            }
        }
        return cluster;
    }

    public List<ClusterListenerType> getClusterListener() {
        return clusterListener;
    }

    public List<ListenerType> getListener() {
        if (listener == null) {
            listener = new ArrayList<ListenerType>();
        }
        return listener;
    }

    public List<ManagerType> getManager() {
        if (manager == null) {
            manager = new ArrayList<ManagerType>();
        }
        return manager;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public List<ValveType> getValve() {
        if (valve == null) {
            valve = new ArrayList<ValveType>();
        }
        return valve;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
