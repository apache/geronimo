/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.validator.ejb;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.validator.ValidationContext;
import org.apache.geronimo.validator.ValidationResult;
import org.apache.geronimo.validator.ValidationTest;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.xmlbeans.SchemaType;

/**
 * Barely worth having, but this implements one method to return the name of
 * the deployment descriptor that its subclasses apply to.  Also tracks the
 * current EJB-JAR JavaBean tree in case someone wants to navigate it.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseEjbJarTest extends ValidationTest {
    protected EjbJarType ejbJar;

    public SchemaType getSchemaType() {
        return EjbJarDocument.type;
    }

    public ValidationResult initialize(ValidationContext context) {
        if(context.type != ModuleType.EJB) {
            return ValidationResult.FAILED;
        } else {
            ejbJar = ((org.apache.geronimo.xbeans.j2ee.EjbJarDocument) context.getCurrentStandardDD()).getEjbJar();
            return ValidationResult.PASSED;
        }
    }
}
