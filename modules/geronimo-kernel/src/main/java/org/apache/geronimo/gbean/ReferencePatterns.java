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

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class ReferencePatterns implements Serializable {
    private static final long serialVersionUID = 1888371271299507818L;

    private final Set<AbstractNameQuery> patterns;
    private final AbstractName abstractName;

    public ReferencePatterns(Set<? extends Object> patterns) {
        this.patterns = new LinkedHashSet<AbstractNameQuery>();
        for (Object pattern : patterns) {
            if (pattern instanceof AbstractName) {
                AbstractName name = (AbstractName) pattern;
                this.patterns.add(new AbstractNameQuery(name));
            } else if (pattern instanceof AbstractNameQuery) {
                AbstractNameQuery nameQuery = (AbstractNameQuery) pattern;
                this.patterns.add(nameQuery);
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
        if (abstractName == null) {
            throw new IllegalArgumentException("parameter abstractName is null");
        }
        this.abstractName = abstractName;
        this.patterns = null;
    }

    public Set<AbstractNameQuery> getPatterns() {
        if (patterns == null) {
            throw new IllegalStateException("This is resolved to: " + abstractName);
        }
        return patterns;
    }

    public AbstractName getAbstractName() {
        if (abstractName == null) {
            throw new IllegalStateException("This is not resolved with patterns: " + patterns);
        }
        return abstractName;
    }

    public boolean isResolved() {
        return abstractName != null;
    }

    public String toString() {
        if (abstractName != null) {
            return abstractName.toString();
        } else {
            return patterns.toString();
        }
    }

    public boolean equals(Object other) {
        if (other instanceof ReferencePatterns) {
            ReferencePatterns otherRefPat = (ReferencePatterns) other;
            if (abstractName != null) {
                return abstractName.equals(otherRefPat.abstractName);
            }
            return patterns.equals(otherRefPat.patterns);
        }
        return false;
    }

    public int hashCode() {
        if (abstractName != null) {
            return abstractName.hashCode();
        }
        return patterns.hashCode();
    }
}
