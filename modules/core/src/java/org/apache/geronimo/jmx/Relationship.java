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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.RoleInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2003/08/18 22:19:28 $
 */
public class Relationship implements MBeanRegistration, RelationshipMBean {
    private final Log log = LogFactory.getLog(getClass());
    private RelationServiceMBean relationService;

    private final String name;

    // left role
    private final String leftRoleName;
    private final String leftRoleClass;
    private final boolean leftRoleReadable;
    private final boolean leftRoleWritable;
    private final int leftRoleMinimum;
    private final int leftRoleMaximum;
    private final String leftRoleDescription;

    // right role
    private final String rightRoleName;
    private final String rightRoleClass;
    private final boolean rightRoleReadable;
    private final boolean rightRoleWritable;
    private final int rightRoleMinimum;
    private final int rightRoleMaximum;
    private final String rightRoleDescription;

    public Relationship(String propertiesString) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(propertiesString.getBytes()));
        } catch (IOException cause) {
            IllegalArgumentException e = new IllegalArgumentException("Properties string is invalid");
            e.initCause(cause);
            throw e;
        }

        name = properties.getProperty("name");

        // left role
        leftRoleName = properties.getProperty("left.name", "left");
        leftRoleClass = properties.getProperty("left.class", "java.lang.Object");
        leftRoleReadable = Boolean.valueOf(properties.getProperty("left.readable", "true")).booleanValue();
        leftRoleWritable = Boolean.valueOf(properties.getProperty("left.writable", "true")).booleanValue();
        String leftRoleMinimumString = properties.getProperty("left.minimum");
        if (leftRoleMinimumString != null) {
            leftRoleMinimum = Integer.parseInt(leftRoleMinimumString);
        } else {
            leftRoleMinimum = 0;
        }
        String leftRoleMaximumString = properties.getProperty("left.maximum");
        if (leftRoleMaximumString != null) {
            leftRoleMaximum = Integer.parseInt(leftRoleMaximumString);
        } else {
            leftRoleMaximum = RoleInfo.ROLE_CARDINALITY_INFINITY;
        }
        leftRoleDescription = properties.getProperty("left.description");

        // right role
        rightRoleName = properties.getProperty("right.name", "right");
        rightRoleClass = properties.getProperty("right.class", "java.lang.Object");
        rightRoleReadable = Boolean.valueOf(properties.getProperty("right.readable", "true")).booleanValue();
        rightRoleWritable = Boolean.valueOf(properties.getProperty("right.writable", "true")).booleanValue();
        String rightRoleMinimumString = properties.getProperty("right.minimum");
        if (rightRoleMinimumString != null) {
            rightRoleMinimum = Integer.parseInt(rightRoleMinimumString);
        } else {
            rightRoleMinimum = 0;
        }
        String rightRoleMaximumString = properties.getProperty("right.maximum");
        if (rightRoleMaximumString != null) {
            rightRoleMaximum = Integer.parseInt(rightRoleMaximumString);
        } else {
            rightRoleMaximum = RoleInfo.ROLE_CARDINALITY_INFINITY;
        }
        rightRoleDescription = properties.getProperty("right.description");
    }

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        relationService = JMXUtil.getRelationService(server);

        // register our relationship
        RoleInfo[] roleInfo = {
            new RoleInfo(leftRoleName, leftRoleClass, leftRoleReadable, leftRoleWritable, leftRoleMinimum, leftRoleMaximum, leftRoleDescription),
            new RoleInfo(rightRoleName, rightRoleClass, rightRoleReadable, rightRoleWritable, rightRoleMinimum, rightRoleMaximum, rightRoleDescription),
            new RoleInfo("dummy", "java.lang.Object", true, true, 0, 0, "dummy role to test three way relationship code"),
        };
        relationService.createRelationType(name, roleInfo);
        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
        try {
            relationService.removeRelationType(name);
        } catch (IllegalArgumentException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        } catch (RelationServiceNotRegisteredException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        } catch (RelationTypeNotFoundException e) {
            log.warn("Could not remove relation type: name=" + name, e);
        }
    }

    public void postDeregister() {
    }

    public List getRegisteredRelationships() {
        try {
            return relationService.findRelationsOfType(name);
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }

    public String getName() {
        return name;
    }

    public String getLeftRoleName() {
        return leftRoleName;
    }

    public String getLeftRoleClass() {
        return leftRoleClass;
    }

    public boolean isLeftRoleReadable() {
        return leftRoleReadable;
    }

    public boolean isLeftRoleWritable() {
        return leftRoleWritable;
    }

    public int getLeftRoleMinimum() {
        return leftRoleMinimum;
    }

    public int getLeftRoleMaximum() {
        return leftRoleMaximum;
    }

    public String getLeftRoleDescription() {
        return leftRoleDescription;
    }

    public String getRightRoleName() {
        return rightRoleName;
    }

    public String getRightRoleClass() {
        return rightRoleClass;
    }

    public boolean isRightRoleReadable() {
        return rightRoleReadable;
    }

    public boolean isRightRoleWritable() {
        return rightRoleWritable;
    }

    public int getRightRoleMinimum() {
        return rightRoleMinimum;
    }

    public int getRightRoleMaximum() {
        return rightRoleMaximum;
    }

    public String getRightRoleDescription() {
        return rightRoleDescription;
    }
}
