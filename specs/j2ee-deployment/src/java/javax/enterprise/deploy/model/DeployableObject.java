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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * The DeployableObject interface is an abstract representation of a J2EE deployable
 * module (JAR, WAR, RAR, EAR).  A DeployableObject provides access to the module's
 * deployment descriptor and class files.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:50 $
 */
public interface DeployableObject {
    /**
     * Return the ModuleType of deployment descriptor (i.e., EAR, JAR, WAR, RAR)
     * this deployable object represents. Values are found in DeploymentManager.
     *
     * @return The ModuleType of deployable object
     */
    public ModuleType getType();

    /**
     * Return the top level standard bean representing the root of the deployment descriptor.
     *
     * @return A standard bean representing the deployment descriptor.
     */
    public DDBeanRoot getDDBeanRoot();

    /**
     * Return an array of standard beans representing the XML content returned based upon the XPath.
     *
     * @param xpath AAn XPath string identifying the data to be extracted from the deployment descriptor.
     *
     * @return an array of DDBeans or <code>null</code> if no matching data is found.
     */
    public DDBean[] getChildBean(String xpath);

    /**
     *
     * @param xpath An xpath string referring to a location in the deployment descriptor
     *
     * @return a list XML content or <code>null</code> if no matching data is found.
     */
    public String[] getText(String xpath);

    /**
     * Retrieve the specified class from this deployable module.
     * <p>One use: to get all finder methods from an EJB.  If the tool is attempting to package a
     * module and retrieve a class from the package, the class request may fail. The class may
     * not yet be available. The tool should respect the manifest Class-Path entries.</p>
     *
     * @param className Class to retrieve.
     *
     * @return Class representation of the class
     */
    public Class getClassFromScope(String className);

    /**
     * A convenience method to return the deployment descriptor
     * document version number of the primary deployment descriptor
     * for the module (e.g. web.xml, ejb-jar.xml, ra.xml, application.xml,
     * and  application-client.xml.)  The version number for documents
     * webservices.xml , webservicesclient.xml and the like are not returned
     * by this method.  DDBeanRoot.getDDBeanRootVersion should be used
     * instead.
     *
     * This method is being deprecated.  DDBeanRoot.getDDBeanRootVersion
     * should be used instead.
     *
     * @deprecated As of version 1.1, replace by DDBeanRoot.getDDBeanRootVersion()
     *
     * @return a string that is the version number of the XML instance document.
     *  Null is returned if no version number can be found.
     */
    public String getModuleDTDVersion();

    /**
     * Returns a DDBeanRoot object for the XML instance document named.
     * This method should be used to return DDBeanRoot objects for non deployment
     * descriptor XML instance documents such as WSDL files.
     *
     * @since 1.1
     *
     * @param filename the full path name from the root of the module of the xml
     *        instance document for which a DDBeanRoot object is to be returned.
     *
     * @return a DDBeanRoot object for the XML data.
     *
     * @throws java.io.FileNotFoundException if the named file can not be found
     * @throws javax.enterprise.deploy.model.exceptions.DDBeanCreateException
     *         if an error is encountered creating the DDBeanRoot object.
     */
    public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException;

    /**
     * Returns an enumeration of the module file entries.  All elements in the
     * enumeration are of type String.  Each String represents a file name relative
     * to the root of the module.
     *
     * @since 1.1
     *
     * @return an enumeration of the archive file entries.
     */
    public Enumeration entries();

    /**
     * Returns the InputStream for the given entry name.
     * The file name must be relative to the root of the module.
     *
     * @since 1.1
     *
     * @param name the file name relative to the root of the module.
     *
     * @return the InputStream for the given entry name or null if not found.
     */
    public InputStream getEntry(String name);
}