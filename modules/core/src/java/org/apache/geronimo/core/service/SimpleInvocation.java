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

package org.apache.geronimo.core.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationKey;

/**
 *
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:27 $
 */
public class SimpleInvocation implements Invocation, Externalizable {
    
    private Map data;

    public Object get(InvocationKey key) {
        if(data==null)
            return null;
        return data.get(key);
    }

    public void put(InvocationKey key, Object value) {
        if(data==null)
            data = new HashMap();
        data.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        if( data !=null ) {
            Iterator iterator = data.keySet().iterator();
            while(iterator.hasNext()) {
                InvocationKey key = (InvocationKey) iterator.next();
                if( key.isTransient() )
                    continue; // don't serialize this item.
                Object value = data.get(key);
                out.writeObject(key);
                out.writeObject(value);
            }        
        }
        // write end of list terminator.
        out.writeObject(null);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
        if( data!=null )
            data.clear();
            
        InvocationKey key = (InvocationKey) in.readObject();
        while( key!=null ) {
            Object value = in.readObject();
            put(key,value);
            key = (InvocationKey) in.readObject();
        }
        
    }


}
