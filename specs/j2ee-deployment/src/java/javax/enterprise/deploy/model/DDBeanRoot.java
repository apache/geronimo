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
 * An interface that represents the root of a standard deployment descriptor.
 * A DDBeanRoot is a type of DDBean.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:50 $
 */
public interface DDBeanRoot extends DDBean {
    /**
     * Return the ModuleType of deployment descriptor.
     *
     * @return The ModuleType of deployment descriptor
     */
    public ModuleType getType();

    /**
     * Return the containing DeployableObject
     *
     * @return The DeployableObject that contains this deployment descriptor
     */
    public DeployableObject getDeployableObject();

    /**
     * A convenience method to return the DTD version number. The DeployableObject has this information.
     *
     * Note: the method getDDBeanRootVersion() is preferred to this method.
     * @see #getDDBeanRootVersion
     *
     * @return a string containing the DTD version number
     */
    public String getModuleDTDVersion();

    /**
     * A convenience method to return the version number of an
     * XML instance document.  This method is replacing the
     * method DeployableObject.getModuleDTDVersion, because
     * it returns the version number of any J2EE XML instance document
     *
     * @return <p>a string that is the version number of the XML instance document.
     *  Null is returned if no version number can be found.</p>
     * <p>A module's deployment descriptor file always contains
     * a document type identifier, DOCTYPE.  The DOCTYPE statement
     * contains the module DTD version number in the label of the
     * statement.</p>
     * <p>The format of the DOCTYPE statement is:</p>
     * <pre>&lt;!DOCTYPE root_element PUBLIC "-//organization//label//language" "location"&gt;</pre>
     * <dl>
     *   <dt>root_element</dt><dd>is the name of the root document in the DTD.</dd>
     *   <dt>organization</dt><dd>is the name of the organization responsible
     * for the creation and maintenance of the DTD
     * being referenced.</dd>
     *   <dt>label</dt><dd>is a unique descriptive name for the public text being
     * referenced.  </dd>
     *   <dt>language</dt><dd>is the ISO 639 language id representing the natural
     * language encoding of th DTD.</dd>
     *   <dt>location</dt><dd>is the URL of the DTD.</dd>
     * </dl>
     * <p>An example J2EE deployment descriptor DOCTYPE statement is:</p>
     * <pre><!DOCTYPE application-client PUBLIC
     *                "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN"
     *                "http://java.sun.com/dtd/application-client_1_3.dtd"></pre>
     * <p>In this example the label is, "DTD J2EE Application Client 1.3",
     * and the DTD version number is 1.3. A call to getModuleDTDVersion
     * would return a string containing, "1.3".</p>
     */
    public String getDDBeanRootVersion();

    /**
     * Return the XPath for this standard bean. The root XPath is "/".
     * 
     * @return "/" this is the root standard bean.
     */
    public String getXpath();

    /**
     * Returns the filename relative to the root of the module of the XML instance document this
     * DDBeanRoot represents.
     *
     * @since 1.1
     *
     *  @return String the filename relative to the root of the module
     */
    public String getFilename();
}