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
package org.apache.geronimo.clustering.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.AbstractTier;
import org.apache.geronimo.clustering.Cluster;
import org.apache.geronimo.clustering.Data;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * Responsible for maintaining state stored in the EJB tier -
 * i.e. StatefulSessions.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/02 17:52:30 $
 */
public class
  EJBTier
  extends AbstractTier
  implements GeronimoMBeanTarget
{
  protected Log _log=LogFactory.getLog(EJBTier.class);

  //----------------------------------------
  // EJBTier
  //----------------------------------------

  protected Object alloc(){return new HashMap();}
  public Object registerData(String uid, Object data) {synchronized (_tier) {return ((Map)_tier).put(uid, data);}}
  public Object deregisterData(String uid) {synchronized (_tier){return ((Map)_tier).remove(uid);}}

  public int
    getAppCount()
  {
    return ((Map)_tier).size();
  }

  public int
    getStatefulSessionCount()
  {
    int count=0;
    synchronized (_tier)	// values() returns a view, so we need to hold a lock...
    {
      for (Iterator i=((Map)_tier).values().iterator(); i.hasNext();)
      {
	Map app=(Map)i.next();
	// TODO - how we synchronise here depends on the apps locking strategy - NYI...
	synchronized (app){count+=app.size();}
      }
    }
    return count;
  }

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=AbstractTier.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(EJBTier.class);
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("AppCount", true, false, "Number of Apps deployed in this Tier"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("StatefulSessionCount", true, false, "Number of Stateful Sessions stored in this Tier"));
    return mbeanInfo;
  }
}
