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

package org.apache.geronimo.proxy;

import java.io.Serializable;

import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.core.service.Container;

/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:43 $
 */
public class SimpleComponent implements Component, Serializable {

    private Container container;
    private String objectName;

    /**
     * @see org.apache.geronimo.core.service.Component#getContainer()
     */
    public Container getContainer() {
        return container;
    }

    /**
     * @see org.apache.geronimo.core.service.Component#setContainer(org.apache.geronimo.core.service.Container)
     */
    public void setContainer(Container container) throws IllegalStateException, IllegalArgumentException {
        this.container = container;

    }

    /**
     * @see org.apache.geronimo.core.service.Component#getObjectName()
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @param objectName
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

}
