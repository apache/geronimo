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

package org.apache.geronimo.clustering.web;

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.MBeanImpl;
import org.apache.geronimo.clustering.Tier;

/**
 * An HttpSessionManager for &lt;distributable/&gt; webapps, which
 * backs onto the generic Geronimo clustering framework.
 *
 * @version $Rev$ $Date$
 */
public class
  HttpSessionManager
  extends MBeanImpl
{
  protected Log _log=LogFactory.getLog(HttpSessionManager.class);
  //----------------------------------------
  // HttpSessionManager
  //----------------------------------------

  protected Map _sessions=new HashMap();

  public int getSize(){return _sessions.size();}

  protected Tier _tier;
  public Tier getTier(){return _tier;}

  public ObjectName getTierObjectName() {return _tier==null?null:_tier.getObjectName();}

  protected String _clusterName;
  public String getClusterName(){return _clusterName;}
  public void setClusterName(String clusterName){_clusterName=clusterName;}

  protected String _nodeName;
  public String getNodeName(){return _nodeName;}
  public void setNodeName(String nodeName){_nodeName=nodeName;}

  protected String _tierName="web";
  public String getTierName(){return _tierName;}
  public void setTierName(String tierName){_tierName=tierName;}

  protected String _contextPath;
  public String getContextPath(){return _contextPath;}
  public void setContextPath(String contextPath){_contextPath=contextPath;}

  protected String _uid;
  public String getUID(){return _uid;}

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean
    canStart()
  {
    if (!super.canStart()) return false;

    try
    {
      // find our tier
      _tier=(Tier)_server.getAttribute(Tier.makeObjectName(getClusterName(), getNodeName(), getTierName()), "Reference");
      _log.debug("Tier: "+_tier);
    }
    catch (Exception e)
    {
      _log.error("could not find Tier", e);
      return false;
    }

    return true;
  }

  public void
    doStart()
  {
    _uid=_contextPath;		// TODO - what does Greg say ?
    _log=LogFactory.getLog(getClass().getName()+"#"+getUID());
    _log.info("starting");
    _tier.registerData(getUID(),_sessions);
    _log.info("sessions registered: "+getUID());

      // test stuff
    _sessions.put("aaa", new Object());
    _sessions.put("bbb", new Object());
    _sessions.put("ccc", new Object());
  }

  public void
    doStop()
  {
    _log.info("stopping");

    _tier.deregisterData(getUID());
    // TODO - leave cluster
  }
  /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(HttpSessionManager.class);
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Size",           true, false, "number of extant HttpSessions within this webapp"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UID",            true, false, "unique identity for this webapp within this vm"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("TierObjectName", true, false, "ObjectName of Tier to which this webapp is attached"));

    // TODO - these should probably become RO...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterName",    true, true, "name of Cluster upon which this webapp is deployed"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("NodeName",       true, true, "name of Cluster Node upon which this webapp is deployed"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("TierName",       true, true, "name of Tier to which this webapp is attached"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ContextPath",    true, true, "context path at which this webapp is deployed"));

    return mbeanInfo;
  }
  */
}
