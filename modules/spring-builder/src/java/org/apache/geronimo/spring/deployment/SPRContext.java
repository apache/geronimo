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
package org.apache.geronimo.spring.deployment;

import java.io.File;
import java.net.URI;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Rev: 125989 $ $Date: 2005-01-22 00:26:15 +0000 (Sat, 22 Jan 2005) $
 */
public class SPRContext
  extends DeploymentContext
{
  public
    SPRContext(File baseDir, URI id, ConfigurationModuleType moduleType, URI[] parentID, Kernel kernel)
    throws MalformedObjectNameException, DeploymentException
  {
    super(baseDir, id, moduleType, parentID, kernel);
  }
}
