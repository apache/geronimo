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
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment.service;

import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;

/**
 * This class contains the information necessary to setup a class space for a deployment.
 *
 * @version $Revision: 1.3 $ $Date: 2003/10/27 21:29:46 $
 */
public class ClassSpaceMetadata {
    public static final int CREATE_IF_NECESSARY = 0;
    public static final int CREATE_ALWYAS = 1;
    public static final int CREATE_NEVER = 2;

    private ObjectName name;
    private ObjectName deploymentName;
    private GeronimoMBeanInfo geronimoMBeanInfo;
    private int create;
    private final List urls = new ArrayList();
    private ObjectName parent;

    /**
     * Gets the object name of the class space for the deployment.
     * @return the object name of the class space
     */
    public ObjectName getName() {
        return name;
    }

    /**
     * Sets the object name of the class space for the deployment.
     * @param name the new class space name
     */
    public void setName(ObjectName name) {
        this.name = name;
    }

    /**
     * Gets the object name of the deployment.
     * @return the object name of the deployment
     */
    public ObjectName getDeploymentName() {
        return deploymentName;
    }

    /**
     * Sets the object name of the deployment
     * @param deploymentName the new deployment name
     */
    public void setDeploymentName(ObjectName deploymentName) {
        this.deploymentName = deploymentName;
    }

    /**
     * Gets the geronimo mbean info for the class space.  This is only used when
     * constructing a new class space.
     * @return the geronimo mbean info for the class spacce
     */
    public GeronimoMBeanInfo getGeronimoMBeanInfo() {
        return geronimoMBeanInfo;
    }

    /**
     * Sets the geronimo mbean info for the class space.  This is only used when
     * constructing a new class space.
     * @param geronimoMBeanInfo the geronimo mbean info for the class spacce
     */
    public void setGeronimoMBeanInfo(GeronimoMBeanInfo geronimoMBeanInfo) {
        this.geronimoMBeanInfo = geronimoMBeanInfo;
    }

    /**
     * Gets the create flag for this class space.  The create flag controls the
     * creation of the class space.
     * @return the create flag
     */
    public int getCreate() {
        return create;
    }

    /**
     * Sets the create flag for this class space.  The create flag controls the
     * creation of the class space.
     * @param create the create flag
     */
    public void setCreate(int create) {
        this.create = create;
    }

    /**
     * Gets the urls to add to the class space for the deployment.
     * @return the urls to add to the class space
     */
    public List getUrls() {
        return urls;
    }

    /**
     * Gets the name of parent of this class space.
     * @return the name of the parent class space
     */
    public ObjectName getParent() {
        return parent;
    }

    /**
     * Sets the name of parent of this class space.
     * @param parent the name of the parent
     */
    public void setParent(ObjectName parent) {
        this.parent = parent;
    }
}
