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
package org.apache.geronimo.deployment.service;

import javax.management.ObjectName;

/**
 * This class contains metadata necessary to enroll an MBean in a relationship.
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/18 22:10:12 $
 */
public class MBeanRelationship {
    /**
     * The name of the relationship (A.K.A relationshipID)
     */
    private final String name;

    /**
     * The type of the relationship.  This is used if a new relationship instance is created during deployment.
     */
    private final String type;

    /**
     * The name of the role to enroll the MBean
     */
    private final String role;

    /**
     * The name of the target MBean to which this MBean will be related.  This is only used if a new relationship
     * is created during deployment.
     */
    private final ObjectName target;

    /**
     * The name of the role to assign the target MBean.  This is only used if a new relationship is created during
     * deployment.  This is only neccessary if the relationship has more then two roles.
     */
    private String targetRole;

    /**
     * Did this MBean create the relationship instance.  If it did, the MBean will attempt to remove the
     * relationship when stopped
     */
    private boolean createdRelationship;

    /**
     * Creates a new MBeanRelationship, which is used during deployment to enroll the new MBean in a relationship.
     *
     * @param name name of the relationship
     * @param type type of the relationship
     * @param role role to which this MBean will be added
     * @param target the object name of another MBean to add to relationship if a new relationship is created
     * @param targetRole the role to assign the target MBean
     */
    public MBeanRelationship(String name, String type, String role, ObjectName target, String targetRole) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.target = target;
        this.targetRole = targetRole;
    }

    /**
     * The name of the relationship (A.K.A relationshipID)
     * @return the name of the relationship
     */
    public String getName() {
        return name;
    }

    /**
     * The type of the relationship.  This is used if a new relationship instance is created during deployment.
     * @return the relationship type
     */
    public String getType() {
        return type;
    }

    /**
     * The name of the role to enroll the MBean.
     * @return name of the role to which this MBean will be assigned
     */
    public String getRole() {
        return role;
    }

    /**
     * The name of the target MBean to which this MBean will be related.  This is only used if a new relationship
     * is created during deployment.
     * @return object name of the target MBean
     */
    public ObjectName getTarget() {
        return target;
    }

    /**
     * The name of the role to assign the target MBean.  This is only used if a new relationship is created during
     * deployment.  This is only neccessary if the relationship has more then two roles.
     * @return name of the role to which the target MBean will be assigned
     */
    public String getTargetRole() {
        return targetRole;
    }

    /**
     * Sets the name of the role that was assigned to the target MBean.
     * @param targetRole the name of the role that was assigned to the target MBean
     */
    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    /**
     * Did this MBean create the relationship instance.  If it did, the MBean will attempt to remove the
     * relationship when stopped.
     * @return true if this MBean created the relationship
     */
    public boolean getCreatedRelationship() {
        return createdRelationship;
    }

    /**
     * Sets the created relationship flag.  If this MBean created the relationship, it will attempt to
     * remove the relationship when stopped.
     * @param createdRelationship the new created relationship flag
     */
    public void setCreatedRelationship(boolean createdRelationship) {
        this.createdRelationship = createdRelationship;
    }
}
