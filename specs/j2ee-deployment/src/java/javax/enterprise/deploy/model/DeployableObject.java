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
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
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