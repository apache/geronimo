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
package org.apache.geronimo.cxf;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JNDIResolver {

    private static final Log LOG = 
        LogFactory.getLog(JNDIResolver.class.getName());

    private Context componentContext;

    public JNDIResolver(Context context) {
        this.componentContext = context;
    }

    public Object resolve(String name, Class clz) throws NamingException {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);

            Context ctx = new InitialContext();
            ctx = (Context) ctx.lookup("java:comp/env");

            LOG.debug("Looking up '" + name + "'");

            Object o = ctx.lookup(name);

            return clz.cast(o);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
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
package org.apache.geronimo.cxf;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JNDIResolver {

    private static final Log LOG = 
        LogFactory.getLog(JNDIResolver.class.getName());

    private Context componentContext;

    public JNDIResolver(Context context) {
        this.componentContext = context;
    }

    public Object resolve(String name, Class clz) throws NamingException {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);

            Context ctx = new InitialContext();
            ctx = (Context) ctx.lookup("java:comp/env");

            LOG.debug("Looking up '" + name + "'");

            Object o = ctx.lookup(name);

            return clz.cast(o);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
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
package org.apache.geronimo.cxf;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JNDIResolver {

    private static final Log LOG = 
        LogFactory.getLog(JNDIResolver.class.getName());

    private Context componentContext;

    public JNDIResolver(Context context) {
        this.componentContext = context;
    }

    public Object resolve(String name, Class clz) throws NamingException {
        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);

            Context ctx = new InitialContext();
            ctx = (Context) ctx.lookup("java:comp/env");

            LOG.debug("Looking up '" + name + "'");

            Object o = ctx.lookup(name);

            return clz.cast(o);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
