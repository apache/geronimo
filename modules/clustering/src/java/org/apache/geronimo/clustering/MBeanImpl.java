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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A base class containing fnality useful to the MBeans of the
 * Clustering module.
 *
 * @version $Rev$ $Date$
 */
public abstract class
  MBeanImpl
  //implements GeronimoMBeanTarget
{
  protected Log         _log=LogFactory.getLog(MBeanImpl.class);
  protected ObjectName  _objectName;
  protected MBeanServer _server;

  /**
   * Return a local reference to this Object. For tight coupling via
   * JMX (bad idea?).
   *
   * @return a <code>Object</code> value
   */
  public Object getReference(){return this;}

  //----------------------------------------
  // GeronimoMBeanTarget
  //----------------------------------------

  public boolean canStart(){return true;}
  public boolean canStop(){return true;}

  public void doStart(){}
  public void doStop(){}
  public void doFail(){}
  /*
  public void
    setMBeanContext(GeronimoMBeanContext context)
  {
    _objectName=(context==null)?null:context.getObjectName();
    _server    =(context==null)?null:context.getServer();
  }
  */
  public ObjectName getObjectName() {return _objectName;}
  /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=new GeronimoMBeanInfo();
    // set target class in concrete subclasses...
    mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Reference", true, false, "a local reference to this Object"));
    return mbeanInfo;
  }
  */
}
