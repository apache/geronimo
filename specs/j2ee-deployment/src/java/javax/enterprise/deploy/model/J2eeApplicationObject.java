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
package javax.enterprise.deploy.model;

import javax.enterprise.deploy.shared.ModuleType;

/**
 * J2eeApplicationObject is an interface that represents a J2EE application (EAR);
 * it maintains a DeployableObject for each module in the archive.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
 */
public interface J2eeApplicationObject extends DeployableObject {
    /**
     * Return the DeployableObject of the specified URI designator.
     *
     * @param uri Describes where to get the module from.
     *
     * @return the DeployableObject describing the j2ee module at this uri
     *         or <code>null</code> if there is not match.
     */
    public DeployableObject getDeployableObject(String uri);

    /**
     * Return the all DeployableObjects of the specified type.
     *
     * @param type The type of module to return.
     *
     * @return the list of DeployableObjects describing the j2ee modules of
     *         this type or <code>null</code> if there are no matches.
     */
    public DeployableObject[] getDeployableObjects(ModuleType type);

    /**
     * Return the all DeployableObjects in this application.
     *
     * @return the DeployableObject instances describing the j2ee modules in
     *         this application or <code>null</code> if there are none available.
     */
    public DeployableObject[] getDeployableObjects();

    /**
     * Return the list of URIs of the designated module type.
     *
     * @param type The type of module to return.
     *
     * @return the Uris of the contained modules or <code>null</code> if there
     *         are no matches.
     */
    public String[] getModuleUris(ModuleType type);

    /**
     * Return the list of URIs for all modules in the application.
     *
     * @return the Uris of the contained modules or <code>null</code> if
     *         the application is completely empty.
     */
    public String[] getModuleUris();

    /**
     * Return a list of DDBean instances based upon an XPath; all deployment
     * descriptors of the specified type are searched.
     *
     * @param type  The type of deployment descriptor to query.
     * @param xpath An XPath string referring to a location in the deployment descriptor
     *
     * @return The list of DDBeans or <code>null</code> if there are no matches.
     */
    public DDBean[] getChildBean(ModuleType type, String xpath);

    /**
     * Return the text value from the XPath; search only the deployment descriptors
     * of the specified type.
     *
     * @param type  The type of deployment descriptor to query.
     * @param xpath The xpath to query for.
     *
     * @return The text values of this xpath or <code>null</code> if there are no matches.
     */
    public String[] getText(ModuleType type, String xpath);

    /**
     * Register a listener for changes in XPath that are related to this deployableObject.
     *
     * @param type  The type of deployment descriptor to query.
     * @param xpath The xpath to listen for.
     * @param xpl   The listener.
     */
    public void addXpathListener(ModuleType type, String xpath, XpathListener xpl);

    /**
     * Unregister the listener for an XPath.
     * @param type  The type of deployment descriptor to query.
     * @param xpath The xpath to listen for.
     * @param xpl   The listener.
     */
    public void removeXpathListener(ModuleType type, String xpath, XpathListener xpl);
}