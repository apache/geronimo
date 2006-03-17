/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.gbean;

import java.util.Set;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.io.Serializable;
import javax.management.ObjectName;

/**
 * @version $Rev:$ $Date:$
 */
public class ReferencePatterns implements Serializable {
    private static final long serialVersionUID = 1888371271299507818L;

    private final Set patterns;
    private final AbstractName abstractName;

    public ReferencePatterns(Set patterns) {
        this.patterns = new LinkedHashSet();
        for (Iterator iterator = patterns.iterator(); iterator.hasNext();) {
            Object pattern = iterator.next();
            if (pattern instanceof AbstractName) {
                AbstractName name = (AbstractName) pattern;
                this.patterns.add(new AbstractNameQuery(name));
            } else if (pattern instanceof AbstractNameQuery) {
                AbstractNameQuery nameQuery = (AbstractNameQuery) pattern;
                this.patterns.add(nameQuery);
            } else if (pattern instanceof ObjectName) {
                ObjectName objectName = (ObjectName) pattern;
                this.patterns.add(objectName);
            } else {
                throw new IllegalArgumentException("Unknown pattern type: " + pattern);
            }
        }
        this.abstractName = null;
    }

    public ReferencePatterns(AbstractNameQuery abstractNameQuery) {
        this.patterns = Collections.singleton(abstractNameQuery);
        this.abstractName = null;
    }

    public ReferencePatterns(AbstractName abstractName) {
        this.abstractName = abstractName;
        this.patterns = null;
    }

    public Set getPatterns() {
        if (patterns == null) {
            throw new IllegalStateException("This is resolved");
        }
        return patterns;
    }

    public AbstractName getAbstractName() {
        if (abstractName == null) {
            throw new IllegalStateException("This is not resolved");
        }
        return abstractName;
    }

    public boolean isResolved() {
        return abstractName != null;
    }

}
