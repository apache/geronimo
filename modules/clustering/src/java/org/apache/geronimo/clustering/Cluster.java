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
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * A 'Cluster' is the in-vm representative of a Cluster of Geronimo
 * nodes. The particular cluster to which it belongs is identified by
 * it's 'name' property. I hope to support different types of cluster
 * including (initially) SimpleCluster, in which every node replicates
 * every other node and CleverCluster, which automagically partitions
 * data into SubClusters etc...
 *
 * @version $Revision: 1.6 $ $Date: 2004/01/04 14:18:06 $
 */
public abstract class
  Cluster
  implements GeronimoMBeanTarget
{
  protected Log _log=LogFactory.getLog(Cluster.class);

  public static ObjectName
    makeObjectName(String clusterName)
    throws Exception
  {
    return new ObjectName("geronimo.clustering:role=Cluster,name="+clusterName);
  }

  protected ObjectName 	_objectName;
  protected MBeanServer _server;

  /**
   * Return current Cluster members.
   *
   * @return a <code>List</code> value
   */
  public abstract List getMembers();

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

  /**
   * Return a local reference to this Object. For tight coupling via
   * JMX (bad idea?).
   *
   * @return a <code>Cluster</code> value
   */
  public Cluster getReference(){return this;}

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean
    canStart()
  {
    if (_objectName.getKeyProperty("name")==null)
    {
      _log.warn("ClusterMBean name must contain a 'name' property");
      return false;
    }

    return true;
  }

  public boolean canStop(){return true;}

  public void
    doStart()
  {
    _log=LogFactory.getLog(Cluster.class.getName()+"#"+_objectName.getKeyProperty("name"));
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

  public void
    setMBeanContext(GeronimoMBeanContext context)
  {
    _objectName=(context==null)?null:context.getObjectName();
    _server    =(context==null)?null:context.getServer();
  }

  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=new GeronimoMBeanInfo();
    // set target class in concrete subclasses...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Reference", true, false, "a local reference to this Cluster"));
    return mbeanInfo;
  }
}
