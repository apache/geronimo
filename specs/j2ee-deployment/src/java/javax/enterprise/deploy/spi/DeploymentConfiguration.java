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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import java.io.OutputStream;
import java.io.InputStream;

/**
 * An interface that defines a container for all the server-specific configuration
 * information for a single top-level J2EE module.  The DeploymentConfiguration
 * object could represent a single stand alone module or an EAR file that contains
 * several sub-modules.
 *
 * @version $Rev$ $Date$
 */
public interface DeploymentConfiguration {
    /**
     * Returns an object that provides access to the deployment descriptor data
     * and classes of a J2EE module.
     *
     * @return A DeployableObject
     */
    public DeployableObject getDeployableObject();

    /**
     * Returns the top level configuration bean, DConfigBeanRoot, associated with
     * the deployment descriptor represented by the designated DDBeanRoot bean.
     *
     * @param bean The top level bean that represents the associated deployment descriptor.
     *
     * @return the DConfigBeanRoot for editing the server-specific properties required by the module.
     *
     * @throws ConfigurationException reports errors in generating a configuration bean
     */
    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot bean) throws ConfigurationException;

    /**
     * Remove the root DConfigBean and all its children.
     *
     * @param bean the top leve DConfigBean to remove.
     *
     * @throws BeanNotFoundException the bean provided is not in this beans child list.
     */
    public void removeDConfigBean(DConfigBeanRoot bean) throws BeanNotFoundException;

    /**
     * Restore from disk to instantated objects all the DConfigBeans associated with a
     * specific deployment descriptor. The beans may be fully or partially configured.
     *
     * @param inputArchive The input stream for the file from which the DConfigBeans
     *                     should be restored.
     * @param bean         The DDBeanRoot bean associated with the deployment descriptor file.
     *
     * @return The top most parent configuration bean, DConfigBeanRoot
     *
     * @throws ConfigurationException reports errors in generating a configuration bean
     */
    public DConfigBeanRoot restoreDConfigBean(InputStream inputArchive, DDBeanRoot bean) throws ConfigurationException;

    /**
     * Save to disk all the configuration beans associated with a particular deployment
     * descriptor file. The saved data may be fully or partially configured DConfigBeans.
     * The output file format is recommended to be XML.
     *
     * @param outputArchive The output stream to which the DConfigBeans should be saved.
     * @param bean          The top level bean, DConfigBeanRoot, from which to be save.
     *
     * @throws ConfigurationException reports errors in storing a configuration bean
     */
    public void saveDConfigBean(OutputStream outputArchive, DConfigBeanRoot bean) throws ConfigurationException;

    /**
     * Restore from disk to a full set of configuration beans previously stored.
     *
     * @param inputArchive The input stream from which to restore the Configuration.
     *
     * @throws ConfigurationException reports errors in generating a configuration bean
     */
    public void restore(InputStream inputArchive) throws ConfigurationException;

    /**
     * Save to disk the current set configuration beans created for this deployable
     * module.  It is recommended the file format be XML.
     *
     * @param outputArchive The output stream to which to save the Configuration.
     *
     * @throws ConfigurationException reports errors in storing a configuration bean
     */
    public void save(OutputStream outputArchive) throws ConfigurationException;
}