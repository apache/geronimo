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
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
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