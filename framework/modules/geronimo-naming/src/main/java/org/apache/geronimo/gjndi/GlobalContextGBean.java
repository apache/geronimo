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
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.xbean.naming.global.GlobalContextManager;
import org.osgi.framework.BundleContext;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Name;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
*/ 
@GBean(j2eeType = "GlobalContext")
public class GlobalContextGBean extends KernelContextGBean implements GBeanLifecycle {
    public GlobalContextGBean(@ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws NamingException {
        super("", new AbstractNameQuery(null, Collections.<String, Object>emptyMap(), Context.class.getName()), kernel, bundleContext);
    }

    @Override
    public void doStart() {
        super.doStart();
        GlobalContextManager.setGlobalContext(getContext());
    }

    @Override
    public void doStop() {
        GlobalContextManager.setGlobalContext(null);
        super.doStop();
    }

    @Override
    public void doFail() {
        GlobalContextManager.setGlobalContext(null);
        super.doFail();
    }

    @Override
    protected Name createBindingName(AbstractName abstractName, Object value) throws NamingException {
        if (value instanceof Context) {
            // don't bind yourself
            if (value == this) return null;

            Context context = (Context) value;
            String nameInNamespace = context.getNameInNamespace();
            return getContext().getNameParser("").parse(nameInNamespace);
        }
        throw new NamingException("value is not a context: abstractName=" + abstractName + " valueType=" + value.getClass().getName());
    }

}
