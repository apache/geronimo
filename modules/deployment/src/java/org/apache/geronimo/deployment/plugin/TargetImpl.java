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

package org.apache.geronimo.deployment.plugin;

import javax.enterprise.deploy.spi.Target;

/**
 * 
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/02 18:05:46 $
 */
public class TargetImpl implements Target {
    private final String name;
    private final String description;

    public TargetImpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TargetImpl)) return false;

        final TargetImpl target = (TargetImpl) o;

        if (!name.equals(target.name)) return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
