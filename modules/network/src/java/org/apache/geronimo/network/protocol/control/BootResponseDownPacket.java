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

package org.apache.geronimo.network.protocol.control;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.geronimo.network.protocol.control.commands.MenuItem;
import org.apache.geronimo.network.protocol.util.ByteKeyDownPacket;


/**
 * @version $Rev$ $Date$
 */
class BootResponseDownPacket extends ByteKeyDownPacket {

    private Collection menu;

    public BootResponseDownPacket() {
        super(AbstractControlProtocol.BOOT_RESPONSE);
    }

    public Collection getMenu() {
        return menu;
    }

    public void setMenu(Collection menu) {
        this.menu = menu;
    }

    protected Collection getChildBuffers() {
        ArrayList buffers = new ArrayList();
        buffers.add(ByteBuffer.allocate(4).putInt(menu.size()).flip());
        for (Iterator iter = menu.iterator(); iter.hasNext();) {
            buffers.addAll(((MenuItem) iter.next()).getBuffers());
        }
        return buffers;
    }
}
