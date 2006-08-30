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
package org.apache.geronimo.naming.java;

import java.util.Map;
import java.util.Iterator;
import javax.naming.NamingException;

/**
 * @version $Rev$ $Date$
 */
public final class SimpleReadOnlyContext extends ReadOnlyContext {
    private SimpleReadOnlyContext() {
    }

    public SimpleReadOnlyContext(Map context) throws NamingException {
        internalBind("env", new ReadOnlyContext());
        for (Iterator iterator = context.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            internalBind(name, value);
        }
        freeze();
    }

    protected ReadOnlyContext newContext() {
        return new SimpleReadOnlyContext();
    }
}
