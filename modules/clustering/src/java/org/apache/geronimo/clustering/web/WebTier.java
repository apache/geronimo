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
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.clustering.Tier;

/**
 * Responsible for maintaining state stored in the Web tier -
 * i.e. HttpSessions.
 *
 * @version $Revision: 1.9 $ $Date: 2004/03/10 09:58:22 $
 */
public class
  WebTier
  extends Tier
{
  //  protected Log _log=LogFactory.getLog(WebTier.class);

  //----------------------------------------
  // WebTier
  //----------------------------------------

  protected Object alloc(){return new HashMap();}
  public Object registerData(String uid, Object data) {synchronized (_tier) {return ((Map)_tier).put(uid, data);}}
  public Object deregisterData(String uid) {synchronized (_tier){return ((Map)_tier).remove(uid);}}

  public int
    getWebAppCount()
  {
    return ((Map)_tier).size();
  }

  public int
    getHttpSessionCount()
  {
    int count=0;
    synchronized (_tier)	// values() returns a view, so we need to hold a lock...
    {
      for (Iterator i=((Map)_tier).values().iterator(); i.hasNext();)
      {
	Map webapp=(Map)i.next();
	// TODO - how we synchronise here depends on the webapps locking strategy - NYI...
	synchronized (webapp){count+=webapp.size();}
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
    mbeanInfo.setTargetClass(WebTier.class);
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("WebAppCount",      true, false, "Number of WebApps deployed in this Tier"));
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("HttpSessionCount", true, false, "Number of HttpSessions stored in this Tier"));
    return mbeanInfo;
  }
  */
}
