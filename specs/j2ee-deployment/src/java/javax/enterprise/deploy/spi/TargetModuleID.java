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

/**
 * A TargetModuleID interface represents a unique identifier for a deployed
 * application module.  A deployable application module can be an EAR, JAR, WAR or
 * RAR file.  A TargetModuleID can represent a root module or a child module.  A
 * root module TargetModuleID has no parent.  It represents a deployed EAR file or
 * standalone module.  A child module TargetModuleID represents a deployed sub
 * module of a J2EE application.  A child TargetModuleID has only one parent, the
 * super module it was bundled and deployed with.  The identifier consists of the
 * target name and the unique identifier for the deployed application module.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
 */
public interface TargetModuleID {
    /**
     * Retrieve the target server that this module was deployed to.
     *
     * @return an object representing a server target.
     */
    public Target getTarget();

    /**
     * Retrieve the id assigned to represent the deployed module.
     */
    public String getModuleID();

    /**
     * If this TargetModulID represents a web module retrieve the URL for it.
     *
     * @return the URL of a web module or null if the module is not a web module.
     */
    public String getWebURL();

    /**
     * Retrieve the identifier representing the deployed module.
     */
    public String toString();

    /**
     * Retrieve the identifier of the parent object of this deployed module.  If
     * there is no parent then this is the root object deployed.  The root could
     * represent an EAR file or it could be a stand alone module that was deployed.
     *
     * @return the TargetModuleID of the parent of this object. A <code>null</code>
     *         value means this module is the root object deployed.
     */
    public TargetModuleID getParentTargetModuleID();

    /**
     * Retrieve a list of identifiers of the children of this deployed module.
     *
     * @return a list of TargetModuleIDs identifying the childern of this object.
     *         A <code>null</code> value means this module has no children
     */
    public TargetModuleID[] getChildTargetModuleID();
}