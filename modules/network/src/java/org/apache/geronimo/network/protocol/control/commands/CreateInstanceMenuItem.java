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

package org.apache.geronimo.network.protocol.control.commands;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Collections;

import org.apache.geronimo.network.protocol.PacketField;
import org.apache.geronimo.network.protocol.PacketFieldFactory;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.ControlException;
import org.apache.geronimo.network.protocol.util.PacketUtil;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:14 $
 */
public class CreateInstanceMenuItem extends MenuItemPacketField implements PacketFieldFactory {

    private String className;
    private Long instanceId;

    public CreateInstanceMenuItem() {
        super(CREATE);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    protected Collection getChildBuffers() {
        ByteBuffer buffer = ByteBuffer.allocate(PacketUtil.getStringSize(className) + PacketUtil.getLongSize());
        PacketUtil.putString(buffer, className);
        PacketUtil.putLong(buffer, instanceId);
        buffer.flip();

        return Collections.singletonList(buffer);
    }

    public PacketField create(ByteBuffer buffer) {
        CreateInstanceMenuItem item = new CreateInstanceMenuItem();
        item.setClassName(PacketUtil.getString(buffer));
        item.setInstanceId(PacketUtil.getLong(buffer));
        return item;
    }

    public Object execute(final ControlContext context) throws ControlException {
        try {
            return AccessController.doPrivileged(new
                    PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            Object object = Class.forName(className, true, context.getClassLoader()).newInstance();
                            context.register(instanceId, object);

                            return object;
                        }
                    });
        } catch (PrivilegedActionException pae) {
            throw new ControlException(pae.getException());
        }
    }
}
