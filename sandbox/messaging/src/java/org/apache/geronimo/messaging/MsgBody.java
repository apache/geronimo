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

/**
 * Msg body.
 *
 * @version $Rev$ $Date$
 */
public class MsgBody
    implements Externalizable
{

    /**
     * Body content.
     */
    private Object content;

    /**
     * Required for Externalization.
     */
    public MsgBody() {}
    
    /**
     * Prototype.
     * 
     * @param aBody Body to be duplicated.
     */
    public MsgBody(MsgBody aBody) {
        content = aBody.content;
    }
    
    /**
     * Gets the content of this body.
     * 
     * @return Content.
     */
    public Object getContent() {
        return content;
    }
    
    /**
     * Sets the content of this body.
     * 
     * @param aContent New body content.
     */
    public void setContent(Object aContent) {
        content = aContent;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(content);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        content = in.readObject();
    }

    public String toString() {
        return "MsgBody=" + content;
    }

    /**
     * Type-safe enumeration of body types.
     */
    public static class Type {

        /**
         * The content is a request.
         */
        public static final Type REQUEST = new Type(0);
        /**
         * The content is a response.
         */
        public static final Type RESPONSE = new Type(1);

        private static final Type[] types = new Type[] {REQUEST , RESPONSE};

        private int code;

        public static Type getByCode(int aCode) {
            return types[aCode]; 
        }

        /**
         * Required for Externalization.
         */
        public Type() {}
        
        private Type(int aCode) {code = aCode;}
        
        public int getCode() {
            return code;
        }
        
        public String toString() {
            return (0 == code ? "Request" : "Response");
        }
    }

}
