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

import java.util.List;
import java.util.Map;
import java.net.URI;

import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * A module representing a resource being deployed. The deployer will call each
 * method once in the sequence:
 * <code>
 * try {
 *   foreach module {
 *       module.init();
 *   }
 *   foreach module {
 *       module.generateClassPath(this);
 *   }
 *   ClassLoader parent = ... ; // get classloader from parent config
 *   ClassLoader cl = new ClassLoader(classPathURLs, parent);
 *   foreach module {
 *       module.defineGBeans(this, cl);
 *   }
 * } finally {
 *   foreach module {
 *       module.complete();
 *   }
 * }
 * </code>
 *
 * Once deployment starts, complete() method must always be called even if
 * problems in the deployment process prevent the other methods being called.
 * complete() may be called without a prior call to init().
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:57:35 $
 */
public interface DeploymentModule {
    /**
     * Indication to this module that the deployment process is starting.
     */
    void init() throws DeploymentException;

    /**
     * Perform any callbacks needed to define the classpath this module needs
     * the resulting Configuration to contain. This would typically involve
     * callbacks to add files to the Configuration or to add URLs for external
     * resources
     * @param callback the callback to use to interact with the deployer
     * @throws DeploymentException if there was a problem generating the classpath
     */
    void generateClassPath(ConfigurationCallback callback) throws DeploymentException;

    /**
     * Perform callbacks needed to define GBeans in the resulting Configuration.
     * @param callback the callback to use to interact with the deployer
     * @param cl a ClassLoader created by the deployer which is guaranteed to
     *           contain all the classpath entries this module added in the prior
     *           call to generateClassPath
     * @throws DeploymentException
     */
    void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException;

    /**
     * Indication from the deployer that its use of this module is complete.
     */
    void complete();
}
