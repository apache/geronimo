/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.clustering;

import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

/**
 * AbstractTier abstracts code common to different 'Cluster' impls
 * into the same abstract base.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/02 17:52:30 $
 */
public abstract class
  AbstractTier
  implements GeronimoMBeanTarget
{
  protected static Log  _log=LogFactory.getLog(AbstractTier.class);
  protected ObjectName  _objectName;
  protected ObjectName  _cluster;
  protected MBeanServer _server;
  protected Data        _data;
  protected Map         _tiers;
  protected Object      _tier;

  //----------------------------------------
  // Tier
  //----------------------------------------

  // share with other classes ?
  protected String
    getKeyProperty(String key, String dft)
  {
    String value=_objectName.getKeyProperty(key);

    if (value==null)
    {
      value=dft;
      _log.warn("MBean name should contain '"+key+"' property - defaulting to: "+value);
    }

    return value;
  }

  public String getClusterName() {return getKeyProperty("cluster", "GERONIMO");}
  public String getNodeName() {return getKeyProperty("node", "0");}
  public String getName() {return getKeyProperty("name", "unknown_tier");}

  protected abstract Object alloc();
  public abstract Object registerData(String uid, Object data);
  public abstract Object deregisterData(String uid);

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean canStart() {return true;}

  public synchronized void
    doStart()
  {
    _log.info("starting");

    // find our cluster
    try
    {
      _cluster=new ObjectName("geronimo.clustering:role=Cluster,name="+getClusterName()+",node="+getNodeName()); // TODO - should be a static in AbstractCluster
      // register our session map with it's Data object
      // perhaps we need an intermediate Object representing the WebTier here  // TODO - YES, abstract out..
      Data data=(Data)_server.getAttribute(_cluster, "Data");
      _log.info("Data:"+data);
      _tiers=data.getTiers(); // immutable, so doesn't need synchronisation
    }
    catch (Exception e)
    {
      _log.error("could not retrieve Cluster state", e);
    }

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
  }


  public boolean canStop() {return true;}
  public void doStop() {}
  public void doFail() {}

  public void
    setMBeanContext(GeronimoMBeanContext context)
  {
    _objectName=(context==null)?null:context.getObjectName();
    _server=(context==null)?null:context.getServer();
  }

  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=new GeronimoMBeanInfo();
    //set target class in concrete subclass
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Name",        true, false, "Name of this Tier"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("NodeName",    true, false, "Name of this Tier's Node"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterName", true, false, "Name of this Tier's Node's Cluster"));
    mbeanInfo.addOperationInfo(new GeronimoOperationInfo("registerData",
							 new GeronimoParameterInfo[] {new GeronimoParameterInfo("uid", String.class, "uid of webapp"),new GeronimoParameterInfo("data", Object.class, "data to be held")},
							 MBeanOperationInfo.ACTION,
							 "Register data with Tier state manager"));
    return mbeanInfo;
  }
}
