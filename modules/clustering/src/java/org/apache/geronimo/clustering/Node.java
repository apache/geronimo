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

import java.util.List;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Node is an instance of a connection to a Cluster. Nodes are named
 * uniquely within their Cluster and VM. A VM may contain more than
 * one Node.
 *
 * @version $Revision: 1.8 $ $Date: 2004/01/22 09:13:16 $
 */
public class
  Node
  extends NamedMBeanImpl
  implements MetaDataListener, DataListener, DataDeltaListener
{
  protected Log     _log=LogFactory.getLog(Node.class);

  /**
   * Makes an ObjectName for a Node MBean with the given parameters.
   *
   * @param clusterName a <code>String</code> value
   * @param nodeName a <code>String</code> value
   * @return an <code>ObjectName</code> value
   * @exception Exception if an error occurs
   */
  public static ObjectName
    makeObjectName(String clusterName, String nodeName)
    throws Exception
  {
    return new ObjectName("geronimo.clustering:role=Node,name="+nodeName+",cluster="+clusterName);
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
  public String getClusterName(){return _objectName.getKeyProperty("cluster");}

  public Cluster getCluster(){return _cluster;}

  public ObjectName getClusterObjectName(){return _cluster==null?null:_cluster.getObjectName();}

  //----------------------------------------
  // MetaDataListener
  //----------------------------------------

  public void
    setMetaData(List members)
  {
    _log.info("membership changed: "+members);
  }

  //----------------------------------------
  // DataListener
  //----------------------------------------

  protected Data _data;

  public Data getData() {return _data;}

  public void
    setData(Data data)
  {
    String xtra="we must be the first node up";

    if (data!=null)
    {
      xtra="we are joining an extant cluster";
      _data=data;
    }
    else
    {
      _data=new Data();
    }

    _log.debug("initialising data - "+xtra);
  }

  //----------------------------------------
  // DataDeltaListener
  //----------------------------------------

  public void
    applyDataDelta(DataDelta delta)
  {
    _log.trace("applying data delta - "+delta);
  }

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean
    canStart()
  {
    if (!super.canStart()) return false;

    if (_objectName.getKeyProperty("cluster")==null)
    {
      _log.warn("NodeMBean name must contain a 'cluster' property");
      return false;
    }

    // should we really be altering our state in this method ?
    try
    {
      _cluster=(Cluster)_server.getAttribute(Cluster.makeObjectName(_objectName.getKeyProperty("cluster")), "Reference");
    }
    catch (Exception e)
    {
      _log.error("could not find Cluster", e);
      return false;
    }

    return true;
  }

  public void
    doStart()
  {
    _log.info("starting");

    synchronized (_cluster)
    {
      Data data=_cluster.getData();
      _log.info("state transfer - sending: "+data);
      setData(data);
      _cluster.join(this);
    }
  }

  public void
    doStop()
  {
    _log.info("stopping");
    _cluster.leave(this);
  }

  public void
    doFail()
  {
    _log.info("failing");
    _cluster.leave(this);	// TODO - ??
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
