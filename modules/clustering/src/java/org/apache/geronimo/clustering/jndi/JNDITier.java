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

package org.apache.geronimo.clustering.jndi;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.clustering.Tier;

/**
 * Responsible for maintaining state stored in the JNDI tier -
 * i.e. StatefulSessions.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:56:55 $
 */
public class
  JNDITier
  extends Tier
{
  //  protected Log _log=LogFactory.getLog(JNDITier.class);

  //----------------------------------------
  // JNDITier
  //----------------------------------------

  protected Object alloc(){return new HashMap();}
  public Object registerData(String uid, Object data) {synchronized (_tier) {return ((Map)_tier).put(uid, data);}}
  public Object deregisterData(String uid) {synchronized (_tier){return ((Map)_tier).remove(uid);}}

  //----------------------------------------
  // Tier
  //----------------------------------------

  public String getTierName(){return "jndi";}

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------
 /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=Tier.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(JNDITier.class);
    return mbeanInfo;
  }
  */
}
