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

package org.apache.geronimo.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.geronimo.messaging.MsgBody.Type;
import org.apache.geronimo.messaging.io.AbstractReplacerResolver;
import org.apache.geronimo.messaging.io.ReplacerResolver;

/**
 * ReplacerResolver for the base classes of Msgs.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/10 23:12:24 $
 */
public class MsgReplacerResolver
    extends AbstractReplacerResolver
    implements ReplacerResolver
{

    protected Object customReplaceObject(Object obj) throws IOException {
        if ( obj instanceof MsgBody.Type ) {
            return new TypeWrapper((MsgBody.Type) obj);
        }
        return null;
    }

    protected Object customResolveObject(Object obj) throws IOException {
        if ( obj instanceof TypeWrapper ) {
            return ((TypeWrapper) obj).getType();
        }
        return null;
    }

    public static class TypeWrapper implements Externalizable {

        private int code;
        
        public TypeWrapper() {}
        
        public TypeWrapper(Type aType) {
            code = aType.getCode();
        }
        
        public Type getType() {
            return MsgBody.Type.getByCode(code);
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            code = in.readInt();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(code);
        }
        
    }
    
}
