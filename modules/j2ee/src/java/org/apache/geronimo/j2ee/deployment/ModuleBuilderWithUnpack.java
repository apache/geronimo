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

package org.apache.geronimo.j2ee.deployment;

import java.io.File;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * Temporary interface. The method defined to this interface should be added to
 * ModuleBuilder. However, as long as all the ModuleBuilders do not implement 
 * it one can not do that.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/07/23 06:06:19 $
 */
public interface ModuleBuilderWithUnpack extends ModuleBuilder
{

    public void installModule(File earFolder, EARContext earContext, Module module) throws DeploymentException;

}
