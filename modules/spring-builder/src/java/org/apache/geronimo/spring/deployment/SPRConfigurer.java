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

package org.apache.geronimo.spring.deployment;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DeploymentConfiguration;

import org.apache.geronimo.deployment.ModuleConfigurer;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 *
 *
 * @version $Rev: 126313 $ $Date: 2005-01-24 21:03:52 +0000 (Mon, 24 Jan 2005) $
 */
public class SPRConfigurer
  implements ModuleConfigurer
{
  //----------------------------------------
  // RTTI

  public static final GBeanInfo GBEAN_INFO;

  static
  {
    GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SPRConfigurer.class, NameFactory.DEPLOYMENT_CONFIGURER);
    infoFactory.addInterface(ModuleConfigurer.class);
    GBEAN_INFO = infoFactory.getBeanInfo();
  }

  public static GBeanInfo getGBeanInfo() {return GBEAN_INFO;}

  //----------------------------------------

  public DeploymentConfiguration
    createConfiguration(DeployableObject deployable)
  {
    return new SPRConfiguration(deployable);
  }
}
