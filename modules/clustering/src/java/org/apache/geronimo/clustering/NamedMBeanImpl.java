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

package org.apache.geronimo.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A base class containing fnality useful to Named MBeans of the
 * Clustering module.
 *
 * @version $Rev$ $Date$
 */
public abstract class
  NamedMBeanImpl
  extends MBeanImpl
{
  protected Log _log=LogFactory.getLog(NamedMBeanImpl.class);

  /**
   * Returns the MBean's name.
   *
   * @return a <code>String</code> value
   */
  public String getName() {return _objectName.getKeyProperty("name");}

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean
    canStart()
  {
    if (!super.canStart()) return false;

    if (_objectName.getKeyProperty("name")==null)
    {
      _log.warn("MBean name must contain a 'name' property");
      return false;
    }

    return true;
  }
  /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=MBeanImpl.getGeronimoMBeanInfo();
    // set target class in concrete subclasses...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Name", true, false, "this Object's name"));
    return mbeanInfo;
  }
  */
}
