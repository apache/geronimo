/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop;

import org.apache.geronimo.interop.adapter.Adapter;

import java.util.HashMap;

public abstract class RemoteObject {

    protected HashMap   methods = new HashMap(10);

    public RemoteObject( ) {
        registerMethods();
    }

    protected void registerMethods() {
        registerMethod("_is_a", -1);
    }

    protected void registerMethod( String methodName, int id )
    {
        methods.put( methodName, new Integer(id) );
    }

    protected Integer getMethodId( String methodName )
    {
        return (Integer)methods.get( methodName );
    }

    public void invoke(int id, byte[] objectKey, Object instance, ObjectInputStream input, ObjectOutputStream output) {
        switch (id) {
            case -1:
                {
                    output.writeBoolean(_is_a(objectKey));
                    break;
                }
        }
    }

    public boolean _is_a(byte[] objectKey) {
        String ids[] = getIds();
        boolean isa = false;

        String id = new String(objectKey);

        if (ids != null && ids.length > 0) {
            int i;
            for (i = 0; i < ids.length && !isa; i++) {
                isa = ids[i].equals(id);

            }
        }

        return isa;
    }

    public abstract String[] getIds();
}
