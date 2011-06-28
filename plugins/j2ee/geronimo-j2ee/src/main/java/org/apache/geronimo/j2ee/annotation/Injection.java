/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.j2ee.annotation;

import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class Injection implements Serializable {

    private final String targetClassName;
    private final String targetName;
    private final String jndiName;
    private final ReferenceType type;


    public Injection(String targetClassName, String targetName, String jndiName, ReferenceType type) {
        this.targetClassName = targetClassName;
        this.targetName = targetName;
        this.jndiName = jndiName;
        this.type = type;
    }


    public String getTargetClassName() {
        return targetClassName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public ReferenceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Injection [targetClassName=" + targetClassName + ", targetName=" + targetName + ", jndiName=" + jndiName + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
        result = prime * result + ((targetClassName == null) ? 0 : targetClassName.hashCode());
        result = prime * result + ((targetName == null) ? 0 : targetName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Injection other = (Injection) obj;
        if (jndiName == null) {
            if (other.jndiName != null)
                return false;
        } else if (!jndiName.equals(other.jndiName))
            return false;
        if (targetClassName == null) {
            if (other.targetClassName != null)
                return false;
        } else if (!targetClassName.equals(other.targetClassName))
            return false;
        if (targetName == null) {
            if (other.targetName != null)
                return false;
        } else if (!targetName.equals(other.targetName))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
