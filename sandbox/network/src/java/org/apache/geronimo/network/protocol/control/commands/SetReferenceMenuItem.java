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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import org.apache.geronimo.network.protocol.PacketField;
import org.apache.geronimo.network.protocol.PacketFieldFactory;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.ControlException;
import org.apache.geronimo.network.protocol.util.PacketUtil;


/**
 * @version $Rev$ $Date$
 */
public class SetReferenceMenuItem extends MenuItemPacketField implements MenuItem, PacketFieldFactory {

    private Long instanceId;
    private String referenceName;
    private Long referenceId;

    public SetReferenceMenuItem() {
        super(REFERENCE);
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    protected Collection getChildBuffers() {
        ByteBuffer buffer = ByteBuffer.allocate(PacketUtil.getLongSize()
                                                + PacketUtil.getStringSize(referenceName)
                                                + PacketUtil.getLongSize());

        PacketUtil.putLong(buffer, instanceId);
        PacketUtil.putString(buffer, referenceName);
        PacketUtil.putLong(buffer, referenceId);
        buffer.flip();

        return Collections.singletonList(buffer);
    }

    public PacketField create(ByteBuffer buffer) throws ProtocolException {
        SetReferenceMenuItem item = new SetReferenceMenuItem();

        item.instanceId = PacketUtil.getLong(buffer);
        item.referenceName = PacketUtil.getString(buffer);
        item.referenceId = PacketUtil.getLong(buffer);

        return item;
    }

    public Object execute(ControlContext context) throws ControlException {
        Object target = context.retrieve(instanceId);
        Object value = context.retrieve(referenceId);

        if (target == null) throw new ControlException("Missing instance: " + instanceId);
        try {
            Class clazz = target.getClass();
            Method setter = clazz.getMethod("set" + referenceName, new Class[]{value.getClass()});
            setter.invoke(target, new Object[]{value});

            return null;
        } catch (NoSuchMethodException e) {
            throw new ControlException("Trying to set an attribute", e);
        } catch (IllegalAccessException e) {
            throw new ControlException("Trying to set an attribute", e);
        } catch (InvocationTargetException e) {
            throw new ControlException("Trying to set an attribute", e);
        }
    }

}
