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
package org.apache.geronimo.jmx;

import javax.management.relation.RelationServiceMBean;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.RoleInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.AbstractStateManageable;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/16 23:16:55 $
 */
public class Relationship extends AbstractStateManageable implements RelationshipMBean {
    private final Log log = LogFactory.getLog(getClass());

    private String name;

    // left role
    private String leftRoleName = "left";
    private String leftRoleClass = "java.lang.Object";
    private boolean leftRoleReadable = true;
    private boolean leftRoleWritable = true;
    private int leftRoleMinimum = 0;
    private int leftRoleMaximum = RoleInfo.ROLE_CARDINALITY_INFINITY;
    private String leftRoleDescription;

    // right role
    private String rightRoleName = "right";
    private String rightRoleClass = "java.lang.object";
    private boolean rightRoleReadable = true;
    private boolean rightRoleWritable = true;
    private int rightRoleMinimum = 0;
    private int rightRoleMaximum = RoleInfo.ROLE_CARDINALITY_INFINITY;
    private String rightRoleDescription;

    public void preDeregister() throws Exception {
        RelationServiceMBean relationService = JMXUtil.getRelationService(server);
        try {
            relationService.removeRelationType(name);
        } catch (IllegalArgumentException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        } catch (RelationServiceNotRegisteredException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        } catch (RelationTypeNotFoundException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        }
        super.preDeregister();
    }

    public void doStart() throws Exception {
        RelationServiceMBean relationService = JMXUtil.getRelationService(server);
        RoleInfo[] roleInfo = {
            new RoleInfo(leftRoleName, leftRoleClass, leftRoleReadable, leftRoleWritable, leftRoleMinimum, leftRoleMaximum, leftRoleDescription),
            new RoleInfo(rightRoleName, rightRoleClass, rightRoleReadable, rightRoleWritable, rightRoleMinimum, rightRoleMaximum, rightRoleDescription),
            new RoleInfo("dummy", "java.lang.Object", true, true, 0, 0, "dummy role to test three way relationship code"),
        };
        relationService.createRelationType(name, roleInfo);
    }

    public void doStop() throws Exception {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeftRoleName() {
        return leftRoleName;
    }

    public void setLeftRoleName(String leftRoleName) {
        this.leftRoleName = leftRoleName;
    }

    public String getLeftRoleClass() {
        return leftRoleClass;
    }

    public void setLeftRoleClass(String leftRoleClass) {
        this.leftRoleClass = leftRoleClass;
    }

    public boolean isLeftRoleReadable() {
        return leftRoleReadable;
    }

    public void setLeftRoleReadable(boolean leftRoleReadable) {
        this.leftRoleReadable = leftRoleReadable;
    }

    public boolean isLeftRoleWritable() {
        return leftRoleWritable;
    }

    public void setLeftRoleWritable(boolean leftRoleWritable) {
        this.leftRoleWritable = leftRoleWritable;
    }

    public int getLeftRoleMinimum() {
        return leftRoleMinimum;
    }

    public void setLeftRoleMinimum(int leftRoleMinimum) {
        this.leftRoleMinimum = leftRoleMinimum;
    }

    public int getLeftRoleMaximum() {
        return leftRoleMaximum;
    }

    public void setLeftRoleMaximum(int leftRoleMaximum) {
        this.leftRoleMaximum = leftRoleMaximum;
    }

    public String getLeftRoleDescription() {
        return leftRoleDescription;
    }

    public void setLeftRoleDescription(String leftRoleDescription) {
        this.leftRoleDescription = leftRoleDescription;
    }

    public String getRightRoleName() {
        return rightRoleName;
    }

    public void setRightRoleName(String rightRoleName) {
        this.rightRoleName = rightRoleName;
    }

    public String getRightRoleClass() {
        return rightRoleClass;
    }

    public void setRightRoleClass(String rightRoleClass) {
        this.rightRoleClass = rightRoleClass;
    }

    public boolean isRightRoleReadable() {
        return rightRoleReadable;
    }

    public void setRightRoleReadable(boolean rightRoleReadable) {
        this.rightRoleReadable = rightRoleReadable;
    }

    public boolean isRightRoleWritable() {
        return rightRoleWritable;
    }

    public void setRightRoleWritable(boolean rightRoleWritable) {
        this.rightRoleWritable = rightRoleWritable;
    }

    public int getRightRoleMinimum() {
        return rightRoleMinimum;
    }

    public void setRightRoleMinimum(int rightRoleMinimum) {
        this.rightRoleMinimum = rightRoleMinimum;
    }

    public int getRightRoleMaximum() {
        return rightRoleMaximum;
    }

    public void setRightRoleMaximum(int rightRoleMaximum) {
        this.rightRoleMaximum = rightRoleMaximum;
    }

    public String getRightRoleDescription() {
        return rightRoleDescription;
    }

    public void setRightRoleDescription(String rightRoleDescription) {
        this.rightRoleDescription = rightRoleDescription;
    }
}
