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

package org.apache.geronimo.validator;

import java.io.PrintWriter;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.xmlbeans.XmlObject;

/**
 * Holds all the context information for the current validation process.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:29 $
 */
public class ValidationContext {
    public final PrintWriter out;
    public final String moduleName;
    public final ClassLoader loader;
    public final ModuleType type;
    public final XmlObject[] standardDD;
    public final Object[] serverDD;
    private Object currentStandardDD;
    private Object currentNode;

    public ValidationContext(ClassLoader loader, String moduleName, PrintWriter out, Object[] serverDD, XmlObject[] standardDD, ModuleType type) {
        this.loader = loader;
        this.moduleName = moduleName;
        this.out = out;
        this.serverDD = serverDD;
        this.standardDD = standardDD;
        this.type = type;
    }

    /**
     * At the moment, this is the standard DD we're validating.
     */
    public Object getCurrentStandardDD() {
        return currentStandardDD;
    }

    /**
     * At the moment, this is the standard DD we're validating.
     */
    void setCurrentStandardDD(Object currentStandardDD) {
        this.currentStandardDD = currentStandardDD;
    }

    /**
     * At the moment, this is the node on the standard DD that we're
     * validating.  It corresponds to the XPath that a particular
     * test is interested in.
     */
    public Object getCurrentNode() {
        return currentNode;
    }

    /**
     * At the moment, this is the node on the standard DD that we're
     * validating.  It corresponds to the XPath that a particular
     * test is interested in.
     */
    void setCurrentNode(Object currentNode) {
        this.currentNode = currentNode;
    }
}
