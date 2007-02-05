/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jaxws;

import org.apache.geronimo.naming.java.RootContext;

import javax.naming.Context;
import javax.naming.NamingException;

public class ServerJNDIResolver extends JNDIResolver {

    private Context componentContext;

    public ServerJNDIResolver(Context context) {
        this.componentContext = context;
    }

    public Object resolve(String name, Class clz) throws NamingException {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);
            return super.resolve(name, clz);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
