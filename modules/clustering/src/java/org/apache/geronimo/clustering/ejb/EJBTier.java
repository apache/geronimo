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

package org.apache.geronimo.clustering.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.clustering.Tier;

/**
 * Responsible for maintaining state stored in the EJB tier -
 * i.e. StatefulSessions.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/25 09:56:55 $
 */
public class
  EJBTier
  extends Tier
{
  //  protected Log _log=LogFactory.getLog(EJBTier.class);

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
  /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=Tier.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(EJBTier.class);
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("AppCount", true, false, "Number of Apps deployed in this Tier"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("StatefulSessionCount", true, false, "Number of Stateful Sessions stored in this Tier"));
    return mbeanInfo;
  }
  */
}
