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

package org.apache.geronimo.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;

/**
 * A peer GBean for mediating between Geronimo Kernel and Spring managed POJO...
 *
 * @version $Rev: 149468 $
 */
public class POJOGBean
  implements GBeanLifecycle
{
  protected static final Log _log = LogFactory.getLog(POJOGBean.class);

  // injected into ctor
  protected final Kernel _kernel;
  protected final String _objectName;
  protected final Object _peer;

  //----------------------------------------
  public static final GBeanInfo GBEAN_INFO;

  static
  {
    GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Spring Managed POJO", POJOGBean.class);

    infoBuilder.addAttribute("kernel"     , Kernel.class , false);
    infoBuilder.addAttribute("objectName" , String.class , false);
    infoBuilder.addAttribute("peer"       , Object.class , true);

    infoBuilder.setConstructor(new String[]{
				 "kernel",
				 "objectName",
				 "peer"
			       });

    GBEAN_INFO = infoBuilder.getBeanInfo();
  }

  public static GBeanInfo getGBeanInfo() {return GBEAN_INFO;}

  //----------------------------------------

  public
    POJOGBean(Kernel kernel, String objectName, Object peer)
  {
    _kernel    =kernel;
    _objectName=objectName;
    _peer      =peer;
  }

  //----------------------------------------
  // GBeanLifecycle
  //----------------------------------------

  public void doStart() throws Exception {}
  public void doStop() throws Exception {}
  public void doFail() {}
}
