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

package org.apache.geronimo.core.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.geronimo.core.service.InvocationKey;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:42 $
 */
public class StringInvocationKey implements InvocationKey, Externalizable {
    
    private String key;
    private boolean isTransient;
    private int hashCode;   
    
    public StringInvocationKey(String key, boolean isTransient) {
        this.key = key;
        this.hashCode = key.hashCode();
        this.isTransient = isTransient;
    }
     
    /**
     * @return
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return hashCode;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if( obj == null )
            return false;
        if( !StringInvocationKey.class.equals(obj.getClass()) )
            return false;
        return key.equals( ((StringInvocationKey)obj).key );
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(key);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        key = in.readUTF();
        hashCode = key.hashCode();
        isTransient = false;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return key;
    }
}
