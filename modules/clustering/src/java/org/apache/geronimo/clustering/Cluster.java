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

package org.apache.geronimo.clustering;

import java.util.List;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A 'Cluster' is a point of connection between all 'Cluster's with
 * the same name, running in other VMs. I hope to support different
 * types of cluster including (initially) SimpleCluster, in which
 * every node replicates every other node and CleverCluster, which
 * automagically partitions data into SubClusters etc...
 *
 * @version $Revision: 1.11 $ $Date: 2004/02/25 09:56:55 $
 */
public abstract class
  Cluster
  extends NamedMBeanImpl
{
  protected Log _log=LogFactory.getLog(Cluster.class);

  /**
   * Makes an ObjectName for a Cluster MBean with the given parameters.
   *
   * @param clusterName a <code>String</code> value
   * @return an <code>ObjectName</code> value
   * @exception Exception if an error occurs
   */
  public static ObjectName
    makeObjectName(String clusterName)
    throws Exception
  {
    return new ObjectName("geronimo.clustering:role=Cluster,name="+clusterName);
  }

  /**
   * Return current Cluster members.
   *
   * @return a <code>List</code> value
   */
  public abstract List getMembers();

  /**
   * Return the Object which this Cluster is responsible for
   * maintaining via e.g. replication.
   *
   * @return a <code>Data</code> value
   */
  public abstract Data getData();

  /**
   * Add the given node to this Cluster.
   *
   * @param member an <code>Object</code> value
   */
  public abstract void join(Object member);

  /**
   * Remove the given node from this Cluster.
   *
   * @param member an <code>Object</code> value
   */
  public abstract void leave(Object member);

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public void
    doStart()
  {
    _log.debug("starting");
  }

  public void
    doStop()
  {
    _log.debug("stopping");
  }

  public void
    doFail()
  {
    _log.debug("failing");
  }
  /*
  public void
    setMBeanContext(GeronimoMBeanContext context)
  {
    super.setMBeanContext(context);
    _log=LogFactory.getLog(Cluster.class.getName()+"#"+getName());
  }
  */
    /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
    // set target class in concrete subclasses...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Members", true, false, "Cluster's current membership"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Data",    true, false, "Cluster's current state"));
    return mbeanInfo;
  }
  */
}
