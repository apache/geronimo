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
package org.apache.geronimo.clustering.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.LocalCluster;
import org.apache.geronimo.clustering.Data;
import org.apache.geronimo.clustering.Tier;
import org.apache.geronimo.clustering.MBeanImpl;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * An HttpSessionManager for &lt;distributable/&gt; webapps, which
 * backs onto the generic Geronimo clustering framework.
 *
 * @version $Revision: 1.6 $ $Date: 2004/01/07 00:15:38 $
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

  protected String _clusterName;
  public String getClusterName(){return _clusterName;}
  public void setClusterName(String clusterName){_clusterName=clusterName;}

  protected String _nodeName;
  public String getNodeName(){return _nodeName;}
  public void setNodeName(String nodeName){_nodeName=nodeName;}

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
      _tier=(Tier)_server.getAttribute(WebTier.makeObjectName(getClusterName(), getNodeName()), "Reference");
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

  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(HttpSessionManager.class);
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Size",        true, false, "number of extant HttpSessions within this webapp"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UID",         true, false, "unique identity for this webapp within this vm"));
    // TODO - these should probably become RO...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ClusterName", true, true, "name of Cluster upon which this webapp is deployed"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("NodeName",    true, true, "name of Cluster Node upon which this webapp is deployed"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ContextPath", true, true, "context path at which this webapp is deployed"));
    return mbeanInfo;
  }
}
