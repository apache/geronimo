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
 * An interface that represents the root of a standard deployment descriptor.
 * A DDBeanRoot is a type of DDBean.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
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