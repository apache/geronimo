/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
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
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
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