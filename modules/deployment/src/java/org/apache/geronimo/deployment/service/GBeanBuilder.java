/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:38 $
 */
public class GBeanBuilder {
    private final ObjectName name;
    private final GBeanMBean gbean;

    public GBeanBuilder(String name, ClassLoader classLoader, String className) throws DeploymentException {
        try {
            this.name = new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Invalid ObjectName: " + name, e);
        }
        try {
            this.gbean = new GBeanMBean(className, classLoader);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create GBean from class " + className, e);
        }
    }

    public void setAttribute(String name, String type, String text) throws DeploymentException {
        try {
            // @todo we should not need all of common just for this
            PropertyEditor editor = PropertyEditors.findEditor(type);
            if (editor == null) {
                throw new DeploymentException("Unable to find PropertyEditor for " + type);
            }
            editor.setAsText(text);
            Object value = editor.getValue();
            gbean.setAttribute(name, value);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to find PropertyEditor for " + type, e);
        } catch (AttributeNotFoundException e) {
            throw new DeploymentException("Unknown attribute " + name);
        } catch (InvalidAttributeValueException e) {
            throw new DeploymentException("Invalid value for attribute " + name + ": " + text, e);
        } catch (Exception e) {
            throw new DeploymentException("Unable to set attribute " + name + " to " + text, e);
        }
    }

    public void setReference(String name, String pattern) throws DeploymentException {
        setReference( name, new String[] { pattern } );
    }

    public void setReference(String name, String[] patterns) throws DeploymentException {
        Set patternNames = new HashSet(patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            try {
                patternNames.add(new ObjectName(patterns[i]));
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid pattern for reference " + name + ": " + patterns[i], e);
            }
        }
        gbean.setReferencePatterns(name, patternNames);
    }

    public GBeanMBean getGBean() {
        return gbean;
    }

    public ObjectName getName() {
        return name;
    }
}
