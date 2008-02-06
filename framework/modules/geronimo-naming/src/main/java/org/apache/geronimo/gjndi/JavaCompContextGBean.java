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
package org.apache.geronimo.gjndi;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.xbean.naming.context.ContextFlyweight;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @version $Rev$ $Date$
 */
public class JavaCompContextGBean extends ContextFlyweight {
    protected Context getContext() {
        Context context = RootContext.getComponentContext();
        if (context == null) {
            throw new NullPointerException("You have accessed the java:comp jndi context on a thread that has not initialized it");
        }
        return context;
    }

    public String getNameInNamespace() throws NamingException {
        return "java:comp";
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(JavaCompContextGBean.class, "Context");
        GBEAN_INFO = builder.getBeanInfo();
    }
}
