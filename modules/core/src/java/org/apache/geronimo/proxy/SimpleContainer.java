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

package org.apache.geronimo.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.common.NullArgumentException;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:29 $
 */
public class SimpleContainer extends SimpleComponent implements Container {

    private ArrayList components = new ArrayList();

    /**
     * @see org.apache.geronimo.core.service.Container#addComponent(org.apache.geronimo.core.service.Component)
     */
    public void addComponent(Component component) {
        if (component == null)
            throw new NullArgumentException("component");

        components.add(component);
    }

    /**
     * @see org.apache.geronimo.core.service.Container#getComponents()
     */
    public List getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * @see org.apache.geronimo.core.service.Container#removeComponent(org.apache.geronimo.core.service.Component)
     */
    public void removeComponent(Component component) throws Exception {
        if (component == null)
            throw new NullArgumentException("component");
        components.remove(component);
    }

}
