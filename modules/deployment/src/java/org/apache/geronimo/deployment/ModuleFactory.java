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

package org.apache.geronimo.deployment;

import java.net.URI;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.URLInfo;

/**
 * A factory for a specific module type capable of returning a module from
 * an external resource.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:35 $
 */
public interface ModuleFactory {
    /**
     * Examine the resource at the supplied URL and, if it is recognized,
     * return a DeploymentModule representing the resource.
     * @param urlInfo the URL to examine
     * @param moduleID a unique id to assign to this module
     * @return a DeploymentModule representing the resource that can be used
     *         for the remainder of the deployment process; null if this
     *         factory does not recognize the resource or does not wish to
     *         handle it
     * @throws DeploymentException if this factory was trying to return a module
     *         but was unable to construct it
     */
    DeploymentModule getModule(URLInfo urlInfo, URI moduleID) throws DeploymentException;
}
