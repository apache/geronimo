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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import java.beans.PropertyChangeListener;

/**
 * The interface for configuring a server-specific deployment descriptor, or subset of same.
 * A DConfigBean corresponds to a specific location in a standard deployment descriptor,
 * typically where values (such as names and roles) are used.
 *
 * <p>There are three different ways that DConfigBeans are created:</p>
 *
 * <ul>
 *   <li><code>DeploymentConfigurator.getDConfigBean(DDBeanRoot)</code> is called by the
 *       deployment tool to create a DConfigBeanRoot for each deployment descriptor in
 *       the J2EE application.</li>
 *   <li><code>DConfigBean.getDConfigBean(DDBean)</code> is called by the deployment
 *       tool for each DDBean that corresponds to a relative XPath pattern given to the
 *       deployment tool by the method <code>DConfigBean.getXpaths()</code>.</li>
 *   <li>Each DConfigBean can structure its configurations as a tree-structure of
 *       DConfigBeans; a DConfigBean can have properties of type DConfigBean or
 *       DConfigBean[].</li>
 * <ul>
 *
 * <p>The properties of DConfigBeans are displayed and edited by the deployment tool by
 * using the JavaBean Property classes.</p>
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:34 $
 */
public interface DConfigBean {
    /**
     * Return the JavaBean containing the deployment descriptor XML text associated with this DConfigBean.
     *
     * @return The bean class containing the XML text for this DConfigBean.
     */
    public DDBean getDDBean();

    /**
     * Return a list of XPaths designating the deployment descriptor information this
     * DConfigBean requires.  Each server vendor may need to specify different
     * server-specific information.  Each String returned by this method is an XPath
     * describing a certain portion of the standard deployment descriptor for which
     * there is corresponding server-specific configuration.
     *
     * @return a list of XPath Strings representing XML data to be retrieved or
     *         <code>null</code> if there are none.
     */
    public String[] getXpaths();

    /**
     * Return the JavaBean containing the server-specific deployment configuration
     * information based upon the XML data provided by the DDBean.
     *
     * @param bean The DDBean containing the XML data to be evaluated.
     *
     * @return The DConfigBean to display the server-specific properties for the standard bean.
     *
     * @throws ConfigurationException reports errors in generating a configuration bean.
     *         This DDBean is considered undeployable to this server until this exception is
     *         resolved.  A suitably descriptive message is required so the user can diagnose
     *         the error.
     */
    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException;

    /**
     * Remove a child DConfigBean from this bean.
     *
     * @param bean The child DConfigBean to be removed.
     *
     * @throws BeanNotFoundException the bean provided is not in the child list of this bean.
     */
    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException;

    /**
     * A notification that the DDBean provided in the event has changed and this bean
     * or its child beans need to reevaluate themselves.
     *
     * <p><i>It is advisable, though not declared explicitly in the specification, for a
     * DConfigBean to receive change events for itself, and add or remove events for
     * its direct children.  The DConfigBean implementation should not add or remove
     * beans here if it will add or remove those beans again in response to a call to
     * getDConfigBean or removeDConfigBean.</i></p>
     *
     * @see #getDConfigBean
     * @see #removeDConfigBean
     *
     * @param event an event containing a reference to the DDBean which has changed.
     */
    public void notifyDDChange(XpathEvent event);

    /**
     * Register a property listener for this bean.
     *
     * @param pcl PropertyChangeListener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Unregister a property listener for this bean.
     *
     * @param pcl Listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);
}