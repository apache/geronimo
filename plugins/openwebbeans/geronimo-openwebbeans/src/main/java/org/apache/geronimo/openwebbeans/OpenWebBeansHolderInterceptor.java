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

package org.apache.geronimo.openwebbeans;

import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.Holder.InterceptorException;
import org.apache.geronimo.j2ee.annotation.Holder.InvocationContext;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;

/**
 * @version $Rev$ $Date$
 */
public class OpenWebBeansHolderInterceptor implements Holder.Interceptor {

    private final WebBeansContext webBeansContext;

    public OpenWebBeansHolderInterceptor(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public void instancerCreated(InvocationContext context) throws InterceptorException {
        OWBInjector beanInjector = new OWBInjector(webBeansContext);
        try {
            beanInjector.inject(context.getInstance());
        } catch (Exception e) {
            throw new InterceptorException("web beans injection problem", e);
        }
    }

    @Override
    public void instanceDestoryed(InvocationContext context) throws InterceptorException {
    }

    @Override
    public void postConstructInvoked(InvocationContext context) throws InterceptorException {
    }

}
