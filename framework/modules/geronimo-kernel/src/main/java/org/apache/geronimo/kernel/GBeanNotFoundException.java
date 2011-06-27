/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel;

import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import javax.management.ObjectName;


/**
 * @version $Rev$ $Date$
 */
public class GBeanNotFoundException extends KernelException {
    private ObjectName gBeanName;
    private AbstractName abstractName;
    private Set<AbstractName> matches;

    public GBeanNotFoundException(ObjectName gBeanName) {
        super(gBeanName+" not found");
        this.gBeanName = gBeanName;
    }

    public GBeanNotFoundException(ObjectName gBeanName, Throwable cause) {
        super(gBeanName+" not found", cause);
        this.gBeanName = gBeanName;
    }

    public GBeanNotFoundException(AbstractName abstractName) {
        super(abstractName + " not found");
        this.abstractName = abstractName;
    }

    public GBeanNotFoundException(String message, Set patterns, Set<AbstractName> matches) {
        super(message + ": " + patterns + (matches == null? " (no matches)": " matches: " + matches));
        this.matches = matches == null? null: new HashSet<AbstractName>(matches);
    }

    public GBeanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectName getGBeanName() {
        return gBeanName;
    }

    public AbstractName getAbstractName() {
        return abstractName;
    }

    public boolean isGBeanName() {
        return gBeanName != null;
    }

    public boolean isAbstractName() {
        return abstractName != null;
    }

    public Set<AbstractName> getMatches() {
        return matches;
    }

    public boolean hasMatches() {
        return matches != null && matches.size() > 0;
    }
}
