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

package org.apache.geronimo.clustering;

import java.util.List;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Node is an instance of a connection to a Cluster. Nodes are named
 * uniquely within their Cluster and VM. A VM may contain more than
 * one Node.
 *
 * @version $Rev$ $Date$
 */
public class Node extends NamedMBeanImpl implements MetaDataListener, DataListener, DataDeltaListener {
    protected Log _log = LogFactory.getLog(Node.class);

    /**
     * Makes an ObjectName for a Node MBean with the given parameters.
     *
     * @param clusterName a <code>String</code> value
     * @param nodeName    a <code>String</code> value
     * @return an <code>ObjectName</code> value
     * @throws Exception if an error occurs
     */
    public static ObjectName makeObjectName(String clusterName, String nodeName) throws Exception {
        return new ObjectName("geronimo.clustering:role=Node,name=" + nodeName + ",cluster=" + clusterName);
    }

    //----------------------------------------
    // Node
    //----------------------------------------

    protected Cluster _cluster;

    /**
     * Returns the Node's Cluster's MBean's unique identifier.
     *
     * @return a <code>String</code> value
     */
    public String getClusterName() {
        return _objectName.getKeyProperty("cluster");
    }

    public Cluster getCluster() {
        return _cluster;
    }

    public ObjectName getClusterObjectName() {
        return _cluster == null ? null : _cluster.getObjectName();
    }

    //----------------------------------------
    // MetaDataListener
    //----------------------------------------

    public void setMetaData(List members) {
        _log.debug("membership changed: " + members);
    }

    //----------------------------------------
    // DataListener
    //----------------------------------------

    protected Data _data;

    public Data getData() {
        return _data;
    }

    public void setData(Data data) {
        String xtra = "we must be the first node up";

        if (data != null) {
            xtra = "we are joining an extant cluster";
            _data = data;
        } else {
            _data = new Data();
        }

        _log.debug("initialising data - " + xtra);
    }

    //----------------------------------------
    // DataDeltaListener
    //----------------------------------------

    public void applyDataDelta(DataDelta delta) {
        _log.trace("applying data delta - " + delta);
    }

    //----------------------------------------
    // GeronimoMBeanTarget
    //----------------------------------------

    public boolean canStart() {
        if (!super.canStart()) return false;

        if (_objectName.getKeyProperty("cluster") == null) {
            _log.warn("NodeMBean name must contain a 'cluster' property");
            return false;
        }

        // should we really be altering our state in this method ?
        try {
            _cluster = (Cluster) _server.getAttribute(Cluster.makeObjectName(_objectName.getKeyProperty("cluster")), "Reference");
        } catch (Exception e) {
            _log.error("could not find Cluster", e);
            return false;
        }

        return true;
    }

    public void doStart() {
        _log.debug("starting");

        synchronized (_cluster) {
            Data data = _cluster.getData();
            _log.debug("state transfer - sending: " + data);
            setData(data);
            _cluster.join(this);
        }
    }

    public void doStop() {
        _log.debug("stopping");
        _cluster.leave(this);
    }

    public void doFail() {
        _log.warn("failing");
        _cluster.leave(this);    // TODO - ??
    }
    /*
    public void
      setMBeanContext(GeronimoMBeanContext context)
    {
      super.setMBeanContext(context);
      _log=LogFactory.getLog(getClass().getName()+"#"+getClusterName()+"/"+getName());
    }
    */
    /*
    public static GeronimoMBeanInfo
      getGeronimoMBeanInfo()
    {
      GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
      mbeanInfo.setTargetClass(Node.class);
      mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterName",       true, false, "Node's Cluster's Name"));
      mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterObjectName", true, false, "Node's Cluster's ObjectName"));
      mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Data",              true, false, "Node's state"));

      return mbeanInfo;
    }
    */
}
