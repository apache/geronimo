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
 * A 'Cluster' is a point of connection between all 'Cluster's with
 * the same name, running in other VMs. I hope to support different
 * types of cluster including (initially) SimpleCluster, in which
 * every node replicates every other node and CleverCluster, which
 * automagically partitions data into SubClusters etc...
 *
 * @version $Revision: 1.10 $ $Date: 2004/01/22 09:13:16 $
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
