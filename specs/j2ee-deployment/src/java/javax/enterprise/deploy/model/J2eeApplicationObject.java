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

package javax.enterprise.deploy.model;

import javax.enterprise.deploy.shared.ModuleType;

/**
 * J2eeApplicationObject is an interface that represents a J2EE application (EAR);
 * it maintains a DeployableObject for each module in the archive.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:50 $
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