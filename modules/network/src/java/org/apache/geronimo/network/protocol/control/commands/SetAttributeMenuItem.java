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
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:28 $
 */
public class SetAttributeMenuItem extends MenuItemPacketField implements MenuItem, PacketFieldFactory {

    private Long instanceId;
    private String attributeName;
    private Object attributeValue;

    public SetAttributeMenuItem() {
        super(ATTRIBUTE);
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(Object attributeValue) {
        if (!PacketUtil.isPrimitive(attributeValue)) throw new java.lang.IllegalArgumentException("Attribute must be a primitive");

        this.attributeValue = attributeValue;
    }

    protected Collection getChildBuffers() {
        ByteBuffer buffer = ByteBuffer.allocate(PacketUtil.getLongSize()
                                                + PacketUtil.getStringSize(attributeName)
                                                + PacketUtil.getPrimitiveSize(attributeValue));

        PacketUtil.putLong(buffer, instanceId);
        PacketUtil.putString(buffer, attributeName);
        PacketUtil.putPrimitive(buffer, attributeValue);
        buffer.flip();

        return Collections.singletonList(buffer);
    }

    public PacketField create(ByteBuffer buffer) throws ProtocolException {
        SetAttributeMenuItem item = new SetAttributeMenuItem();

        item.instanceId = PacketUtil.getLong(buffer);
        item.attributeName = PacketUtil.getString(buffer);
        item.attributeValue = PacketUtil.getPrimitive(buffer);

        return item;
    }

    public Object execute(ControlContext context) throws ControlException {
        Object target = context.retrieve(instanceId);

        if (target == null) throw new ControlException("Missing instance: " + instanceId);
        try {
            Class clazz = target.getClass();
            Method setter = clazz.getMethod("set" + attributeName, new Class[]{attributeValue.getClass()});
            setter = clazz.getMethod("set" + attributeName, new Class[]{attributeValue.getClass()});
            setter.invoke(target, new Object[]{attributeValue});

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
