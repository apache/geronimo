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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi;

import javax.enterprise.deploy.model.DDBeanRoot;

/**
 * The DConfigBeanRoot interface represent the root of a deployment descriptor.
 * A DConfigBeanRoot is associated with a DDRoot object which in turn is associated
 * with a specific deployment descriptor file.
 *
 * <p>Only DConfigBeanRoots are saved or restored by methods in
 * DeploymentConfiguration.</p>
 *
 * @see DeploymentConfiguration
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:34 $
 */
public interface DConfigBeanRoot extends DConfigBean {
    /**
     * Return a DConfigBean for a deployment descriptor that is not the module's
     * primary deployment descriptor.   Web services provides a deployment descriptor
     * in addition to the module's primary deployment descriptor.  Only the DDBeanRoot
     * for this category of secondary deployment descriptors are to be passed as arguments
     * through this method.
     *
     * Web service has two deployment descriptor files, one that defines the web service
     * and one that defines a client of a web service.  See the Web Service specification for
     * the details.
     *
     * @since 1.1
     *
     * @param ddBeanRoot represents the root element of a deployment descriptor file.
     *
     * @return a DConfigBean to be used for processing this deployment descriptor data. Null may be returned
     *         if no DConfigBean is required for this deployment descriptor.
     */
    public DConfigBean getDConfigBean(DDBeanRoot ddBeanRoot);
}
