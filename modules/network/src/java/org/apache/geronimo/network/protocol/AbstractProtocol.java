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

package org.apache.geronimo.network.protocol;

/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:13 $
 */
public abstract class AbstractProtocol implements Protocol {

    private Protocol up;
    private Protocol down;

    public Protocol getUp() {
        return up;
    }

    public void setUp(Protocol up) {
        this.up = up;
    }

    public Protocol getDown() {
        return down;
    }

    public void setDown(Protocol down) {
        this.down = down;
    }

    public void clearLinks() {
        up = down = null;
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        return (Protocol) super.clone();
    }
}
