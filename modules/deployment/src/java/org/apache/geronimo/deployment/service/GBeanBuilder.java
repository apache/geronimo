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
 * @version $Revision: 1.2 $ $Date: 2004/02/24 18:16:10 $
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
