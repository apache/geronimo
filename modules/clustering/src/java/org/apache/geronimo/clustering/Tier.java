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

import java.util.Map;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tier abstracts code common to different Tier impls
 * into the same abstract base.
 *
 *
 * @version $Revision: 1.10 $ $Date: 2004/03/10 09:58:21 $
 */
public abstract class
  Tier
  extends NamedMBeanImpl
{
  protected Log    _log=LogFactory.getLog(Tier.class);
  protected Data   _data;
  protected Map    _tiers;
  protected Object _tier;


  /**
   * Makes an ObjectName for a Tier MBean with the given parameters.
   *
   * @param clusterName a <code>String</code> value
   * @param nodeName a <code>String</code> value
   * @param tierName a <code>String</code> value
   * @return an <code>ObjectName</code> value
   * @exception Exception if an error occurs
   */
  public static ObjectName
    makeObjectName(String clusterName, String nodeName, String tierName)
    throws Exception
  {
    return new ObjectName("geronimo.clustering:role=Tier,name="+tierName+",node="+nodeName+",cluster="+clusterName);
  }

  //----------------------------------------
  // Tier
  //----------------------------------------

  protected Node   _node;
  public Node getNode(){return _node;}

  public ObjectName getNodeObjectName(){return _node==null?null:_node.getObjectName();}

  public String getClusterName(){return _objectName.getKeyProperty("cluster");}
  public String getNodeName(){return _objectName.getKeyProperty("node");}

  protected abstract Object alloc();
  public abstract Object registerData(String uid, Object data);
  public abstract Object deregisterData(String uid);

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean
    canStart()
  {
    if (!super.canStart()) return false;

    if (_objectName.getKeyProperty("cluster")==null)
    {
      _log.warn("Tier MBean name must contain a 'cluster' property");
      return false;
    }

    if (_objectName.getKeyProperty("node")==null)
    {
      _log.warn("Tier MBean name must contain a 'node' property");
      return false;
    }

    try
    {
      _node=(Node)_server.getAttribute(Node.makeObjectName(getClusterName(), getNodeName()), "Reference");
      _log.debug("Node: "+_node);
    }
    catch (Exception e)
    {
      _log.error("could not find Node", e);
      return false;
    }

    return true;
  }

  public synchronized void
    doStart()
  {
    _log.info("starting");

    // register our session map with it's Data object
    Data data=_node.getData();
    _tiers=data.getTiers(); // immutable, so doesn't need synchronisation
    _tier=null;
    synchronized (_tiers)
    {
      _tier=_tiers.get(getName());
      if (_tier==null)
      {
	_tier=alloc();
	_tiers.put(getName(), _tier);
      }
      // tier storage now initialised...
    }
    _log.info("Node Data:"+data);
  }
  /*
  public void
    setMBeanContext(GeronimoMBeanContext context)
  {
    super.setMBeanContext(context);
    _log=LogFactory.getLog(getClass().getName()+"#"+getClusterName()+"/"+getNodeName()+"/"+getName());
  }
  */
    /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
    //set target class in concrete subclass
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterName",    true, false, "Name of this Tier's Node's Cluster"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("NodeName",       true, false, "Name of this Tier's Node"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("NodeObjectName", true, false, "ObjectName of this Tier's Node"));
    return mbeanInfo;
  }
  */
}
