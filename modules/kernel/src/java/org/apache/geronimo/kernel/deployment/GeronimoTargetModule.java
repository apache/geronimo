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
package org.apache.geronimo.kernel.deployment;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;

/**
 * The Geronimo implementation of TargetModuleID.  In addition to the basic
 * properties required by the interface, this implementation tracks the
 * deployment job ID for when this module was originally distributed, as
 * well as the ObjectName of the MBean for the specified Target where this
 * module can be found.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/17 10:57:40 $
 */
public class GeronimoTargetModule implements TargetModuleID, Serializable {
    private Target target;
    private String moduleID;
    private int deploymentID;
    private String webURL;
    private TargetModuleID parent;
    private List children = new ArrayList();
    private String mbeanObjectName;

    public GeronimoTargetModule(Target target, String moduleID) {
        this.target = target;
        this.moduleID = moduleID;
        this.deploymentID = deploymentID;
    }

    public GeronimoTargetModule(Target target, String moduleID, String webURL) {
        this.target = target;
        this.moduleID = moduleID;
        this.deploymentID = deploymentID;
        this.webURL = webURL;
    }

    public GeronimoTargetModule(Target target, String moduleID, GeronimoTargetModule parent) {
        this.target = target;
        this.moduleID = moduleID;
        this.deploymentID = deploymentID;
        this.parent = parent;
        parent.children.add(this);
    }

    public GeronimoTargetModule(Target target, String moduleID, String webURL, GeronimoTargetModule parent) {
        this.target = target;
        this.moduleID = moduleID;
        this.deploymentID = deploymentID;
        this.webURL = webURL;
        this.parent = parent;
        parent.children.add(this);
    }

    /**
     * Retrieve the target server that this module was deployed to.
     *
     * @return an object representing a server target.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Retrieve the id assigned to represent the deployed module.
     */
    public String getModuleID() {
        return moduleID+"."+deploymentID;
    }

    /**
     * If this TargetModulID represents a web module retrieve the URL for it.
     *
     * @return the URL of a web module or null if the module is not a web module.
     */
    public String getWebURL() {
        return webURL;
    }

    /**
     * Retrieve the identifier of the parent object of this deployed module.  If
     * there is no parent then this is the root object deployed.  The root could
     * represent an EAR file or it could be a stand alone module that was deployed.
     *
     * @return the TargetModuleID of the parent of this object. A <code>null</code>
     *         value means this module is the root object deployed.
     */
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }

    /**
     * Retrieve a list of identifiers of the children of this deployed module.
     *
     * @return a list of TargetModuleIDs identifying the childern of this object.
     *         A <code>null</code> value means this module has no children
     */
    public TargetModuleID[] getChildTargetModuleID() {
        return (TargetModuleID[])children.toArray(new TargetModuleID[children.size()]);
    }

    public int getDeploymentID() {
        return deploymentID;
    }

    public void setDeploymentID(int deploymentID) {
        this.deploymentID = deploymentID;
    }

    public String getMbeanObjectName() {
        return mbeanObjectName;
    }

    public void setMbeanObjectName(String mbeanObjectName) {
        this.mbeanObjectName = mbeanObjectName;
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof GeronimoTargetModule)) return false;

        final GeronimoTargetModule geronimoTargetModule = (GeronimoTargetModule)o;

        if(deploymentID != geronimoTargetModule.deploymentID) return false;
        if(!moduleID.equals(geronimoTargetModule.moduleID)) return false;
        if(parent != null ? !parent.equals(geronimoTargetModule.parent) : geronimoTargetModule.parent != null) return false;
        if(!target.equals(geronimoTargetModule.target)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = target.hashCode();
        result = 29 * result + moduleID.hashCode();
        result = 29 * result + deploymentID;
        return result;
    }
}
